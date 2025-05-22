package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.service.AdminAuthenticator;
import com.mixfa.ailibrary.service.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AccountRepo accountRepo;
    private final AdminAuthenticator adminAuthenticator;

    public Account getOrCreateAccount(String id, String email, String name) {
        var accountOpt = accountRepo.findById(id);
        if (accountOpt.isPresent()) return accountOpt.get();

        return accountRepo.save(new Account(id, name, email, adminAuthenticator.isAdmin(email) ? Role.ADMIN : Role.USER));
    }
}
