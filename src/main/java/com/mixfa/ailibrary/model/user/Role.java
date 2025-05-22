package com.mixfa.ailibrary.model.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public enum Role {
    USER,
    WORKER,
    ADMIN;

    @Getter
    private final String roleName = "ROLE_" + this.name();

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String WORKER_ROLE = "ROLE_WORKER";
    public static final String USER_ROLE = "ROLE_USER";
    public final Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(this.roleName));
}
