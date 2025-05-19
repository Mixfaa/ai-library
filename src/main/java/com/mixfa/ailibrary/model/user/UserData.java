package com.mixfa.ailibrary.model.user;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.ReadBook;
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
        @Id long id,
        @DBRef
        Account owner,
        @DBRef Book[] waitList,
        @DBRef ReadBook[] readBooks,
        Locale targetLocale
) implements HasOwner {
    public static Criteria ownerCriteria() {
        return Criteria.where("_id").is(Account.getAuthenticatedAccount().getId());
    }

    public static Criteria ownerCriteriaBy(Long userID) {
        return Criteria.where("_id").is(userID);
    }

    public UserData(Account owner) {
        this(owner.getId(), owner, new Book[0], new ReadBook[0], DEFAULT_LOCALE);
    }

    // TODO
    public UserData(Book[] waitList) {
        this(
                Account.getAuthenticatedAccount().getId(),
                Account.getAuthenticatedAccount(),
                waitList,
                new ReadBook[0],
                DEFAULT_LOCALE
        );
    }

    public UserData(ReadBook[] readBooks) {
        this(
                Account.getAuthenticatedAccount().getId(),
                Account.getAuthenticatedAccount(),
                new Book[0],
                readBooks,
                DEFAULT_LOCALE
        );
    }

    public UserData(Locale locale) {
        this(
                Account.getAuthenticatedAccount().getId(),
                Account.getAuthenticatedAccount(),
                new Book[0],
                new ReadBook[0],
                locale
        );
    }

    public static final String COLLECTION_NAME = MongoCollectionUtils.getPreferredCollectionName(UserData.class);
}
