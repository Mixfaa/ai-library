package com.mixfa.ailibrary.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.service.filestorage.FileStorageService;
import com.mixfa.ailibrary.service.library.BookService;
import com.mixfa.ailibrary.service.library.CommentService;
import com.mixfa.ailibrary.service.misc.impl.Services;
import com.mixfa.ailibrary.service.search.SearchEngine;
import com.mixfa.ailibrary.service.user.UserDataService;
import com.mixfa.ailibrary.ui.components.EditBookCompontent;
import com.mixfa.ailibrary.ui.components.GridPagination;
import com.mixfa.ailibrary.ui.components.SideBarInitializer;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizer;
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

@Slf4j
@Route("books-edit")
@RolesAllowed(Role.ADMIN_ROLE)
public class BooksEditRoute extends AppLayout {
    private final BookService bookService;
    private final SearchEngine.ForBooks bookSearchService;
    private final FileStorageService fileStorageService;
    private final Localizer localizer = LocalizationProvider.getLocalizator();
    private final TextField searchField = new TextField(localizer.get("booksedit.searchquery"));

    private final Grid<Book> foundBooksGrid = new Grid<>();
    private final GridPagination<Book> gridPagination = new GridPagination<>(foundBooksGrid, 10, this::fetchBooks);
    private final ObjectMapper objectMapper;

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
        var addDialog = new EditBookCompontent(localizer.get("booksedit.addnewbook"), req -> {
            try {
                bookService.addBook(req);
                Notification.show(localizer.get("booksedit.bookadded"));
            } catch (Exception e) {
                String msg = localizer.get("booksedit.errorregisteringbook");
                if (e instanceof UserFriendlyException ufEx) {
                    msg = localizer.formatError(ufEx);
                }
                Notification.show(msg);
                log.error(e.getLocalizedMessage());
            }
        }, localizer, services);
        return new Button(localizer.get("booksedit.createbook"), _ -> {
            addDialog.open();
        });
    }

    private FormLayout makeSearch() {
        var searchBtn = new Button(localizer.get("booksedit.search"), _ -> {
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
        SideBarInitializer.init(this, localizer);

        foundBooksGrid.addColumn(Book::title).setHeader(localizer.get("aifeatures.grid.title"));
        foundBooksGrid.addColumn(book -> String.join(", ", book.authors())).setHeader(localizer.get("editbook.authors"));
        foundBooksGrid.addComponentColumn(book -> new Button(localizer.get("booksedit.delete"), _ -> {
            try {
                bookService.removeBook(book.id().toHexString());
                foundBooksGrid.setItems(fetchBooks(gridPagination.getCurrentPage()).getContent());
            } catch (Exception e) {
            }
        }));
        foundBooksGrid.addComponentColumn(book -> new Button(localizer.get("booksedit.edit"),
                _ -> {
                    var dialog = new EditBookCompontent(localizer.get("booksedit.editbook"), req -> {
                        try {
                            bookService.editBook(book.id(), req);
                            Notification.show(localizer.get("booksedit.bookeditedsuccessfully"));
                        } catch (Exception e) {
                            if (e instanceof UserFriendlyException ufEx)
                                Notification.show(localizer.formatError(ufEx));
                            else
                                Notification.show(localizer.get("booksedit.errorupdatingbook"));
                        }
                    }, localizer, services);
                    dialog.initForBook(book);
                    dialog.open();
                }));
        var dialogCache = new LinkedHashMap<Book, Dialog>();
        foundBooksGrid.addComponentColumn(book -> new Button(localizer.get("booksedit.preview"), _ -> {
            var dialog = dialogCache.computeIfAbsent(book, (key) -> VaadinCommons.bookPreviewDialog(book, services, localizer));
            dialog.open();
        }));

        setContent(makeContent());

    }
}
