package com.mixfa.ailibrary.service.library;

import com.mixfa.ailibrary.model.library.Book;

public interface BookChatBotService {
    ChatBot createBookChatBot(Book book);

    interface ChatBot {
        String talk(String message);
    }
}
