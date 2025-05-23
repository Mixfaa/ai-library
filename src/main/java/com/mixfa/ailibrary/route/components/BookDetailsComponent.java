package com.mixfa.ailibrary.route.components;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.ReadBook;
import com.mixfa.ailibrary.route.BookContentRoute;
import com.mixfa.ailibrary.service.BookBorrowingService;
import com.mixfa.ailibrary.service.BookChatBotService;
import com.mixfa.ailibrary.service.UserDataService;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.function.Consumer;

public class BookDetailsComponent extends VerticalLayout {
    private final Book book;
    private final Locale userLocale;
    private final UserDataService userDataService;
    private final double rating;
    private final UserDataService.WaitList waitList;
    private final UserDataService.ReadBooks readBooks;
    private final BookChatBotService bookChatBotService;

    private final BookBorrowingService bookBorrowingService;

    public BookDetailsComponent(Book book, Services services) {
        this.book = book;
        this.bookChatBotService = services.bookChatBotService();
        this.rating = services.commentService().getBookRate(book.id());
        this.userDataService = services.userDataService();
        this.bookBorrowingService = services.bookBorrowingService();
        this.userLocale = userDataService.getLocale();
        this.waitList = userDataService.waitList();
        this.readBooks = userDataService.readBooks();

        setSpacing(false);
        setPadding(true);
        VaadinCommons.applyMainStyle(this);

        add(createHeaderSection(), createDescriptionSection());
    }

    private Component createHeaderSection() {
        HorizontalLayout headerSection = new HorizontalLayout();
        headerSection.setWidthFull();
        headerSection.setSpacing(true);
        headerSection.setPadding(true);

        // Book cover image
        Image coverImage = new Image();
        coverImage.setSrc(book.imageUrl());
        coverImage.setWidth("200px");
        coverImage.setHeight("300px");
        coverImage.getStyle()
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
                .set("object-fit", "cover");

        // Title and metadata container
        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setPadding(false);
        titleSection.setSpacing(true);

        titleSection.add(
                createTitleWithButtons(),
                createAuthorSection(),
                createGenreSection(),
                createPublishYearSection(),
                createIsbnSection(),
                createRatingSection(),
                createStatsSection()
        );

        headerSection.add(coverImage, titleSection);
        return headerSection;
    }

    private Component createTitleWithButtons() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setSpacing(true);
        header.setPadding(false);

