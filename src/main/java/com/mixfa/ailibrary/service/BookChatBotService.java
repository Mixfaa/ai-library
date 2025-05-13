package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Book;

public interface BookChatBotService {
    ChatBot createBookChatBot(Book book);

    interface ChatBot {
        String talk(String message);
    }
}
