package com.mixfa.ailibrary.service.user.impl;

import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.AuthenticatedAccount;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.service.repo.AccountRepo;
import com.mixfa.ailibrary.service.user.AccountService;
import com.mixfa.ailibrary.service.user.AdminAuthenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepo accountRepo;
    private final AdminAuthenticator adminAuthenticator;
    private final MongoTemplate mongoTemplate;

    @Override
    public Account findOrThrow(String id) {
        return accountRepo.findById(id).orElseThrow();
    }

    @Override
    public Account getOrCreateAccount(String id, String email, String name) {
        var accountOpt = accountRepo.findById(id);
        if (accountOpt.isPresent()) return accountOpt.get();

        return accountRepo.save(new Account(id, name, email, adminAuthenticator.isAdmin(email) ? Role.ADMIN : Role.USER));
    }

    @Override
    public void editUsername(String username) {
        if (username.isBlank()) return;
        var authenticated = Account.getAuthenticated();
        var account = authenticated.getAccount();

        mongoTemplate.updateFirst(
                Query.query(
                        Criteria.where(Account.Fields.id).is(account.getId())
                ),
                new Update().set(Account.Fields.username, username),
                Account.class
        );

        account = account.withUsername(username);

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof OAuth2AuthenticationToken oauth2Token) {
            SecurityContextHolder.getContext().setAuthentication(
                    new OAuth2AuthenticationToken(
                            new AuthenticatedAccount(account, authenticated.getUser()),
                            authenticated.getAuthorities(),
                            oauth2Token.getAuthorizedClientRegistrationId()
                    )
            );
        }  else {
            SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
        }
    }
}
