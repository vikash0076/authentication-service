package com.org.repository;

import com.org.entity.User;
import com.org.entity.UserSections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSectionsRepository extends JpaRepository<UserSections, Long> {
    java.util.Optional<UserSections> findByUserAndRefreshTokenHash(User user, String refreshTokenHash);
}
