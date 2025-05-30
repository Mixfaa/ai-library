package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.html.Div;

import java.util.Collection;


public class BookGrid extends Div {
    private static final int COLUMNS = 4;
    private final Localizer localizer;

    public BookGrid(Localizer localizer) {
        this.localizer = localizer;
        setupGrid();
    }

    private void setupGrid() {
        getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(" + COLUMNS + ", 1fr)")
                .set("gap", "1em")
                .set("padding", "1em")
                .set("width", "100%");
    }

    public void setItems(Collection<Book> books) {
        removeAll();
        for (Book book : books)
            add(new BookCard(book, localizer));
    }
}
