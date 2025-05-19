package com.mixfa.ailibrary.route.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.function.IntFunction;

public class GridPagination<T> extends HorizontalLayout {
    private final Grid<T> grid;
    @Setter
    private IntFunction<Page<T>> dataProvider;
    private final Span pageInfo = new Span();
    private final int pageSize;
    private int currentPage = 0;
    private long totalElements = 0;
    private int totalPages = 0;

    /**
     * Creates a new pagination component for a Grid.
     *
     * @param grid         The Grid to paginate
     * @param pageSize     Number of items per page
     * @param dataProvider Function that takes a page number (0-based) and returns a Spring Data Page
     */
    public GridPagination(Grid<T> grid, int pageSize, IntFunction<Page<T>> dataProvider) {
        this.grid = grid;
        this.pageSize = pageSize;
        this.dataProvider = dataProvider;

        Button firstPage = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT), e -> goToPage(0));
        Button prevPage = new Button(new Icon(VaadinIcon.ANGLE_LEFT), e -> goToPage(currentPage - 1));
        Button nextPage = new Button(new Icon(VaadinIcon.ANGLE_RIGHT), e -> goToPage(currentPage + 1));
        Button lastPage = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT), e -> goToPage(totalPages - 1));

        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        add(firstPage, prevPage, pageInfo, nextPage, lastPage);
    }

    public GridPagination(Grid<T> grid, int pageSize) {
        this(grid, pageSize, _ -> Page.empty());
    }

    /**
     * Navigate to a specific page
     */
    public void goToPage(int pageNumber) {
        if (pageNumber < 0 || (totalPages > 0 && pageNumber >= totalPages)) {
            return;
        }
        loadPage(pageNumber);
    }

    /**
     * Refresh the current page
     */
    public void refresh() {
        loadPage(currentPage);
    }

    /**
     * Load a specific page of data
     */
    private void loadPage(int pageNumber) {
        Page<T> page = dataProvider.apply(pageNumber);

        this.currentPage = pageNumber;
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();

        // Update grid
        grid.setItems(page.getContent());

        // Update pagination display
        updatePaginationInfo();
    }

    private void updatePaginationInfo() {
        int displayedPage = currentPage + 1; // For display, use 1-based
        pageInfo.setText(String.format("Page %d of %d",
                Math.min(displayedPage, totalPages),
                Math.max(1, totalPages)));
    }

    /**
     * Returns the current page index (0-based)
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Returns the total number of pages
     */
    public int getTotalPages() {
        return totalPages;
    }
}

