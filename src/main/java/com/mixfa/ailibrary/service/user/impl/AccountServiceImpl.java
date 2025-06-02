package com.mixfa.ailibrary.service.user.impl;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.AuthenticatedAccount;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.service.repo.AccountRepo;
import com.mixfa.ailibrary.service.search.SearchEngine;
import com.mixfa.ailibrary.service.user.AccountService;
import com.mixfa.ailibrary.service.user.AdminAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepo accountRepo;
    private final AdminAuthenticator adminAuthenticator;
    private final MongoTemplate mongoTemplate;
    private final SessionRegistry sessionRegistry;
    private final SearchEngine.ForAccounts accountsSearchEngine;
    private final String adminPassword;

    public AccountServiceImpl(AccountRepo accountRepo, AdminAuthenticator adminAuthenticator, MongoTemplate mongoTemplate, SessionRegistry sessionRegistry, SearchEngine.ForAccounts accountsSearchEngine, @Value("${adminutils.password}") String adminPassword) {
        this.accountRepo = accountRepo;
        this.adminAuthenticator = adminAuthenticator;
        this.mongoTemplate = mongoTemplate;
        this.sessionRegistry = sessionRegistry;
        this.accountsSearchEngine = accountsSearchEngine;
        this.adminPassword = adminPassword;
    }

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
        } else {
            SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
        }
    }

    @Override
    public void changeRole(String userId, Role role, String adminPassword) {
        var authenticated = Account.getAuthenticated();
        if (!authenticated.role().isAdmin())
            throw ExceptionType.accessDenied();

        if (!this.adminPassword.equals(adminPassword))
            throw ExceptionType.accessDenied();

        var user = findOrThrow(userId);
        if (user.getRole() == role)
            return;

        var savedUser = accountRepo.save(user.withRole(role));

        sessionRegistry.getAllPrincipals()
                .stream()
                .filter(principal -> principal instanceof AuthenticatedAccount)
                .map(AuthenticatedAccount.class::cast)
                .filter(acc -> acc.id().equals(savedUser.getId()))
                .forEach(account -> {
                    try {
                        var sessions = sessionRegistry.getAllSessions(account, false);
                        for (SessionInformation session : sessions)
                            session.expireNow();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                });
    }

    @Override
    public Page<Account> findAccounts(SearchOption searchOption, Pageable pageable) {
        return accountsSearchEngine.find(searchOption, pageable);
    }
}
