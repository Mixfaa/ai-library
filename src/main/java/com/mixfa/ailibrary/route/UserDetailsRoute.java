package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookStatus;
import com.mixfa.ailibrary.model.Comment;
import com.mixfa.ailibrary.model.ReadBook;
import com.mixfa.ailibrary.route.components.GridWithPagination;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.CommentService;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.UserDataService;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Locale;
import java.util.function.IntFunction;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@PermitAll
@Route("/user_details")
public class UserDetailsRoute extends AppLayout {
    private final Locale userLocale;

    private final LibraryService libService;
    private final CommentService commentService;
    private final UserDataService userDataService;
    private final Services services;

    public UserDetailsRoute(Services services) {
        this.userDataService = services.userDataService();
        this.commentService = services.commentService();
        this.userLocale = userDataService.getLocale();
        this.libService = services.libService();
        this.services = services;
        SideBarInitializer.init(this);

        setContent(makeContent());
    }

    private Component makeMyOrders() {
        var takenBooks = libService.findAllMyTakenBooks();

        var layout = new VerticalLayout();
        var takenBooksGrid = new Grid<BookStatus>(BookStatus.class, false);

        takenBooksGrid.addColumn(bks -> fmt("{0} ({1})", bks.book().titleString(userLocale), bks.locale().getDisplayName())).setHeader("Book");
        takenBooksGrid.addColumn(bks -> bks.library().name()).setHeader("Library");
        takenBooksGrid.addColumn(BookStatus::tookDate).setHeader("Took Date");
        takenBooksGrid.addColumn(BookStatus::returnDate).setHeader("Return Date");
        takenBooksGrid.addColumn(BookStatus::status).setHeader("Status");
        takenBooksGrid.addComponentColumn(bks -> {
            if (bks.status() != BookStatus.Status.BOOKED)
                return null;
            else
                return new Button("Cancel", _ -> {
                    try {
                        libService.cancelBookOrder(bks.id());
                        Notification.show("Book order cancelled");

                        takenBooksGrid.setItems(libService.findAllMyTakenBooks());
                    } catch (UserFriendlyException ex) {
                        Notification.show(ex.format(userLocale));
                    }
                });
        });

        takenBooksGrid.setItems(takenBooks);

        layout.add(new Div(new H3("Your ordered books")), takenBooksGrid);
        return VaadinCommons.applyMainStyle(new Div(layout));
    }

    private Component makeWaitList() {
        var waitList = userDataService.waitList();

        var waitListGrid = new Grid<>(Book.class, false);
        VaadinCommons.configureDefaultBookGrid(waitListGrid, userLocale);
        VaadinCommons.configureBookGridPreview(waitListGrid, userLocale, services);

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
        VaadinCommons.configureDefaultBookGridEx(grid, ReadBook::book, userLocale);
        grid.addComponentColumn(rb -> new Button((rb.mark() == ReadBook.Mark.LIKE ? VaadinIcon.THUMBS_UP : VaadinIcon.THUMBS_DOWN).create()))
                .setHeader("Your Mark");
        VaadinCommons.configureBookGridPreviewEx(grid, ReadBook::book, userLocale, services);
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
        commentsGrid.addColumn(comment -> comment.book().titleString(userLocale)).setHeader("Book");
        VaadinCommons.configureBookGridPreviewEx(commentsGrid, Comment::book, userLocale, services);
        commentsGrid.addComponentColumn(comment -> new Button("Delete", _ -> {
            commentService.removeComment(comment.id());
            commentsGrid.refresh();
        }));

        commentsGrid.refresh();
        return commentsGrid;
    }

    private Component makeContent() {
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        accordion.add("My Orders", makeMyOrders());
        accordion.add("Wait List", makeWaitList());
        accordion.add("Read List", makeReadList());
        accordion.add("My Comments", makeCommentsSection());

        return VaadinCommons.applyMainStyle(accordion);
    }
}
