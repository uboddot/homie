package com.homie.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homie.model.AppUser;
import com.homie.model.EmailVerificationToken;
import com.homie.repository.EmailVerificationTokenRepository;
import com.homie.repository.UserRepository;

/** Creates the one-time links for the verification mail and checks them when they are clicked. */
@Service
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final String frontendUrl;
    private final Duration validity;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository, UserRepository userRepository,
            MailService mailService, @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.verification-token-hours}") long validityHours) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.frontendUrl = frontendUrl;
        this.validity = Duration.ofHours(validityHours);
    }

    /** Replaces any older link of the user with a new one and mails it. */
    @Transactional
    public void sendVerification(AppUser user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        tokenRepository.deleteByUser(user);
        tokenRepository.save(new EmailVerificationToken(user, hash(token), Instant.now().plus(validity)));

        mailService.sendVerificationMail(user.getEmail(), user.getUsername(),
                frontendUrl + "/verify-email?token=" + token);
    }

    /** Marks the user's email as verified. Empty if the token is unknown, already used or expired. */
    @Transactional
    public Optional<AppUser> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Optional<EmailVerificationToken> stored = tokenRepository.findByTokenHash(hash(token));
        if (stored.isEmpty()) {
            return Optional.empty();
        }

        EmailVerificationToken verification = stored.get();
        tokenRepository.delete(verification); // one-time use, also when expired
        if (verification.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }

        AppUser user = verification.getUser();
        user.setEmailVerified(true);
        return Optional.of(userRepository.save(user));
    }

    /** Sends a new link if the email belongs to a not yet verified user, otherwise does nothing. */
    @Transactional
    public void resend(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmail(email.trim())
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerification);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
