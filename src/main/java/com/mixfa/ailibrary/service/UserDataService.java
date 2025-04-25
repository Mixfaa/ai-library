package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.ReadBook;
import com.mixfa.ailibrary.model.user.UserData;

import java.util.Locale;

public interface UserDataService {
    UserData getUserData();

    Locale getLocale();

    Locale setLocale(Locale locale);

    ReadBooks readBooks();

    WaitList waitList();

    interface WaitList {
        Book[] get();

        boolean addRemove(Book book);

        boolean isInList(Book book);
    }

    interface ReadBooks {
        ReadBook[] get();

        void setMark(Book book, ReadBook.Mark mark);

        void unmark(Book book);

        // true if added
        boolean addRemove(Book book, ReadBook.Mark mark);

        ReadBook.Mark getMark(Book book);
    }
}
