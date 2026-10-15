package com.org.repository;

import com.org.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    java.util.Optional<User> findByEmail(String email);
    java.util.Optional<User> findByPhoneNumber(String phoneNumber);
}
