package com.homie.security;

public enum ApplicationUserPermission {
    RECIPE_READ("recipe:read"),
    RECIPE_WRITE("recipe:write"),
    RECIPE_ADMIN("recipe:admin");

    private final String permission;

    private ApplicationUserPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
