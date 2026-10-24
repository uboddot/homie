package com.homie.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.homie.model.AppUser;
import com.homie.model.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    void deleteByUser(AppUser user);
}
