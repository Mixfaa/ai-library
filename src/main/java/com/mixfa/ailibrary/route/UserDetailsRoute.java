package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookBorrowing;
import com.mixfa.ailibrary.model.Comment;
import com.mixfa.ailibrary.model.ReadBook;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.route.components.GridWithPagination;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.BookBorrowingService;
import com.mixfa.ailibrary.service.CommentService;
import com.mixfa.ailibrary.service.UserDataService;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.lang3.function.Functions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.IntFunction;

@PermitAll
@Route("/user_details")
public class UserDetailsRoute extends AppLayout {
    private final Locale userLocale;

    private final CommentService commentService;
    private final UserDataService userDataService;
    private final BookBorrowingService borrowingService;
    private final Account account;
    private final Services services;

    private DateTimeFormatter dateTimeFormatter;

    public UserDetailsRoute(Services services) {
        this.userDataService = services.userDataService();
        this.commentService = services.commentService();
        this.borrowingService = services.bookBorrowingService();
        this.userLocale = userDataService.getLocale();
        this.services = services;
        this.account = Account.getAuthenticatedAccount();
        SideBarInitializer.init(this);

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
        VaadinCommons.configureBookGridPreviewEx(takenBooksGrid, BookBorrowing::book, services);

        takenBooksGrid.addColumn(it -> dateTimeFormatter.format(it.borrowedTime())).setHeader("Borrowed Time");
        takenBooksGrid.addColumn(it -> dateTimeFormatter.format(it.returnTime())).setHeader("Return Time");
        takenBooksGrid.refresh();

        layout.add(new Div(new H3("Your ordered books")), takenBooksGrid);
        return VaadinCommons.applyMainStyle(new Div(layout));
    }

    private Component makeWaitList() {
        var waitList = userDataService.waitList();

        var waitListGrid = new Grid<>(Book.class, false);
        VaadinCommons.configureDefaultBookGrid(waitListGrid);
        VaadinCommons.configureBookGridPreview(waitListGrid, services);

        waitListGrid.addComponentColumn(book -> new Button("Remove", _ ->
        {
            waitList.addRemove(book);
            waitListGrid.setItems(waitList.get());
        })).setHeader("Remove");

        waitListGrid.setItems(waitList.get());

        return waitListGrid;
    }

    private Component makeReadList() {
        var readList = userDataService.readBooks();

        var grid = new Grid<>(ReadBook.class, false);
        VaadinCommons.configureDefaultBookGridEx(grid, ReadBook::book);
        grid.addComponentColumn(rb -> new Button((rb.mark() == ReadBook.Mark.LIKE ? VaadinIcon.THUMBS_UP : VaadinIcon.THUMBS_DOWN).create()))
                .setHeader("Your Mark");
        VaadinCommons.configureBookGridPreviewEx(grid, ReadBook::book, services);
        grid.addComponentColumn(rb -> new Button("Remove", _ -> {
            readList.addRemove(rb.book(), null);
            grid.setItems(readList.get());
        })).setHeader("Remove");
        grid.setItems(readList.get());
        return grid;
    }

    private Component makeCommentsSection() {
        IntFunction<Page<Comment>> fetchFunc = page -> commentService.listMyComments(PageRequest.of(page, 10));
        var commentsGrid = new GridWithPagination<>(Comment.class, 10, fetchFunc);
        commentsGrid.addColumn(Comment::text).setHeader("Text");
        commentsGrid.addColumn(comment -> comment.book().title()).setHeader("Book");
        VaadinCommons.configureBookGridPreviewEx(commentsGrid, Comment::book, services);
        commentsGrid.addComponentColumn(comment -> new Button("Delete", _ -> {
            commentService.removeComment(comment.id());
            commentsGrid.refresh();
        }));

        commentsGrid.refresh();
        return commentsGrid;
    }

    private Component makeProfileSection() {
        return new HorizontalLayout(
                new Span("Username: " + account.getUsername()),
                new Span("Email: " + account.getEmail()),
                new Span("Role: " + account.getRole().name().toLowerCase())
        );
    }

    private Component makeContent() {
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        accordion.add("Profile", makeProfileSection());
        accordion.add("My Orders", makeMyOrders());
        accordion.add("Wait List", makeWaitList());
        accordion.add("Read List", makeReadList());
        accordion.add("My Comments", makeCommentsSection());

        return VaadinCommons.applyMainStyle(accordion);
    }
}
