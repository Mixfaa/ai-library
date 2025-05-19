package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.route.components.GridWithPagination;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.CommentService;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.UserDataService;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.function.IntFunction;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@PermitAll
@Route("/library")
public class LibraryRoute extends AppLayout implements HasUrlParameter<String> {
    private final UserDataService userDataService;
    private Library library;
    private Locale userLocale;
    private final SearchEngine.ForBooks booksSearch;
    private final LibraryService libraryService;
    private final CommentService commentService;
    private final Services services;

    public LibraryRoute(Services services) {
        this.booksSearch = services.booksSearchEngine();
        this.libraryService = services.libService();
        this.commentService = services.commentService();
        this.userDataService = services.userDataService();
        this.services = services;
        SideBarInitializer.init(this);
    }

    private Component makeContent() {
        var div = new Div();

        div.setWidthFull();
        div.getStyle()
                .set("max-width", "1500px")
                .set("margin", "0 auto")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "12px")
                .set("box-shadow", "0 8px 24px rgba(0, 0, 0, 0.12)")
                .set("padding", "24px");

        div.add(new HorizontalLayout(new H3(library.name()), new H3(library.address())));
        var queryField = new TextField("Search books");
        IntFunction<Page<Book>> fetchFunc = (page) -> {
            var searchOptions = SearchOption.composition(
                    SearchOption.Books.byTitle(queryField.getValue()),
                    SearchOption.Books.presentInLibs(library.name())
            );

            return booksSearch.find(searchOptions, PageRequest.of(page, 10));
        };
        var booksGrid = new GridWithPagination<>(Book.class, 10, fetchFunc);
        booksGrid.addColumn(book -> book.titleString(userLocale)).setHeader("Title");
        booksGrid.addColumn(Book::authorsString).setHeader("Authors");
        booksGrid.addColumn(book -> fmt("took/count {0}/{1}", book.tookCount(), book.readCount())).setHeader("Took/Count");

        var dialogCache = new LinkedHashMap<Book, Dialog>();
        booksGrid.addComponentColumn(book -> new Button("Preview", _ -> {
            var dialog = dialogCache.computeIfAbsent(book, (key) -> VaadinCommons.bookPreviewDialog(book, userLocale, services));
            dialog.open();
        }));

        booksGrid.addComponentColumn(book -> VaadinCommons.orderBookComboBox(library, book, libraryService, userLocale));

        var searchButton = new Button("Search", _ -> booksGrid.setItems(fetchFunc.apply(0).getContent()));

        div.add(
                new VerticalLayout(queryField, searchButton) {{
                    setDefaultHorizontalComponentAlignment(Alignment.BASELINE);
                }},
                booksGrid.component()
        );

        return div;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            library = libraryService.findOrThrow(parameter);

            setContent(makeContent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
