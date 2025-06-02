package com.mixfa.ailibrary.service.user;

import com.mixfa.ailibrary.model.user.Account;

public interface AccountService {
    Account findOrThrow(String id);

    Account getOrCreateAccount(String id, String email, String name);

    void editUsername(String username);
}
