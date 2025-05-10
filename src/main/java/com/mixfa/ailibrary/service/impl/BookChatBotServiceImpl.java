package com.mixfa.ailibrary.service.impl;

import com.google.common.collect.Collections2;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.service.AiFunctions;
import com.mixfa.ailibrary.service.BookChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
@Service
public class BookChatBotServiceImpl implements BookChatBotService {
    private final ChatModel chatModel;
    private final AiFunctions aiFunctions;

    @Override
    public BookChatBot createBookChatBot(Book book) {
        return new BookChatBotImpl(book);
    }

    private static SystemMessage makeSystemMessage(Book book) {
        return new SystemMessage(String.join("\n",
                "Act as library assistent",
                "Answer in user`s language",
                "In context of book:",
                Utils.makeBookDescription(book)));
    }

    private class BookChatBotImpl implements BookChatBot {
        private final OpenAiChatOptions chatOptions;
        private final List<Message> chatHistory = Collections.synchronizedList(new ArrayList<>());

        BookChatBotImpl(Book book) {
            this.chatOptions = OpenAiChatOptions.builder().toolCallbacks(aiFunctions.searchFunction(), aiFunctions.usersReadBooks()).build();
            chatHistory.addFirst(makeSystemMessage(book));
        }

        @Override
        public String talk(String message) {
            chatHistory.addLast(new UserMessage(message));
            var response = chatModel.call(new Prompt(chatHistory, chatOptions));
            var output = response.getResult().getOutput();
            chatHistory.addLast(output);

            return output.getText();
        }
    }
}
