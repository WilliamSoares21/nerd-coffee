package com.nerdcoffe.domain;

public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    EDITOR("ROLE_EDITOR"),
    VIEWER("ROLE_VIEWER");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
