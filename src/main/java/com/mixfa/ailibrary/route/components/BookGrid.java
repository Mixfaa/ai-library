package com.mixfa.ailibrary.route.components;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.vaadin.flow.component.html.Div;

import java.util.Collection;
import java.util.Locale;


public class BookGrid extends Div {
    private final Locale locale;
    private static final int COLUMNS = 4;

    public BookGrid() {
        this(Utils.DEFAULT_LOCALE);
    }

    public BookGrid(Locale locale) {
        this.locale = locale;
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
            add(new BookCard(book, locale));
    }
}
