package com.mixfa.ailibrary.model.user;

import com.mixfa.ailibrary.misc.ExceptionType;
import org.springframework.data.mongodb.core.query.Criteria;


public interface HasOwner {
    Account owner();

    default void throwIfNotOwned() {
        if (owner().getId() != Account.getAuthenticatedAccount().getId())
            throw ExceptionType.accessDenied();
    }


    static Criteria ownerCriteria(Account account) {
        return Criteria.where("owner").is(account);
    }

    static Criteria ownerCriteria() {
        return ownerCriteria(Account.getAuthenticated().account());
    }
}
