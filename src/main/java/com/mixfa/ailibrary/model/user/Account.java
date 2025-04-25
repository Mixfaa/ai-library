package com.mixfa.ailibrary.model.user;

import com.mixfa.ailibrary.misc.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Document
@Getter
@RequiredArgsConstructor
public class Account implements UserDetails {
    @Id
    private final long id;
    private final String username;
    private final String email;
    private final Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    public static Account getAuthenticatedAccount() {
        return getAuthenticated().account();
    }

    public static AuthenticatedAccount getAuthenticated() {
        return Utils.getPrincipal();
    }

    public static boolean isAdminAuthenticated() {
        return getAuthenticatedAccount().role.isAdmin();
    }
}
