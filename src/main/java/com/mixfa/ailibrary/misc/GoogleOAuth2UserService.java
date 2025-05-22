package com.mixfa.ailibrary.misc;

import com.mixfa.ailibrary.model.user.AuthenticatedAccount;
import com.mixfa.ailibrary.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2UserService extends OidcUserService {
    private final UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        if (!"google".equals(userRequest.getClientRegistration().getRegistrationId()))
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);

        var email = userRequest.getIdToken().getEmail();
        var username = userRequest.getIdToken().getFullName();
        var id = userRequest.getIdToken().getSubject();

        var account = userService.getOrCreateAccount(id, email, username);
        var user = super.loadUser(userRequest);

        return new AuthenticatedAccount(account, user);
    }
}