        H1 bookTitle = new H1(book.title());
        bookTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "2.5rem")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "600");

        header.add(
                bookTitle,
                createWaitListButton(),
                createReadBookButton(),
                createTalkToButton(),
                createBorrowBookButton()
        );

        return header;
    }

    private Button createBorrowBookButton() {
        return new Button(VaadinIcon.MONEY.create(), e -> {
            try {
                var invoice = bookBorrowingService.borrowBook(book.id());
                var paymentUrl = invoice.pageUrl();
                var dialog = new Dialog("Payment url");
                dialog.add(new Anchor(paymentUrl, "Pay") {{
                    setRouterIgnore(true);
                }});
                dialog.open();
            } catch (UserFriendlyException ex) {
                if (ex.getType() == ExceptionType.BOOK_ALREADY_BORROWED)
                    UI.getCurrent().navigate(BookContentRoute.class, book.id().toHexString());
            }

            return;
        });
    }

    private Button createWaitListButton() {
        Button waitListBtn = new Button(waitList.isInList(book) ? VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create());
        waitListBtn.addClickListener(_ -> {
            var added = waitList.addRemove(book);
            waitListBtn.setIcon(added ? VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create());
        });
        return waitListBtn;
    }

    private Component createReadBookButton() {
        var bookMark = readBooks.getMark(book);
        var icon = (bookMark == null) ? VaadinIcon.OPEN_BOOK.create() :
                (bookMark == ReadBook.Mark.LIKE) ? VaadinIcon.THUMBS_UP.create() : VaadinIcon.THUMBS_DOWN.create();
        Button readBookBtn = new Button(icon);
        Popover readBookPopover = createReadBookPopover(readBookBtn);

        readBookBtn.addClickListener(_ -> {
            if (readBooks.getMark(book) != null) {
                readBooks.addRemove(book, null);
                readBookBtn.setIcon(VaadinIcon.OPEN_BOOK.create());
            } else {
                readBookPopover.open();
            }
        });

        return readBookBtn;
    }

    private Popover createReadBookPopover(Button target) {
        Popover readBookPopover = new Popover();
        readBookPopover.setTarget(target);
        readBookPopover.setOpenOnClick(false);
        readBookPopover.setPosition(PopoverPosition.BOTTOM);

        Consumer<ReadBook.Mark> setBookMark = (mark) -> {
            readBooks.addRemove(book, mark);
            target.setIcon(mark == ReadBook.Mark.LIKE ? VaadinIcon.THUMBS_UP.create() : VaadinIcon.THUMBS_DOWN.create());
            readBookPopover.close();
        };

        var likedBtn = new Button("I liked", VaadinIcon.THUMBS_UP.create(), _ -> {
            setBookMark.accept(ReadBook.Mark.LIKE);
        });
        var dislikeBtn = new Button("I disliked", VaadinIcon.THUMBS_DOWN.create(), _ -> {
            setBookMark.accept(ReadBook.Mark.DISLIKE);
        });

        readBookPopover.setWidth("300px");
        readBookPopover.addThemeVariants(PopoverVariant.ARROW, PopoverVariant.LUMO_NO_PADDING);
        readBookPopover.setModal(true);
        readBookPopover.setAriaLabelledBy("notifications-heading");
        readBookPopover.add(new HorizontalLayout(likedBtn, dislikeBtn));

        return readBookPopover;
    }

    private Button createTalkToButton() {
        var aiChatBotComp = new AiChatBotDialog(book, bookChatBotService);

        return new Button(VaadinIcon.MAGIC.create(), _ -> aiChatBotComp.open()) {{
            setTooltipText("Chat with chat bot about this book");
        }};
    }

    private Component createAuthorSection() {
        var authors = String.join(", ", book.authors());
        Icon authorIcon = VaadinIcon.USER.create();
        authorIcon.setColor("var(--lumo-primary-color)");
        HorizontalLayout authorsLayout = new HorizontalLayout(authorIcon, new Span(authors));
        authorsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return authorsLayout;
    }

    private Component createGenreSection() {
        var subjects = book.subjectsString();
        if (subjects.isBlank()) return new Div();

        Icon genreIcon = VaadinIcon.TAGS.create();
        genreIcon.setColor("var(--lumo-primary-color)");
        HorizontalLayout genresLayout = new HorizontalLayout(genreIcon, new Span(subjects));
        genresLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return genresLayout;
    }

    private Component createIsbnSection() {
        Icon icon = VaadinIcon.BARCODE.create();
        icon.setColor("var(--lumo-primary-color)");
        HorizontalLayout genresLayout = new HorizontalLayout(icon, new Span("ISBN: " + book.isbn()));
        genresLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return genresLayout;
    }

    private Component createPublishYearSection() {
        Icon icon = VaadinIcon.DATE_INPUT.create();
        icon.setColor("var(--lumo-primary-color)");
        HorizontalLayout genresLayout = new HorizontalLayout(icon, new Span("Publish year: " + book.firstPublishYear()));
        genresLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return genresLayout;
    }

    private Component createRatingSection() {
        HorizontalLayout ratingLayout = new HorizontalLayout();
        ratingLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon starIcon = VaadinIcon.STAR.create();
        starIcon.setColor("var(--lumo-primary-color)");
        Span ratingValue = new Span(String.format("%.1f", rating));
        ratingValue.getStyle()
                .set("font-size", "1.2rem")
                .set("font-weight", "bold")
                .set("margin-left", "4px");

        ratingLayout.add(starIcon, ratingValue);
        return ratingLayout;
    }

    private Component createStatsSection() {
        HorizontalLayout statsLayout = new HorizontalLayout();
        Icon takeIcon = VaadinIcon.BOOK.create();
        Icon readIcon = VaadinIcon.CHECK.create();
        Span takeSpan = new Span(String.format("Taken: %d", book.tookCount()));
        Span readSpan = new Span(String.format("Read: %d", book.readCount()));
        statsLayout.add(takeIcon, takeSpan, readIcon, readSpan);
        statsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return statsLayout;
    }

    private Component createDescriptionSection() {
        var description = book.description();
        if (StringUtils.isBlank(description)) description = "No description yet";

        Div descriptionSection = new Div();
        descriptionSection.setWidthFull();
        descriptionSection.getStyle()
                .set("margin-top", "24px")
                .set("padding", "16px")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "8px");

        H3 descriptionTitle = new H3("Description");
        descriptionTitle.getStyle().set("margin-top", "0");

        Paragraph bookDescription = new Paragraph(description);
        bookDescription.getStyle()
                .set("font-size", "1.1rem")
                .set("line-height", "1.6")
                .set("margin", "0");

        descriptionSection.add(descriptionTitle, bookDescription);
        return descriptionSection;
    }
}