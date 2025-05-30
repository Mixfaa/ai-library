package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.service.library.BookChatBotService;
import com.mixfa.ailibrary.ui.localization.Localizator;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.mixfa.ailibrary.misc.Utils.fmt;

public class AiChatBotDialog extends Dialog {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private final BookChatBotService.ChatBot bookChatBot;

    public AiChatBotDialog(Book book, Localizator localizator, BookChatBotService bookChatBotService) {
        super();
        setHeaderTitle(fmt(localizator.get("aichatbot.dialog.title"), book.title()));
        getFooter().add(new CloseDialogButton(this, localizator));
        this.bookChatBot = bookChatBotService.createBookChatBot(book);

        var messages = new CopyOnWriteArrayList<MessageListItem>();

        var messageList = new MessageList();
        messageList.setWidth("1000px");
        messageList.setHeight("600px");
        var messageInput = new MessageInput(e -> {
            messages.addLast(new MessageListItem(e.getValue()));
            messageList.setItems(messages);
            executor.execute(() -> {
                messageList.getUI().ifPresent(ui -> ui.access(() -> {
                    var resp = bookChatBot.talk(e.getValue());

                    messages.addLast(new MessageListItem(resp));

                    messageList.setItems(messages);
                }));
            });
        });
        messageInput.setWidth("500px");
        messageList.setItems(messages);

        this.add(new VerticalLayout(messageList, messageInput));
    }
}
