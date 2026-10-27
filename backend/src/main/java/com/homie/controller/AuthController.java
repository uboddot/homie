package com.homie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homie.controller.dto.AuthDtos.ErrorResponse;
import com.homie.controller.dto.AuthDtos.LoginRequest;
import com.homie.controller.dto.AuthDtos.MessageResponse;
import com.homie.controller.dto.AuthDtos.RegisterRequest;
import com.homie.controller.dto.AuthDtos.ResendVerificationRequest;
import com.homie.controller.dto.AuthDtos.UserResponse;
import com.homie.controller.dto.AuthDtos.VerifyEmailRequest;
import com.homie.jwt.JwtService;
import com.homie.model.AppUser;
import com.homie.repository.UserRepository;
import com.homie.security.ApplicationUserRole;
import com.homie.services.EmailVerificationService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService,
            EmailVerificationService emailVerificationService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Please confirm your email first"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Wrong username or password"));
        }

        AppUser user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return withTokenCookie(user, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (isBlank(request.username()) || isBlank(request.email()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username, email and password are required"));
        }
        if (request.password().length() < 8) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Password must be at least 8 characters"));
        }
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Username is already taken"));
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Email is already registered"));
        }

        AppUser user = userRepository.save(new AppUser(
                request.username().trim(),
                request.email().trim(),
                passwordEncoder.encode(request.password()),
                ApplicationUserRole.USER));

        try {
            emailVerificationService.sendVerification(user);
        } catch (MailException e) {
            log.warn("Could not send the verification mail", e);
            // without the mail the account could never be activated, so don't keep it
            userRepository.delete(user);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse("Could not send the confirmation email, please try again later"));
        }

        // no cookie yet: the user is logged in once the link in the mail was clicked
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new MessageResponse("Check your email to confirm your account"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        return emailVerificationService.verify(request.token())
                .<ResponseEntity<?>>map(user -> withTokenCookie(user, HttpStatus.OK))
                .orElse(ResponseEntity.badRequest().body(new ErrorResponse("This link is invalid or has expired")));
    }

    /** Always 202, so nobody can find out which emails are registered. */
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@RequestBody ResendVerificationRequest request) {
        try {
            emailVerificationService.resend(request.email());
        } catch (MailException e) {
            log.warn("Could not send the verification mail", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new MessageResponse("Could not send the confirmation email, please try again later"));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new MessageResponse("If the email belongs to an unconfirmed account, a new link is on its way"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, jwtService.createClearingCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .map(user -> ResponseEntity.ok(UserResponse.from(user)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private ResponseEntity<UserResponse> withTokenCookie(AppUser user, HttpStatus status) {
        String token = jwtService.createToken(user.getUsername(), user.getRole().getGrantedAuthorities());
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, jwtService.createCookie(token).toString())
                .body(UserResponse.from(user));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
