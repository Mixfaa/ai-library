package com.mixfa.ailibrary.ui;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.search.*;
import com.mixfa.ailibrary.service.misc.impl.Services;
import com.mixfa.ailibrary.service.search.SearchEngine;
import com.mixfa.ailibrary.service.user.UserDataService;
import com.mixfa.ailibrary.ui.components.BookGrid;
import com.mixfa.ailibrary.ui.components.CloseDialogButton;
import com.mixfa.ailibrary.ui.components.OpenDialogButton;
import com.mixfa.ailibrary.ui.components.SideBarInitializer;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

class SearchParamsDialog extends Dialog {
    private final List<SearchOption> searchOptions = new ArrayList<>();
    private final Localizer localizer;
    private final Services services;
    private final MongoTemplate mongoTemplate;

    public SearchParamsDialog(Services services, Localizer localizer, MongoTemplate mongoTemplate) {
        super(localizer.get("searchparams.title"));
        this.localizer = localizer;
        this.services = services;
        this.mongoTemplate = mongoTemplate;
        getFooter().add(new CloseDialogButton(this, localizer));

        var accordion = new Accordion();
        accordion.add(localizer.get("searchparams.simpleoptions"), makeSimpleOptions());
        accordion.add(localizer.get("searchparams.byauthorssearch"), makeByAuthorsSearch());
        accordion.add(localizer.get("searchparams.bysubjectssearch"), makeBySubjectsSearch());

        add(accordion);
        setWidth("1200px");
    }

    private Component makeBySubjectsSearch() {
        var subjectTextField = new TextField(localizer.get("searchparams.searchbysubject"));
        var searchBySubjectsGrid = new Grid<String>(String.class, false);
        var subjectsSearch = new Button(localizer.get("searchparams.querysubjects"), _ -> searchBySubjectsGrid.setItems(findSubjects("")));
        searchBySubjectsGrid.addColumn(ObjectUtils::CONST).setHeader(localizer.get("searchparams.subject"));
        subjectTextField.addValueChangeListener(e -> {
            var subjects = findSubjects(e.getValue());
            searchBySubjectsGrid.setItems(subjects);
        });
        searchBySubjectsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        searchBySubjectsGrid.addSelectionListener(e -> {
            searchOptions.removeIf(BySubjectsSearch.class::isInstance);
            var subjects = e.getAllSelectedItems();
            if (!subjects.isEmpty())
                searchOptions.add(new BySubjectsSearch(subjects));
        });
        return new VerticalLayout(
                new HorizontalLayout(subjectTextField, subjectsSearch) {{
                    setAlignItems(Alignment.BASELINE);
                }},
                searchBySubjectsGrid
        );
    }

    private Component makeByAuthorsSearch() {
        var authorTextField = new TextField(localizer.get("searchparams.searchbyauthor"));
        var searchByAuthorsGrid = new Grid<String>(String.class, false);
        var authorsSearch = new Button(localizer.get("searchparams.queryauthors"), _ -> searchByAuthorsGrid.setItems(findAuthors("")));
        searchByAuthorsGrid.addColumn(ObjectUtils::CONST).setHeader(localizer.get("searchparams.author"));
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
        return new VerticalLayout(
                new HorizontalLayout(authorTextField, authorsSearch) {{
                    setAlignItems(Alignment.BASELINE);
                }},
                searchByAuthorsGrid
        );
    }

