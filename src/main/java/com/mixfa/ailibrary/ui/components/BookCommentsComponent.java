package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.Comment;
import com.mixfa.ailibrary.service.library.CommentService;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class BookCommentsComponent extends VerticalLayout {
    private final CommentService commentService;
    private final Book book;
    private final MessageList messageList;
    private final ArrayList<MessageListItem> commentsItems;
    private final Localizer localizer;

    public BookCommentsComponent(Book book, Localizer localizer, CommentService commentService) {
        this.book = book;
        this.localizer = localizer;
        this.commentService = commentService;
        this.messageList = new MessageList();

        var comments = commentService.listComments(book.id(), Pageable.ofSize(15));
        commentsItems = comments.getContent()
                .stream()
                .map(BookCommentsComponent::commentToItem)
                .collect(Collectors.toCollection(ArrayList::new));

        messageList.setItems(commentsItems);

        add(
                messageList,
                createCommentInputSection()
        );

        setWidthFull();
        getStyle()
                .set("max-width", "1500px")
                .set("margin", "0 auto")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "12px")
                .set("box-shadow", "0 8px 24px rgba(0, 0, 0, 0.12)")
                .set("padding", "24px");
    }

    private static MessageListItem commentToItem(Comment comment) {
        return new MessageListItem(
                Utils.fmt("{0} ({1})", comment.text(), comment.rate()),
                comment.timestamp(),
                comment.owner().getUsername()
        );
    }

    private Component createCommentInputSection() {
        var messageInput = new MessageInput();
        var rateInput = new IntegerField(localizer.get("comment.rate.label")) {{
            setWidth("100px");
            setValue(5);
            setMin(0);
            setMax(5);
            setStepButtonsVisible(true);
        }};

        messageInput.addSubmitListener(event -> {
            var text = event.getValue();
            var rate = rateInput.getValue();
            try {
                var comment = commentService.addComment(new Comment.AddRequest(book.id().toHexString(), text, rate));
                commentsItems.addLast(commentToItem(comment));
                messageList.setItems(commentsItems);
            } catch (Exception e) {
                Notification.show(e.getLocalizedMessage());
            }
        });

        return new HorizontalLayout(
                rateInput,
                messageInput
        ) {{
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        }};
    }
}