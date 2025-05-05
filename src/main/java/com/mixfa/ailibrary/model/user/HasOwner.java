package com.mixfa.ailibrary.model.user;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.model.search.SearchOption;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;


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

    static SearchOption ownerSearchOption() {
        return () -> List.of(Aggregation.match(HasOwner.ownerCriteria()));
    }
}
