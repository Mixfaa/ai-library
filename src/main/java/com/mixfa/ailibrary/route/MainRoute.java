package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.route.comp.BookGrid;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.UserDataService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Locale;

@Route("")
@PermitAll
public class MainRoute extends AppLayout {
    private final SearchEngine.ForBooks bookSearchService;
    private final BookGrid bookGrid;
    private final TextField searchField = new TextField("Search");
    private final Button searchButton = new Button("Search");
    private final int BOOKS_PER_PAGE = 12;
    private int currentPage = 0;
    private final Span pageIndicator;
    private Page<Book> booksPage;

    public MainRoute(SearchEngine.ForBooks bookSearchService, UserDataService userDataService) {
        this.bookSearchService = bookSearchService;
        Locale locale = userDataService.getLocale();
        this.bookGrid = new BookGrid(locale);
        SideBarInitializer.init(this);

        pageIndicator = new Span();
        loadCurrentPage();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.addClassName("book-list-view");
        mainLayout.add(new HorizontalLayout(searchField, searchButton) {{
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        }});
        mainLayout.add(bookGrid, createPaginationControls());

        setContent(mainLayout);

        searchButton.addClickListener(e -> {
            var q = searchField.getValue();
            var page = bookSearchService.find(SearchOption.Books.byTitle(q), PageRequest.of(0, BOOKS_PER_PAGE));
            currentPage = 0;
            updatePageIndicator();
            bookGrid.setItems(page.getContent());
        });
    }

    private Component createPaginationControls() {
        Button firstPageBtn = new Button("First", e -> goToPage(0));
        Button prevPageBtn = new Button("Previous", e -> goToPage(currentPage - 1));
        Button nextPageBtn = new Button("Next", e -> goToPage(currentPage + 1));
        Button lastPageBtn = new Button("Last", e -> goToPage(booksPage.getTotalPages() - 1));

        updatePageIndicator();

        HorizontalLayout paginationControls = new HorizontalLayout(
                firstPageBtn, prevPageBtn, pageIndicator, nextPageBtn, lastPageBtn
        );
        paginationControls.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        paginationControls.setWidthFull();
        paginationControls.setSpacing(true);

        return paginationControls;
    }

    private void updatePageIndicator() {
        pageIndicator.setText(String.format("Page %d of %d", currentPage + 1, Math.max(1, booksPage.getTotalPages())));
    }

    private void goToPage(int page) {
        if (page < 0 || (booksPage != null && page >= booksPage.getTotalPages())) {
            return; // Invalid page number
        }

        currentPage = page;
        loadCurrentPage();
        updatePageIndicator();
    }

    private void loadCurrentPage() {
        booksPage = bookSearchService.findAll(PageRequest.of(currentPage, BOOKS_PER_PAGE));
        bookGrid.setItems(booksPage.getContent());
    }

}