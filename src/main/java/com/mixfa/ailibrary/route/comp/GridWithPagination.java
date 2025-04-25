package com.mixfa.ailibrary.route.comp;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.experimental.Delegate;
import org.springframework.data.domain.Page;

import java.util.function.IntFunction;

public class GridWithPagination<T> extends Grid<T> {
    private final VerticalLayout layout;
    @Delegate(excludes = HorizontalLayout.class)
    private final GridPagination<T> pagination;

    public GridWithPagination(Class<T> type, int pageSize, IntFunction<Page<T>> fetchFunc) {
        super(type, false);
        this.pagination = new GridPagination<>(this, pageSize, fetchFunc);
        this.layout = new VerticalLayout(this, pagination);
    }

    public Component component() {
        return layout;
    }
}
