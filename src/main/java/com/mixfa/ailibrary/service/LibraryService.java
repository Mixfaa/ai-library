package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.BookStatus;
import com.mixfa.ailibrary.model.Library;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LibraryService {
    Library registerOfflineLib(Library.AddRequest request);

    void editOfflineLib(String libname, Library library);

    Library.BookAvailability[] getBooksAvailability(String libname);

    BookStatus tryOrderBook(String libname, Object bookId);

    void cancelBookOrder(Object bookStatusId);

    BookStatus updateBookStatusData(Object bookStatusId, BookStatus.Status status);

    @Deprecated
    List<BookStatus> findAllMyTakenBooks();

    Page<BookStatus> findAllTakenBooks(String libraryId, Pageable pageable);

    Page<BookStatus> findTakenBooks(String query, String libId, Pageable pageable);

    Library findOrThrow(String libname);

    Library.BookAvailability[] setBookAvailability(String libname, Object bookId, int amount);

    void deleteOfflineLib(String libname);
}
