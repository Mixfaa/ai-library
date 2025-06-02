package com.mixfa.ailibrary.service.user;

import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {
    Account findOrThrow(String id);

    Account getOrCreateAccount(String id, String email, String name);

    void editUsername(String username);

    void changeRole(String userId, Role role, String adminPassword);

    Page<Account> findAccounts(SearchOption searchOption, Pageable pageable);
}
