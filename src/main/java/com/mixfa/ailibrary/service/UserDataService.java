package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.misc.ReadWriteLockVisitorAdapter;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.ReadBook;
import com.mixfa.ailibrary.model.user.UserData;

import java.util.Locale;
import java.util.function.Predicate;

public interface UserDataService {
    UserData getUserData();

    Locale getLocale();

    Locale setLocale(Locale locale);

    ReadBooks readBooks();

    WaitList waitList();

    interface WaitList extends ReadWriteLockVisitorAdapter<WaitList> {
        Book[] get();

        boolean addRemove(Book book);

        boolean isInList(Book book);

        boolean isInList(Predicate<Book> predicate);
    }

    interface ReadBooks extends ReadWriteLockVisitorAdapter<ReadBooks> {
        ReadBook[] get();

        void setMark(Book book, ReadBook.Mark mark);

        void unmark(Book book);

        // true if added
        boolean addRemove(Book book, ReadBook.Mark mark);

        ReadBook.Mark getMark(Book book);
    }
}
