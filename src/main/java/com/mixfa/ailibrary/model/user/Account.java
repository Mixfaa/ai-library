package com.mixfa.ailibrary.model.user;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Library;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Document("account")
@Getter
@RequiredArgsConstructor
@With
@FieldNameConstants
public class Account implements UserDetails {
    @Id
    private final String id;
    private final String username;
    private final String email;
    private final Role role;

    public boolean isWorkerOfLibrary(Library library) {
        return false;
    }

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
        return getAuthenticated().getAccount();
    }

    public static AuthenticatedAccount getAuthenticated() {
        return Utils.getPrincipal();
    }

    public static boolean isAdminAuthenticated() {
        return getAuthenticatedAccount().role.isAdmin();
    }
}
