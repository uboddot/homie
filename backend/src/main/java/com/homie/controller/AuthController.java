package com.homie.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
import com.homie.controller.dto.AuthDtos.RegisterRequest;
import com.homie.controller.dto.AuthDtos.UserResponse;
import com.homie.jwt.JwtService;
import com.homie.model.AppUser;
import com.homie.repository.UserRepository;
import com.homie.security.ApplicationUserRole;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
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

        return withTokenCookie(user, HttpStatus.CREATED);
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
