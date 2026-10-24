package com.homie.controller.dto;

import com.homie.model.AppUser;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(String username, String password) {
    }

    public record RegisterRequest(String username, String email, String password) {
    }

    /** What the frontend gets to see of a user (never the password hash). */
    public record UserResponse(Long id, String username, String email, String role) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        }
    }

    public record ErrorResponse(String message) {
    }
}
