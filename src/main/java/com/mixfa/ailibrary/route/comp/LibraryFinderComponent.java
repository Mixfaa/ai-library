package com.mixfa.ailibrary.route.comp;

import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.SearchEngine;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Locale;
import java.util.function.IntFunction;

public class LibraryFinderComponent extends Button {
    private final Book book;
    private final SearchEngine.ForLibraries libSearchEngine;
    private final LibraryService libraryService;
    private final Locale userLocale;

    public LibraryFinderComponent(Book book, SearchEngine.ForLibraries libSearchEngine,
                                  LibraryService libraryService, Locale userLocale) {
        super("Find in libraries");
        this.book = book;
        this.libSearchEngine = libSearchEngine;
        this.libraryService = libraryService;
        this.userLocale = userLocale;

        addClickListener(e -> findInLibraries());
    }

    private void findInLibraries() {
        var containsBookQuery = SearchOption.Libraries.containsBook(book);
        var libs = libSearchEngine.find(containsBookQuery, PageRequest.of(0, 10));

        if (libs.isEmpty()) {
            Notification.show("This book is not present in any library yet");
            return;
        }

        var dialog = new Dialog("Search in libraries");
        dialog.setWidth("1500px");

        IntFunction<Page<Library>> fetchFunc = (page) -> libSearchEngine.find(containsBookQuery, PageRequest.of(page, 10));

        var pageableGrid = new GridWithPagination<>(Library.class, 10, fetchFunc);
        pageableGrid.addColumn(Library::name).setHeader("Name");
        pageableGrid.addColumn(Library::address).setHeader("Address");

        pageableGrid.addComponentColumn(lib -> VaadinCommons.orderBookComboBox(lib, book, libraryService, userLocale));

        pageableGrid.setItems(libs.getContent());
        dialog.add(pageableGrid.component());
        dialog.getFooter().add(new DialogCloseButton(dialog));
        dialog.open();
    }
}