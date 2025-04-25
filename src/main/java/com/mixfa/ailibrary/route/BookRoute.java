package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.route.comp.BookCommentsComponent;
import com.mixfa.ailibrary.route.comp.BookDetailsComponent;
import com.mixfa.ailibrary.route.comp.LibraryFinderComponent;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.*;
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

    private Book book;

    public BookRoute(BookService bookService, LibraryService libraryService,
                     UserDataService userDataService, CommentService commentService,
                     SearchEngine.ForLibraries libSearchEngine) {
        this.bookService = bookService;
        this.libraryService = libraryService;
        this.commentService = commentService;
        this.userDataService = userDataService;
        this.libSearchEngine = libSearchEngine;
        this.userLocale = userDataService.getLocale();

        SideBarInitializer.init(this);
    }

    private Component makeContent() {
        var bookDetails = new BookDetailsComponent(book, commentService, userDataService);
        var libraryFinder = new LibraryFinderComponent(book, libSearchEngine, libraryService, userLocale);
        var comments = new BookCommentsComponent(book, commentService);

        return new VerticalLayout(
                bookDetails,
                libraryFinder,
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