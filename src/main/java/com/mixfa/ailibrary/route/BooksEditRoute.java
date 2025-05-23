package com.mixfa.ailibrary.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.components.EditBookCompontent;
import com.mixfa.ailibrary.route.components.GridPagination;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.*;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.LinkedHashMap;
import java.util.Locale;

@Slf4j
@Route("books-edit")
@RolesAllowed(Role.ADMIN_ROLE)
public class BooksEditRoute extends AppLayout {
    private final BookService bookService;
    private final SearchEngine.ForBooks bookSearchService;
    private final FileStorageService fileStorageService;
    private final TextField searchField = new TextField("Search query");

    private final Grid<Book> foundBooksGrid = new Grid<>();
    private final GridPagination<Book> gridPagination = new GridPagination<>(foundBooksGrid, 10, this::fetchBooks);
    private final ObjectMapper objectMapper;

    private final Locale userLocale;
    private final CommentService commentService;
    private final UserDataService userDataService;
    private final Services services;

    private Page<Book> fetchBooks(int page) {
        var query = searchField.getValue();

        var pg = PageRequest.of(page, 10);
        var books = query.isBlank() ? bookSearchService.findAll(pg)
                : bookSearchService.find(SearchOption.Books.byTitle(query), pg);

        return books;
    }

    private Component makeAddButton() {
        var addDialog = new EditBookCompontent("Add new book", req -> {
            try {
                bookService.addBook(req);
                Notification.show("Book added successfully");
            } catch (Exception e) {
                String msg = "Error during registering new book";
                if (e instanceof UserFriendlyException ufEx) {
                    msg = ufEx.format(Locale.ENGLISH);
                }
                Notification.show(msg);
                log.error(e.getLocalizedMessage());
            }
        }, services);
        return new Button("Create book", _ -> {
            addDialog.open();
        });
    }

    private FormLayout makeSearch() {
        var searchBtn = new Button("search", _ -> {
            var books = fetchBooks(0);
            foundBooksGrid.setItems(books.getContent());
        });

        return new FormLayout() {
            {
                add(searchField, searchBtn);
            }
        };
    }

    private Component makeContent() {
        var layout = new VerticalLayout();

        layout.add(makeAddButton(), makeSearch(), foundBooksGrid, gridPagination);

        return layout;
    }

    public BooksEditRoute(Services services, ObjectMapper objectMapper) {
        this.bookService = services.bookService();
        this.bookSearchService = services.booksSearchEngine();
        this.fileStorageService = services.fileStorageService();
        this.objectMapper = objectMapper;
        this.commentService = services.commentService();
        this.userDataService = services.userDataService();
        this.services = services;
        this.userLocale = userDataService.getLocale();
        SideBarInitializer.init(this);

        foundBooksGrid.addColumn(Book::title).setHeader("Title");
        foundBooksGrid.addColumn(book -> String.join(", ", book.authors())).setHeader("Author");
        foundBooksGrid.addComponentColumn(book -> new Button("Delete", _ -> {
            try {
                bookService.removeBook(book.id().toHexString());
                foundBooksGrid.setItems(fetchBooks(gridPagination.getCurrentPage()).getContent());
            } catch (Exception e) {
            }
        }));
        foundBooksGrid.addComponentColumn(book -> new Button("Edit",
                _ -> {
                    var dialog = new EditBookCompontent("Edit book", req -> {
                        try {
                            bookService.editBook(book.id(), req);
                            Notification.show("Book successfully edited!");
                        } catch (Exception e) {
                            System.out.println(e.getLocalizedMessage());
                            Notification.show("Error during updating book");
                        }
                    }, services);
                    dialog.initForBook(book);
                    dialog.open();
                }));
        var dialogCache = new LinkedHashMap<Book, Dialog>();
        foundBooksGrid.addComponentColumn(book -> new Button("Preview", _ -> {
            var dialog = dialogCache.computeIfAbsent(book, (key) -> VaadinCommons.bookPreviewDialog(book, services));
            dialog.open();
        }));

        setContent(makeContent());

    }
}