    private List<String> selectDistinct(String field, Criteria criteria) {
        record Result(String result) {
        }

        var aggregations = new ArrayList<AggregationOperation>();
        aggregations.addLast(Aggregation.unwind(field));
        aggregations.addLast(Aggregation.match(criteria));
        aggregations.addLast(Aggregation.group(field));
        aggregations.addLast(Aggregation.limit(20));
        aggregations.addLast(Aggregation.project().and("_id").as("result"));

        var aggregation = Aggregation.newAggregation(aggregations);

        AggregationResults<Result> results = mongoTemplate.aggregate(aggregation, Book.class, Result.class);

        return results.getMappedResults().stream()
                .map(Result::result)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> findAuthors(String query) {
        return selectDistinct(Book.Fields.authors, Criteria.where(Book.Fields.authors).regex(query, "i"));
    }

    private List<String> findSubjects(String query) {
        return selectDistinct(Book.Fields.subjects, Criteria.where(Book.Fields.subjects).regex(query, "i"));
    }

    private Component makeSimpleOptions() {
        var textField = new TextField(localizer.get("searchparams.searchbyname"));
        textField.addValueChangeListener(e -> {
            searchOptions.removeIf(AnyTitleSearchOption.class::isInstance);
            searchOptions.add(new AnyTitleSearchOption(textField.getValue()));
        });

        var isbnField = new TextField(localizer.get("searchparams.searchbyisbn"));
        isbnField.setPattern("[0-9]*");
        isbnField.addValueChangeListener(e -> {
            searchOptions.removeIf(ISBNSearch.class::isInstance);
            var isbnStr = e.getValue();
            if (!isbnStr.isBlank())
                searchOptions.add(new ISBNSearch(Long.parseLong(isbnField.getValue())));
        });

        var ratingField = new NumberField(localizer.get("searchparams.searchbyminimalrating"));
        ratingField.setMin(0.0);
        ratingField.setMax(5.0);
        ratingField.setStep(0.1);
        ratingField.addValueChangeListener(e -> {
            searchOptions.removeIf(RatingSearch.class::isInstance);
            searchOptions.add(new RatingSearch(e.getValue()));
        });

        return new VerticalLayout(textField, isbnField, ratingField);
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
    private final Localizer localizer = LocalizationProvider.getLocalizator();
    private final Button searchButton = new Button(VaadinIcon.SEARCH.create());
    private final int BOOKS_PER_PAGE = 12;
    private int currentPage = 0;
    private final Span pageIndicator;
    private Page<Book> booksPage;

    private <T extends SearchOption> void addSortItem(
            MenuBar menuBar,
            String localizerKey,
            Class<T> tClass,
            Function<Sort.Direction, T> resolver,
            Function<T, Sort.Direction> directionFunction,
            List<SearchOption> sortSearchOptions
    ) {
        var textSpan = new Span(
                new Text(localizer.get(localizerKey))
        );

        var ascendingIcon = VaadinIcon.ANGLE_UP.create();
        var descendingIcon = VaadinIcon.ANGLE_DOWN.create();

        menuBar.addItem(
                textSpan,
                e -> {
                    var sortOpt = sortSearchOptions.stream().filter(tClass::isInstance).map(tClass::cast).findFirst();
                    if (sortOpt.isPresent()) {
                        var sort = sortOpt.get();
                        sortSearchOptions.remove(sort);
                        var dir = directionFunction.apply(sort);
                        textSpan.remove(ascendingIcon, descendingIcon);
                        if (dir == Sort.Direction.ASC) {
                            textSpan.add(descendingIcon);
                            sortSearchOptions.add(resolver.apply(Sort.Direction.DESC));
                        }
                    } else {
                        textSpan.add(ascendingIcon);
                        sortSearchOptions.add(resolver.apply(Sort.Direction.ASC));
                    }
                }
        );
    }

    public MainRoute(SearchEngine.ForBooks bookSearchService, UserDataService userDataService, Services services, MongoTemplate mongoTemplate) {
        searchParamsComp = new SearchParamsDialog(services, localizer, mongoTemplate);
        this.bookSearchService = bookSearchService;
        this.bookGrid = new BookGrid(localizer);
        SideBarInitializer.init(this, localizer);

        pageIndicator = new Span();
        loadCurrentPage();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        var sortSearchOptions = new ArrayList<SearchOption>();

        var sortMenuBar = new MenuBar();
        {
            addSortItem(sortMenuBar, "mainroute.sortopt.popularity", PopularitySort.class, dir -> {
                return switch (dir) {
                    case ASC -> PopularitySort.ascending();
                    case DESC -> PopularitySort.descending();
                };
            }, PopularitySort::getDirection, sortSearchOptions);

            addSortItem(sortMenuBar, "mainroute.sortopt.rating", RatingSort.class, dir -> {
                return switch (dir) {
                    case ASC -> RatingSort.ascending();
                    case DESC -> RatingSort.descending();
                };
            }, RatingSort::getDirection, sortSearchOptions);

        }

        var openDialogButton = new OpenDialogButton(localizer.get("mainroute.customizesearch"), searchParamsComp);

        var searchLayout = new HorizontalLayout(
                openDialogButton,
                sortMenuBar,
                searchButton
        );
        searchLayout.setFlexShrink(2.5,
                openDialogButton,
                sortMenuBar,
                searchButton);
        searchLayout.setWidthFull();

        mainLayout.add(searchLayout);
        mainLayout.add(new VerticalLayout(bookGrid, createPaginationControls()));

        setContent(mainLayout);

        searchButton.addClickListener(e -> {
            var searchOption = searchParamsComp.getSearchOption();
            if (!sortSearchOptions.isEmpty()) {
                sortSearchOptions.addFirst(searchOption);
                searchOption = SearchOption.composition(sortSearchOptions);
            }

            booksPage = bookSearchService.find(searchOption, PageRequest.of(0, BOOKS_PER_PAGE));
            System.out.println(booksPage.getContent());
            currentPage = 0;
            updatePageIndicator();
            bookGrid.setItems(booksPage.getContent());
        });
    }

    private Component createPaginationControls() {
        Button firstPageBtn = new Button(localizer.get("mainroute.first"), e -> goToPage(0));
        Button prevPageBtn = new Button(localizer.get("mainroute.previous"), e -> goToPage(currentPage - 1));
        Button nextPageBtn = new Button(localizer.get("mainroute.next"), e -> goToPage(currentPage + 1));
        Button lastPageBtn = new Button(localizer.get("mainroute.last"), e -> goToPage(booksPage.getTotalPages() - 1));

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
        pageIndicator.setText(localizer.formatGet("mainroute.page", currentPage + 1, Math.max(1, booksPage.getTotalPages())));
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