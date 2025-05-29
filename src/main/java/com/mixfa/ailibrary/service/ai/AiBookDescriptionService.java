package com.mixfa.ailibrary.service.ai;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.ReadBook;

import java.util.List;

public interface AiBookDescriptionService {
    String bookDescription(Book book);

    String bookDescriptionAndMark(ReadBook readBook);

    List<String> bookDescriptionList(List<Book> books);

    List<String> bookDescriptionAndMarkList(List<ReadBook> readBooks);

    void evictCache(Object bookId);

    void evictCache(List<Object> booksIds);
}
