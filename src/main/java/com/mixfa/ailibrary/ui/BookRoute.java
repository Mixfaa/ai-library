package com.mixfa.ailibrary.ui;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.service.library.BookService;
import com.mixfa.ailibrary.service.library.CommentService;
import com.mixfa.ailibrary.service.misc.impl.Services;
import com.mixfa.ailibrary.service.user.UserDataService;
import com.mixfa.ailibrary.ui.components.BookCommentsComponent;
import com.mixfa.ailibrary.ui.components.BookDetailsComponent;
import com.mixfa.ailibrary.ui.components.SideBarInitializer;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizator;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("book")
@PermitAll
public class BookRoute extends AppLayout implements HasUrlParameter<String> {
    private final BookService bookService;
    private final CommentService commentService;
    private final Localizator localizator;
    private final UserDataService userDataService;
    private final Services services;

    private Book book;

    public BookRoute(Services services) {
        this.bookService = services.bookService();
        this.commentService = services.commentService();
        this.userDataService = services.userDataService();
        this.localizator = LocalizationProvider.getLocalizator();
        this.services = services;

        SideBarInitializer.init(this, localizator);
    }

    private Component makeContent() {
        var localizer = LocalizationProvider.getLocalizator();
        var bookDetails = new BookDetailsComponent(book, localizator, services);
        var comments = new BookCommentsComponent(book, localizer, commentService);

        return new VerticalLayout(
                bookDetails,
                comments
        );
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            book = bookService.findBookOrThrow(parameter);
            setContent(makeContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}