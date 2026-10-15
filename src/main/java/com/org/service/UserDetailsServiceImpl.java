package com.org.service;

import com.org.entity.User;
import com.org.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByEmailOrPhone(username);
        
        List<SimpleGrantedAuthority> authorities = user.getUserRoles() != null
                ? user.getUserRoles().stream()
                        .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getName()))
                        .collect(Collectors.toList())
                : Collections.emptyList();
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail() != null ? user.getEmail() : user.getPhoneNumber())
                .authorities(authorities)
                .build();
    }
}
