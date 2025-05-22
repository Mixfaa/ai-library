package com.mixfa.ailibrary.model.user;

import com.mixfa.ailibrary.model.Library;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class AuthenticatedAccount implements OidcUser, UserDetails {
    private final Account account;
    private final OidcUser user;

    public boolean isWorkerOfLibrary(Library library) {
        if (account instanceof LibraryWorker libraryWorker)
            return libraryWorker.getLibrary().name().equals(library.name());
        return false;
    }

    public String id() {
        return account.getId();
    }

    public Role role() {
        return account.getRole();
    }

    public String username() {
        return account.getUsername();
    }

    public String email() {
        return account.getEmail();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public Map<String, Object> getClaims() {
        return user.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return user.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return user.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return user.getAttributes();
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
