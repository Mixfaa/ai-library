package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.CachedRuntimeWrapperClassGen;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.ReadBook;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.UserData;
import com.mixfa.ailibrary.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class UserDataServiceImpl implements UserDataService {
    private final MongoTemplate mongoTemplate;

    @Override
    public UserData getUserData() {
        var q = Query.query(UserData.ownerCriteria());
        var userData = mongoTemplate.findOne(q, UserData.class);
        if (userData != null)
            return userData;

        var account = Account.getAuthenticatedAccount();
        return mongoTemplate.save(new UserData(account));
    }


    private UserData saveUserData(UserData userData) {
        userData.throwIfNotOwned();
        return mongoTemplate.save(userData);
    }

    private <T> T fetchField(String field, Class<T> tClass, Function<UserData, T> fallbackSelector) {
        var match = Aggregation.match(
                UserData.ownerCriteria()
        );
        var projection = Aggregation.project().and(field).as("result");

        var classAndGetter = CachedRuntimeWrapperClassGen.get(tClass);

        var result = mongoTemplate.aggregate(
                Aggregation.newAggregation(match, projection),
                UserData.class, classAndGetter.tClass()
        );

        var mapped = result.getUniqueMappedResult();
        if (mapped == null) {
            var account = Account.getAuthenticatedAccount();
            var userData = mongoTemplate.save(new UserData(account));
            return fallbackSelector.apply(userData);
        }

        return classAndGetter.get(mapped);
    }

    private <T> void setField(String field, T value, Function<T, UserData> fallback) {
        var q = Query.query(UserData.ownerCriteria());
        var exists = mongoTemplate.exists(q, UserData.class);
        if (!exists) {
            saveUserData(fallback.apply(value));
            return;
        }

        var upd = new Update().set(field, value);

        mongoTemplate.updateFirst(q, upd, UserData.class);
    }

    @Override
    public Locale getLocale() {
        return fetchField(UserData.Fields.targetLocale, Locale.class, UserData::targetLocale);
    }

    @Override
    public Locale setLocale(Locale locale) {
        setField(UserData.Fields.targetLocale, locale, UserData::new);
        return locale;
    }

    private final Map<Account, ReadBooks> readBooksCache = new ConcurrentHashMap<>();
    private final Map<Account, WaitList> waitListCache = new ConcurrentHashMap<>();

    @Override
    public ReadBooks readBooks() {
        var account = Account.getAuthenticatedAccount();
        return readBooksCache.computeIfAbsent(account, key -> new ReadBooksImpl(fetchField(UserData.Fields.readBooks, ReadBook[].class, UserData::readBooks)));
    }

    @Override
    public WaitList waitList() {
        var account = Account.getAuthenticatedAccount();
        return waitListCache.computeIfAbsent(account, key -> new WaitListImpl(fetchField(UserData.Fields.waitList, Book[].class, UserData::waitList)));
    }

    private class ReadBooksImpl implements ReadBooks {
        private AtomicReference<ReadBook[]> readBooksRef;

        private static Predicate<ReadBook> makePredicate(Book book) {
            return rb -> rb.book().compareById(book);
        }

        public ReadBooksImpl(ReadBook[] readBooks) {
            this.readBooksRef = new AtomicReference<>(readBooks);
        }

        @Override
        public ReadBook[] get() {
            return readBooksRef.get();
        }

        @Override
        public void setMark(Book book, ReadBook.Mark mark) {
            var readBooks = readBooksRef.get();

            Predicate<ReadBook> predicate = makePredicate(book);
            var exists = Utils.anyMatch(readBooks, predicate);
            if (exists) {
                if (Utils.anyMatch(readBooks, rb -> predicate.test(rb) && rb.mark() == mark))
                    return;

                readBooks = Utils.replace(readBooks, new ReadBook(book, mark), predicate);
            } else
                readBooks = ArrayUtils.add(readBooks, new ReadBook(book, mark));

            readBooksRef.set(readBooks);
            setField(UserData.Fields.readBooks, readBooks, UserData::new);
        }

        @Override
        public void unmark(Book book) {
            var readBooks = readBooksRef.get();

            Predicate<ReadBook> predicate = makePredicate(book);
            var exists = Utils.anyMatch(readBooks, predicate);
            if (!exists)
                return;
            readBooks = Utils.filter(readBooks, predicate.negate());
            readBooksRef.set(readBooks);
            setField(UserData.Fields.readBooks, readBooks, UserData::new);
        }

        @Override
        public boolean addRemove(Book book, ReadBook.Mark mark) {
            var readBooks = readBooksRef.get();
            Predicate<ReadBook> predicate = makePredicate(book);
            var exists = Utils.anyMatch(readBooks, predicate);
            if (exists) {
                readBooks = Utils.filter(readBooks, predicate.negate());
            } else {
                readBooks = ArrayUtils.add(readBooks, new ReadBook(book, mark));
            }
            readBooksRef.set(readBooks);
            setField(UserData.Fields.readBooks, readBooks, UserData::new);

            return !exists; // true if added
        }

        @Override
        public ReadBook.Mark getMark(Book book) {
            var readBooks = readBooksRef.get();
            for (ReadBook readBook : readBooks) {
                if (readBook.book().compareById(book))
                    return readBook.mark();
            }
            return null;
        }
    }

    private class WaitListImpl implements WaitList {
        private final AtomicReference<Book[]> waitListRef;

        private static Predicate<Book> makePredicate(Book book) {
            return bk -> bk.compareById(book);
        }

        private WaitListImpl(Book[] waitList) {
            this.waitListRef = new AtomicReference<>(waitList);
        }

        @Override
        public Book[] get() {
            return waitListRef.get();
        }

        @Override
        public boolean addRemove(Book book) {
            var waitListedBooks = waitListRef.get();
            var predicate = makePredicate(book);

            var exists = Utils.anyMatch(waitListedBooks, predicate);

            if (exists)
                waitListedBooks = Utils.filter(waitListedBooks, predicate.negate());
            else
                waitListedBooks = ArrayUtils.add(waitListedBooks, book);

            waitListRef.set(waitListedBooks);
            setField(UserData.Fields.waitList, waitListedBooks, UserData::new);
            return !exists;
        }

        @Override
        public boolean isInList(Book book) {
            var readBooks = waitListRef.get();
            var predicate = makePredicate(book);

            return Utils.anyMatch(readBooks, predicate);
        }
    }
}
