package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.Services;
import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookStatus;
import com.mixfa.ailibrary.model.ReadBook;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.CommentService;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.UserDataService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.Locale;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@PermitAll
@Route("/user_details")
public class UserDetailsRoute extends AppLayout {
    private final Locale userLocale;
    private final Services services;

    private final LibraryService libService;
    private final CommentService commentService;
    private final UserDataService userDataService;

    public UserDetailsRoute(Services services) {
        this.services = services;
        this.userDataService = services.userDataService();
        this.commentService = services.commentService();
        this.userLocale = userDataService.getLocale();
        this.libService = services.libService();
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

        var disabledButton = new Button("Cancel") {{
            setEnabled(false);
        }};
        takenBooksGrid.addComponentColumn(bks -> {
            if (bks.status() != BookStatus.Status.BOOKED)
                return disabledButton;
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

        var grid = new Grid<>(Book.class, false);
        VaadinCommons.configureDefaultBookGrid(grid, userLocale);
        VaadinCommons.configureBookGridPreview(grid, userLocale, commentService, userDataService);

        grid.addComponentColumn(book -> new Button("Remove", _ ->
        {
            waitList.addRemove(book);
            grid.setItems(waitList.get());
        }));

        grid.setItems(waitList.get());

        return grid;
    }

    private Component makeReadList() {
        var readList = userDataService.readBooks();

        var grid = new Grid<>(ReadBook.class, false);
        VaadinCommons.configureDefaultBookGridEx(grid, ReadBook::book, userLocale);
        VaadinCommons.configureBookGridPreviewEx(grid, ReadBook::book, userLocale, commentService, userDataService);
        grid.addComponentColumn(rb -> new Button("Remove", _ -> {
            readList.addRemove(rb.book(), null);
            grid.setItems(readList.get());
        }));
        grid.setItems(readList.get());
        return grid;
    }

    private Component makeContent() {
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        accordion.add("My Orders", makeMyOrders());
        accordion.add("Wait List", makeWaitList());
        accordion.add("Read List", makeReadList());

        return VaadinCommons.applyMainStyle(accordion);
    }
}
