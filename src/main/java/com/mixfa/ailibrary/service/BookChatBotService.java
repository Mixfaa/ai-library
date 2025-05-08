package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Book;
import reactor.core.publisher.Flux;

public interface BookChatBotService {
    BookChatBot createBookChatBot(Book book);

    interface BookChatBot {
        String talk(String message);
    }
}
