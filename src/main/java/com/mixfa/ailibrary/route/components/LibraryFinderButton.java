package com.mixfa.ailibrary.route.components;

import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.SearchEngine;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Locale;
import java.util.function.IntFunction;

public class LibraryFinderButton extends Button {
    private final Book book;
    private final SearchEngine.ForLibraries libSearchEngine;
    private final LibraryService libraryService;
    private final Locale userLocale;

    public LibraryFinderButton(Book book, SearchEngine.ForLibraries libSearchEngine,
                               LibraryService libraryService, Locale userLocale) {
        super(VaadinIcon.SEARCH.create());
        super.setTooltipText("Find book in libraries");
        this.book = book;
        this.libSearchEngine = libSearchEngine;
        this.libraryService = libraryService;
        this.userLocale = userLocale;

        addClickListener(e -> findInLibraries());
    }

    private void findInLibraries() {
        final var containsBookSearchOpt = SearchOption.Libraries.containsBook(book);
        var libs = libSearchEngine.find(containsBookSearchOpt, PageRequest.of(0, 10));

        var searchField = new TextField("Search libraries");

        if (libs.isEmpty()) {
            Notification.show("This book is not present in any library yet", 5000, Notification.Position.MIDDLE);
            return;
        }

        var dialog = new Dialog("Search in libraries");
        dialog.setWidth("1500px");

        IntFunction<Page<Library>> fetchFunc = (page) -> libSearchEngine.find(
                SearchOption.composition(SearchOption.Libraries.byName(searchField.getValue()), containsBookSearchOpt),
                PageRequest.of(page, 10));

        var pageableGrid = new GridWithPagination<>(Library.class, 10, fetchFunc);
        pageableGrid.addColumn(Library::name).setHeader("Name");
        pageableGrid.addColumn(Library::address).setHeader("Address");

        pageableGrid.addComponentColumn(lib -> VaadinCommons.orderBookComboBox(lib, book, libraryService, userLocale));

        pageableGrid.setItems(libs.getContent());
        dialog.add(searchField, pageableGrid.component());
        dialog.getFooter().add(new CloseDialogButton(dialog));
        dialog.open();
    }
}