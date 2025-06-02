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
import com.mixfa.ailibrary.service.user.AccountService;
import com.mixfa.ailibrary.service.user.UserDataService;
import com.mixfa.ailibrary.ui.components.GridWithPagination;
import com.mixfa.ailibrary.ui.components.SideBarInitializer;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;

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
    private final Localizer localizer = LocalizationProvider.getLocalizator();
    private final AccountService accountService;

    private DateTimeFormatter dateTimeFormatter;

    public UserDetailsRoute(Services services, AccountService accountService) {
        this.userDataService = services.userDataService();
        this.commentService = services.commentService();
        this.borrowingService = services.bookBorrowingService();
        this.services = services;
        this.account = Account.getAuthenticatedAccount();
        SideBarInitializer.init(this, localizer);

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
        this.accountService = accountService;
    }

    private Component makeMyOrders() {
        IntFunction<Page<BookBorrowing>> fetchFunc = page -> borrowingService.findAllMyBorrowings(PageRequest.of(page, 15));

        var takenBooksGrid = new GridWithPagination<BookBorrowing>(BookBorrowing.class, 15, fetchFunc);
        VaadinCommons.configureDefaultBookGridEx(takenBooksGrid, BookBorrowing::book, localizer);
        VaadinCommons.configureBookGridPreviewEx(takenBooksGrid, BookBorrowing::book, services, localizer);

        takenBooksGrid.addComponentColumn(it -> new Button(localizer.get("userdetails.readbook"),
                        _ -> UI.getCurrent().navigate(BookContentRoute.class, it.book().id().toHexString())))
                .setHeader(localizer.get("userdetails.readbook"));
        takenBooksGrid.addColumn(it -> dateTimeFormatter.format(it.borrowedTime())).setHeader(localizer.get("userdetails.borrowedtime"));
        takenBooksGrid.addColumn(it -> dateTimeFormatter.format(it.returnTime())).setHeader(localizer.get("userdetails.returntime"));
        takenBooksGrid.refresh();


        return takenBooksGrid.component();
    }

    private Component makeWaitList() {
        var waitList = userDataService.waitList();

        var waitListGrid = new Grid<>(Book.class, false);
        VaadinCommons.configureDefaultBookGrid(waitListGrid, localizer);
        VaadinCommons.configureBookGridPreview(waitListGrid, services, localizer);

        waitListGrid.addComponentColumn(book -> new Button(localizer.get("userdetails.remove"), _ ->
        {
            waitList.addRemove(book);
            waitListGrid.setItems(waitList.get());
        })).setHeader(localizer.get("userdetails.remove"));

        waitListGrid.setItems(waitList.get());

        return waitListGrid;
    }

    private Component makeReadList() {
        var readList = userDataService.readBooks();

        var grid = new Grid<>(ReadBook.class, false);
        VaadinCommons.configureDefaultBookGridEx(grid, ReadBook::book, localizer);
        grid.addComponentColumn(rb -> new Button((rb.mark() == ReadBook.Mark.LIKE ? VaadinIcon.THUMBS_UP : VaadinIcon.THUMBS_DOWN).create()))
                .setHeader(localizer.get("userdetails.yourmark"));
        VaadinCommons.configureBookGridPreviewEx(grid, ReadBook::book, services, localizer);
        grid.addComponentColumn(rb -> new Button(localizer.get("userdetails.remove"), _ -> {
            readList.addRemove(rb.book(), null);
            grid.setItems(readList.get());
        })).setHeader(localizer.get("userdetails.remove"));
        grid.setItems(readList.get());
        return grid;
    }

    private Component makeCommentsSection() {
        IntFunction<Page<Comment>> fetchFunc = page -> commentService.listMyComments(PageRequest.of(page, 10));
        var commentsGrid = new GridWithPagination<>(Comment.class, 10, fetchFunc);
        commentsGrid.addColumn(Comment::text).setHeader(localizer.get("userdetails.text"));
        commentsGrid.addColumn(comment -> comment.book().title()).setHeader(localizer.get("userdetails.book"));
        VaadinCommons.configureBookGridPreviewEx(commentsGrid, Comment::book, services, localizer);
        commentsGrid.addComponentColumn(comment -> new Button(localizer.get("userdetails.delete"), _ -> {
            commentService.removeComment(comment.id());
            commentsGrid.refresh();
        }));

        commentsGrid.refresh();
        return commentsGrid.component();
    }

    private Component makeProfileSection() {
        return new VerticalLayout(
                new TextField(localizer.get("userdetails.username"), account.getUsername(), "username") {{
                    addValueChangeListener(e -> {
                        var username = e.getValue();
                        if (StringUtils.isBlank(username))
                            return;

                        try {
                            accountService.editUsername(username);
                            Notification.show(localizer.get("userdetails.usernameupdated"));
                        } catch (Exception ex) {
                            Notification.show(localizer.formatError(ex));
                        }
                    });
                }},
                new Span(localizer.formatGet("userdetails.email", account.getEmail())),
                new Span(localizer.formatGet("userdetails.role", account.getRole().name().toLowerCase())),
                new ComboBox<Locale>(localizer.get("userdetails.localecombobox")) {{
                    setItems(Locale.ENGLISH, Locale.forLanguageTag("UA"));
                    setValue(localizer.locale());
                    addValueChangeListener(e -> {
                        userDataService.setLocale(e.getValue());
                        UI.getCurrent().navigate(UserDetailsRoute.class);
                    });
                }},
                new Button(localizer.get("userdetails.logout"), _ -> {
                    SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
                    UI.getCurrent().navigate(MainRoute.class);
                }) {{
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
                }}
        ) {{
            setAlignItems(Alignment.BASELINE);
        }};
    }

    private Component makeContent() {
        Accordion accordion = new Accordion();

        accordion.add(localizer.get("userdetails.profile"), makeProfileSection());
        accordion.add(localizer.get("userdetails.myorders"), makeMyOrders());
        accordion.add(localizer.get("userdetails.waitlist"), makeWaitList());
        accordion.add(localizer.get("userdetails.readlist"), makeReadList());
        accordion.add(localizer.get("userdetails.mycomments"), makeCommentsSection());

        return VaadinCommons.applyMainStyle(accordion);
    }
}
