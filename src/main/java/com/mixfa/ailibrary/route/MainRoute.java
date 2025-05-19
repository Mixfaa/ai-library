package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Genre;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.*;
import com.mixfa.ailibrary.route.components.BookGrid;
import com.mixfa.ailibrary.route.components.CloseDialogButton;
import com.mixfa.ailibrary.route.components.OpenDialogButton;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.UserDataService;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

class SearchParamsDialog extends Dialog {
    private final List<SearchOption> searchOptions = new ArrayList<>();
    private final Services services;
    private final MongoTemplate mongoTemplate;

    public SearchParamsDialog(Services services, MongoTemplate mongoTemplate) {
        super("Search for books");
        this.services = services;
        this.mongoTemplate = mongoTemplate;
        getFooter().add(new CloseDialogButton(this));

        var accordion = new Accordion();
        accordion.add("Simple Options", makeSimpleOptions());
        accordion.add("Search in libraries", makeSearchInLibrariesSection());

        add(accordion);
        setWidth("1200px");
    }

    private List<String> findAuthors(String query) {
        record AuthorResult(String author) {
        }

        var aggregations = new ArrayList<AggregationOperation>();
        aggregations.addLast(Aggregation.unwind(Book.Fields.authors));
        if (!query.isBlank())
            aggregations.addLast(Aggregation.match(Criteria.where(Book.Fields.authors).regex(query, "i")));

        aggregations.addLast(Aggregation.group(Book.Fields.authors));
        aggregations.addLast(Aggregation.limit(10));
        aggregations.addLast(Aggregation.project().and("_id").as("author"));

        var aggregation = Aggregation.newAggregation(aggregations);

        AggregationResults<AuthorResult> results = mongoTemplate.aggregate(aggregation, Book.class, AuthorResult.class);

        return results.getMappedResults().stream()
                .map(AuthorResult::author)
                .filter(Objects::nonNull)
                .toList();
    }

    private Component makeSimpleOptions() {
        var textField = new TextField("Search by name");
        textField.addValueChangeListener(e -> {
            searchOptions.removeIf(AnyTitleSearchOption.class::isInstance);
            searchOptions.add(new AnyTitleSearchOption(textField.getValue()));
        });

        MultiSelectComboBox<Genre> genresSelect = new MultiSelectComboBox<>("Search by genres");
        genresSelect.setItems(Genre.values());
        genresSelect.setItemLabelGenerator(Genre::name);
        genresSelect.addValueChangeListener(e -> {
            searchOptions.removeIf(ByGenresSearch.class::isInstance);
            searchOptions.add(new ByGenresSearch(e.getValue().stream().map(Genre::name).toList()));
        });

        var authorTextField = new TextField("Search by author");
        var searchByAuthorsGrid = new Grid<String>(String.class, false);
        var authorsSearch = new Button("Query authors", _ -> searchByAuthorsGrid.setItems(findAuthors("")));
        searchByAuthorsGrid.addColumn(Utils::value).setHeader("Author");
        authorTextField.addValueChangeListener(e -> {
            var authors = findAuthors(e.getValue());
            searchByAuthorsGrid.setItems(authors);
        });
        searchByAuthorsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        searchByAuthorsGrid.addSelectionListener(e -> {
            searchOptions.removeIf(ByAuthorsSearch.class::isInstance);
            var authorsToSearch = e.getAllSelectedItems();
            if (!authorsToSearch.isEmpty())
                searchOptions.add(new ByAuthorsSearch(authorsToSearch));
        });

        return new VerticalLayout(textField, genresSelect, new HorizontalLayout(authorTextField, authorsSearch) {{
            setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        }}, searchByAuthorsGrid);
    }

    private Component makeSearchInLibrariesSection() {
        var searchField = new TextField("Search for libraries");
        var librariesGrid = VaadinCommons.makeLibrariesPageableGrid(
                VaadinCommons.makeLibrariesSearchFunc(services.librariesSearchEngine(), searchField)
        );
        searchField.addValueChangeListener(_ -> librariesGrid.refresh());
        librariesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        librariesGrid.refresh();
        librariesGrid.addSelectionListener(e -> {
            searchOptions.removeIf(PresentInLibraries.class::isInstance);
            var selectedLibs = e.getAllSelectedItems();
            if (!selectedLibs.isEmpty())
                searchOptions.add(
                        new PresentInLibraries(selectedLibs.stream().map(Library::name).toArray(String[]::new))
                );
        });

        return new VerticalLayout(searchField, librariesGrid.component());
    }

    public SearchOption getSearchOption() {
        return searchOptions.isEmpty() ? SearchOption.empty() : SearchOption.composition(searchOptions);
    }
}

@Route("")
@PermitAll
public class MainRoute extends AppLayout {
    private final SearchEngine.ForBooks bookSearchService;
    private final BookGrid bookGrid;
    private final SearchParamsDialog searchParamsComp;
    private final Button searchButton = new Button("Search");
    private final int BOOKS_PER_PAGE = 12;
    private int currentPage = 0;
    private final Span pageIndicator;
    private Page<Book> booksPage;

    public MainRoute(SearchEngine.ForBooks bookSearchService, UserDataService userDataService, Services services, MongoTemplate mongoTemplate) {
        searchParamsComp = new SearchParamsDialog(services, mongoTemplate);
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
        var searchLayout = new HorizontalLayout(
                new OpenDialogButton("Customize searech", searchParamsComp),
                searchButton
        );
        mainLayout.add(searchLayout);
        mainLayout.add(bookGrid, createPaginationControls());

        setContent(mainLayout);

        searchButton.addClickListener(e -> {
            var searchOption = searchParamsComp.getSearchOption();
            booksPage = bookSearchService.find(searchOption, PageRequest.of(0, BOOKS_PER_PAGE));
            System.out.println(booksPage.getContent());
            currentPage = 0;
            updatePageIndicator();
            bookGrid.setItems(booksPage.getContent());
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
        var searchOption = searchParamsComp.getSearchOption();
        booksPage = bookSearchService.find(searchOption, PageRequest.of(currentPage, BOOKS_PER_PAGE));
        bookGrid.setItems(booksPage.getContent());
    }
}