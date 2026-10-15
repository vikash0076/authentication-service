package com.org.service;

import com.org.apimodels.AuthResponse;
import com.org.apimodels.LoginRequest;
import com.org.apimodels.RefreshTokenRequest;
import com.org.entity.User;
import com.org.entity.UserSections;
import com.org.repository.UserSectionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UserSectionsRepository userSectionsRepository;

    public AuthResponse login(LoginRequest request, String deviceId, String deviceName, 
                              String ipAddress, String userAgent) {
        String identifier = request.getEmail() != null ? request.getEmail() : request.getPhoneNumber();
        
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(identifier);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        saveRefreshToken(identifier, refreshToken, deviceId, deviceName, ipAddress, userAgent);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request, String deviceId, String deviceName,
                                     String ipAddress, String userAgent) {
        String refreshToken = request.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        if (jwtService.isTokenValid(refreshToken, userDetails)) {
            String accessToken = jwtService.generateToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            revokeOldRefreshToken(username, refreshToken);
            saveRefreshToken(username, newRefreshToken, deviceId, deviceName, ipAddress, userAgent);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        }
        
        throw new RuntimeException("Invalid refresh token");
    }

    public void logout(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        revokeOldRefreshToken(username, refreshToken);
    }

    private void saveRefreshToken(String username, String refreshToken, String deviceId, 
                                   String deviceName, String ipAddress, String userAgent) {
        User user = userService.findByEmailOrPhone(username);
        
        String tokenHash = hashToken(refreshToken);
        
        UserSections userSection = UserSections.builder()
                .user(user)
                .refreshTokenHash(tokenHash)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .lastActiveAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        userSectionsRepository.save(userSection);
    }

    private void revokeOldRefreshToken(String username, String refreshToken) {
        User user = userService.findByEmailOrPhone(username);
        String tokenHash = hashToken(refreshToken);
        
        userSectionsRepository.findByUserAndRefreshTokenHash(user, tokenHash)
                .ifPresent(section -> {
                    section.setRevokedAt(LocalDateTime.now());
                    userSectionsRepository.save(section);
                });
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}
