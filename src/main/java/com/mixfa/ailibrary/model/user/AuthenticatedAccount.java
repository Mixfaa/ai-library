package com.mixfa.ailibrary.model.user;

import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;


public record AuthenticatedAccount(
        Account account,
        OAuth2User oAuth2User
) implements UserDetails, OAuth2User {
    public Role role() {
        return account.getRole();
    }

    public String username() {
        return account.getUsername();
    }

    public long id() {
        return account.getId();
    }

    public String email() {
        return account.getEmail();
    }

    @Override
    public String getName() {
        return oAuth2User.getName();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return account.getAuthorities();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }
}