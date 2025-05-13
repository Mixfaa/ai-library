package com.mixfa.ailibrary.route.comp;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.service.BookChatBotService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AiChatBotDialog extends Dialog {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private final BookChatBotService.ChatBot bookChatBot;

    public AiChatBotDialog(Book book, BookChatBotService bookChatBotService) {
        super();
        getFooter().add(new CloseDialogButton(this));
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
