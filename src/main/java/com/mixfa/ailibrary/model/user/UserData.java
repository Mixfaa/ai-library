package com.mixfa.ailibrary.model.user;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.ReadBook;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.MongoCollectionUtils;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Locale;

import static com.mixfa.ailibrary.misc.Utils.DEFAULT_LOCALE;

@Document
@With
@FieldNameConstants
public record UserData(
        @Id String id,
        @DBRef Account owner,
        @DBRef Book[] waitList,
        ReadBook[] readBooks,
        Locale targetLocale
) implements HasOwner {
    public static Criteria ownerCriteria() {
        return Criteria.where("_id").is(Account.getAuthenticated().id());
    }

    public static Criteria ownerCriteriaBy(String userID) {
        return Criteria.where("_id").is(userID);
    }

    public UserData() {
        this(
                Account.getAuthenticatedAccount().getId(),
                Account.getAuthenticatedAccount(),
                new Book[0],
                new ReadBook[0],
                DEFAULT_LOCALE
        );
    }

    public UserData(Account owner) {
        this(owner.getId(), owner, new Book[0], new ReadBook[0], DEFAULT_LOCALE);
    }

    public static final String COLLECTION_NAME = MongoCollectionUtils.getPreferredCollectionName(UserData.class);
}
