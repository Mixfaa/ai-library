package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.route.comp.BookCommentsComponent;
import com.mixfa.ailibrary.route.comp.BookDetailsComponent;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.*;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.Locale;

@Route("book")
@PermitAll
public class BookRoute extends AppLayout implements HasUrlParameter<String> {
    private final BookService bookService;
    private final LibraryService libraryService;
    private final CommentService commentService;
    private final Locale userLocale;
    private final UserDataService userDataService;
    private final SearchEngine.ForLibraries libSearchEngine;
    private final Services services;

    private Book book;

    public BookRoute(Services services) {
        this.bookService = services.bookService();
        this.libraryService = services.libService();
        this.commentService = services.commentService();
        this.userDataService = services.userDataService();
        this.libSearchEngine = services.librariesSearchEngine();
        this.userLocale = userDataService.getLocale();
        this.services = services;

        SideBarInitializer.init(this);
    }

    private Component makeContent() {
        var bookDetails = new BookDetailsComponent(book, services);
        var comments = new BookCommentsComponent(book, commentService);

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