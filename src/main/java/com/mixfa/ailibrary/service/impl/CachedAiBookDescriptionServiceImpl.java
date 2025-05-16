package com.mixfa.ailibrary.service.impl;

import com.google.common.collect.Streams;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.ReadBook;
import com.mixfa.ailibrary.service.AiBookDescriptionService;
import com.mixfa.ailibrary.service.BookService;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CachedAiBookDescriptionServiceImpl implements AiBookDescriptionService {
    private final ValueOperations<String, String> valueOps;
    private final RedisTemplate<String, String> redisTemplate;

    public CachedAiBookDescriptionServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String bookDescription(Book book) {
        var bookKey = makeBookDescriptionKey(book);

        var description = valueOps.get(bookKey);
        if (description == null) {
            description = Utils.makeBookDescription(book);
            valueOps.set(bookKey, description);
        }

        return description;
    }

    @Override
    public String bookDescriptionAndMark(ReadBook readBook) {
        var bookKey = makeBookDescriptionKey(readBook.book());

        var description = valueOps.get(bookKey);
        if (description == null) {
            description = Utils.makeBookDescription(readBook.book());
            valueOps.set(bookKey, description);
        }

        var sb = new StringBuilder();
        sb.append(description).append('\n');
        sb.append("Mark = ").append(readBook.mark()).append("\n");
        return sb.toString();
    }

    @Override
    public List<String> bookDescriptionList(List<Book> books) {
        var descriptions = valueOps.multiGet(books.stream().map(CachedAiBookDescriptionServiceImpl::makeBookDescriptionKey).toList());

        if (books.size() != descriptions.size())
            throw new RuntimeException("Book description count mismatch (valueOps multiGet)");

        var toSetValues = new HashMap<String, String>();
        for (int i = 0; i < books.size(); i++) {
            var book = books.get(i);
            var description = descriptions.get(i);

            if (description == null) {
                description = Utils.makeBookDescription(book);
                toSetValues.put(makeBookDescriptionKey(book), description);
            }
        }

        if (!toSetValues.isEmpty())
            valueOps.multiSet(toSetValues);

        return descriptions;
    }

    @Override
    public List<String> bookDescriptionAndMarkList(List<ReadBook> readBooks) {
        var descriptions = bookDescriptionList(readBooks.stream().map(ReadBook::book).toList());

        var descriptionsWithMarks = new ArrayList<String>(readBooks.size());

        Streams.forEachPair(descriptions.stream(), readBooks.stream(), (desc, readBook) -> {
            var sb = new StringBuilder();
            sb.append(desc).append('\n');
            sb.append("Mark = ").append(readBook.mark()).append("\n");
            descriptionsWithMarks.addLast(sb.toString());
        });

        return descriptionsWithMarks;
    }

    @Override
    public void evictCache(Object bookId) {
        valueOps.getAndDelete(makeBookDescriptionKey(Utils.idToStr(bookId)));
    }

    @Override
    public void evictCache(List<Object> booksIds) {
        var keys = booksIds.stream().map(id -> makeBookDescriptionKey(Utils.idToStr(id))).toList();
        redisTemplate.delete(keys);
    }

    public static String makeBookDescriptionKey(Book book) {
        return makeBookDescriptionKey(book.id().toHexString());
    }

    public static String makeBookDescriptionKey(String id) {
        return id + ":book-desc";
    }

    @EventListener(BookService.Event.class)
    public void onEvent(BookService.Event event) {
        System.out.println(event);
        System.out.println(event.getClass());
        switch (event) {
            case BookService.Event.OnBookAdded onAdded -> evictCache(onAdded.book().id());
            case BookService.Event.OnBookEdited onEdited -> evictCache(onEdited.book().id());
            case BookService.Event.OnBookDeleted onRemoved -> evictCache(onRemoved.bookId());
        }
    }
}
