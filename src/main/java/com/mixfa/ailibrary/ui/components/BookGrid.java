package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

import java.util.Collection;


public class BookGrid extends FlexLayout {
    private static final int COLUMNS = 4;
    private final Localizer localizer; 
    public BookGrid(Localizer localizer) {
        this.localizer = localizer;
        setupGrid();
    }

    private void setupGrid() {
        setFlexWrap(FlexLayout.FlexWrap.WRAP); // Allows cards to wrap to new lines
        setJustifyContentMode(FlexLayout.JustifyContentMode.CENTER); // Centers cards horizontally
        setAlignItems(FlexComponent.Alignment.STRETCH); // Cards stretch to fill height of the row

        setWidthFull(); // Container takes full available width

        // Using CSS gap property is generally better than setSpacing() for FlexLayouts
        // as it correctly applies spacing when items wrap.
        getStyle().set("gap", "var(--lumo-space-m)"); // Space between rows and columns (e.g., 16px)

        // Add some padding to the entire container to prevent cards from sticking to the edges of the screen
//        getStyle().set("padding", "var(--lumo-space-m)");

        setHeightFull(); // Ensure the view itself takes full height

//        getStyle()
//                .set("display", "grid")
//                .set("grid-template-columns", "repeat(" + COLUMNS + ", 1fr)")
//                .set("gap", "1em")
//                .set("padding", "1em")
//                .set("width", "95%");
    }

    public void setItems(Collection<Book> books) {
        removeAll();
        for (Book book : books)
            add(new BookCard(book, localizer));
    }
}
