package com.mixfa.ailibrary.ui;

import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.BookBorrowing;
import com.mixfa.ailibrary.model.library.Comment;
import com.mixfa.ailibrary.model.library.ReadBook;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.service.library.BookBorrowingService;
import com.mixfa.ailibrary.service.library.CommentService;
import com.mixfa.ailibrary.service.misc.impl.Services;
import com.mixfa.ailibrary.service.user.UserDataService;
import com.mixfa.ailibrary.ui.components.GridWithPagination;
import com.mixfa.ailibrary.ui.components.SideBarInitializer;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizator;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.IntFunction;

@PermitAll
@Route("/user_details")
public class UserDetailsRoute extends AppLayout {
    private final CommentService commentService;
    private final UserDataService userDataService;
    private final BookBorrowingService borrowingService;
    private final Account account;
    private final Services services;
    private final Localizator localizator = LocalizationProvider.getLocalizator();

    private DateTimeFormatter dateTimeFormatter;

    public UserDetailsRoute(Services services) {
        this.userDataService = services.userDataService();
        this.commentService = services.commentService();
        this.borrowingService = services.bookBorrowingService();
        this.services = services;
        this.account = Account.getAuthenticatedAccount();
        SideBarInitializer.init(this, localizator);

        UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
            var timezone = details.getTimeZoneId();
            ZoneId zoneId;
            try {
                zoneId = ZoneId.of(timezone);
            } catch (DateTimeException e) {
                zoneId = ZoneId.systemDefault();
            }
            dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of(timezone));
            setContent(makeContent());
        });
    }

    private Component makeMyOrders() {
        var layout = new VerticalLayout();
        IntFunction<Page<BookBorrowing>> fetchFunc = page -> borrowingService.findAllMyBorrowings(PageRequest.of(page, 15));

        var takenBooksGrid = new GridWithPagination<BookBorrowing>(BookBorrowing.class, 15, fetchFunc);
        VaadinCommons.configureDefaultBookGridEx(takenBooksGrid, BookBorrowing::book);
        VaadinCommons.configureBookGridPreviewEx(takenBooksGrid, BookBorrowing::book, services, localizator);

        takenBooksGrid.addComponentColumn(it -> new Button(localizator.get("userdetails.readbook"),
                        _ -> UI.getCurrent().navigate(BookContentRoute.class, it.book().id().toHexString())))
                .setHeader(localizator.get("userdetails.readbook"));
        takenBooksGrid.addColumn(it -> dateTimeFormatter.format(it.borrowedTime())).setHeader(localizator.get("userdetails.borrowedtime"));
        takenBooksGrid.addColumn(it -> dateTimeFormatter.format(it.returnTime())).setHeader(localizator.get("userdetails.returntime"));
        takenBooksGrid.refresh();

        layout.add(new Div(new H3(localizator.get("userdetails.yourorderedbooks"))), takenBooksGrid);
        return VaadinCommons.applyMainStyle(new Div(layout));
    }

    private Component makeWaitList() {
        var waitList = userDataService.waitList();

        var waitListGrid = new Grid<>(Book.class, false);
        VaadinCommons.configureDefaultBookGrid(waitListGrid);
        VaadinCommons.configureBookGridPreview(waitListGrid, services, localizator);

        waitListGrid.addComponentColumn(book -> new Button(localizator.get("userdetails.remove"), _ ->
        {
            waitList.addRemove(book);
            waitListGrid.setItems(waitList.get());
        })).setHeader(localizator.get("userdetails.remove"));

        waitListGrid.setItems(waitList.get());

        return waitListGrid;
    }

    private Component makeReadList() {
        var readList = userDataService.readBooks();

        var grid = new Grid<>(ReadBook.class, false);
        VaadinCommons.configureDefaultBookGridEx(grid, ReadBook::book);
        grid.addComponentColumn(rb -> new Button((rb.mark() == ReadBook.Mark.LIKE ? VaadinIcon.THUMBS_UP : VaadinIcon.THUMBS_DOWN).create()))
                .setHeader(localizator.get("userdetails.yourmark"));
        VaadinCommons.configureBookGridPreviewEx(grid, ReadBook::book, services, localizator);
        grid.addComponentColumn(rb -> new Button(localizator.get("userdetails.remove"), _ -> {
            readList.addRemove(rb.book(), null);
            grid.setItems(readList.get());
        })).setHeader(localizator.get("userdetails.remove"));
        grid.setItems(readList.get());
        return grid;
    }

    private Component makeCommentsSection() {
        IntFunction<Page<Comment>> fetchFunc = page -> commentService.listMyComments(PageRequest.of(page, 10));
        var commentsGrid = new GridWithPagination<>(Comment.class, 10, fetchFunc);
        commentsGrid.addColumn(Comment::text).setHeader(localizator.get("userdetails.text"));
        commentsGrid.addColumn(comment -> comment.book().title()).setHeader(localizator.get("userdetails.book"));
        VaadinCommons.configureBookGridPreviewEx(commentsGrid, Comment::book, services, localizator);
        commentsGrid.addComponentColumn(comment -> new Button(localizator.get("userdetails.delete"), _ -> {
            commentService.removeComment(comment.id());
            commentsGrid.refresh();
        }));

        commentsGrid.refresh();
        return commentsGrid;
    }

    private Component makeProfileSection() {
        return new HorizontalLayout(
                new Span(localizator.formatGet("userdetails.username", account.getUsername())),
                new Span(localizator.formatGet("userdetails.email", account.getEmail())),
                new Span(localizator.formatGet("userdetails.role", account.getRole().name().toLowerCase())),
                new ComboBox<Locale>(localizator.get("userdetails.localecombobox")) {{
                    setItems(Locale.ENGLISH, Locale.forLanguageTag("UA"));
                    setValue(localizator.locale());
                    addValueChangeListener(e -> {
                        userDataService.setLocale(e.getValue());
                        UI.getCurrent().navigate(UserDetailsRoute.class);
                    });
                }}
        ) {{
            setAlignItems(Alignment.BASELINE);
        }};
    }

    private Component makeContent() {
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        accordion.add(localizator.get("userdetails.profile"), makeProfileSection());
        accordion.add(localizator.get("userdetails.myorders"), makeMyOrders());
        accordion.add(localizator.get("userdetails.waitlist"), makeWaitList());
        accordion.add(localizator.get("userdetails.readlist"), makeReadList());
        accordion.add(localizator.get("userdetails.mycomments"), makeCommentsSection());

        return VaadinCommons.applyMainStyle(accordion);
    }
}
