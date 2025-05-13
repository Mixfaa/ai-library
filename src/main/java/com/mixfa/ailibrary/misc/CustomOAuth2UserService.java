package com.mixfa.ailibrary.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.AuthenticatedAccount;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.service.AdminAuthenticator;
import com.mixfa.ailibrary.service.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final AdminAuthenticator adminAuthenticator;
    private final AccountRepo accountRepo;
    private final ObjectMapper objectMapper;

    private record GithubEmail(
            String email,
            boolean primary,
            boolean verified,
            String visibility
    ) {
    }

    @SneakyThrows
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        var accessToken = userRequest.getAccessToken().getTokenValue();
        var request = HttpRequest.newBuilder(URI.create("https://api.github.com/user/emails"))
                .GET()
                .header("Authorization", "Bearer " + accessToken)
                .headers("Accept", "application/vnd.github+json")
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        GithubEmail[] emails;
        try {

            emails = objectMapper.readValue(response.body(), GithubEmail[].class);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        var primaryEmail = Utils.find(emails, GithubEmail::primary).orElseThrow().email;

        var role = adminAuthenticator.isAdmin(primaryEmail) ? Role.ADMIN : Role.USER;

        var oauth2User = super.loadUser(userRequest);

        String username = oauth2User.getAttribute("login");
        long id = ((Number) Objects.requireNonNull(oauth2User.getAttribute("id"), "ID cannot be null"))
                .longValue();

        Objects.requireNonNull(username, "Username cannot be null");

        var account = accountRepo.findById(id)
                .orElseGet(() -> accountRepo.save(new Account(id, username, primaryEmail, role)));

        return new AuthenticatedAccount(account, oauth2User);
    }
}
