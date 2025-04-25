package com.mixfa.ailibrary.route.comp;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.route.BookRoute;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Locale;

public class BookCard extends Div {
    public BookCard(Book book) {
        this(book, com.mixfa.ailibrary.misc.Utils.DEFAULT_LOCALE);
    }

    private void navigateToBook(Book book) {
        UI.getCurrent().navigate(BookRoute.class, book.id().toHexString());
    }

    public BookCard(Book book, Locale locale) {

        this.addClassName("book-card"); // CSS class for styling

        var titleStr = Utils.getFromLocalizedMap(book.localizedTitle(), locale);

        Image bookImage = new Image(book.imageUrl(), "Cover of " + titleStr);
        bookImage.addClassName("book-image"); // CSS class for styling
        bookImage.setWidth("100px");
        bookImage.setHeight("150px");

        // Details container (Title, Author, Rating)
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.addClassName("book-details");
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);

        // Title
        H3 title = new H3(titleStr);
        title.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Top.NONE);

        // Author
        Span author = new Span("Written by: " + String.join(", ", book.authors()));
        author.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        // *** SIMPLIFIED RATING ***
        // Create a Span for the rating text, formatted to one decimal place
        Span ratingText = new Span("Took/Read %d/%d".formatted(book.tookCount(), book.readCount()));
        ratingText.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.Margin.Top.XSMALL // Add a little space above the rating
        );

        // Add details to the details layout
        // Notice we add ratingText directly instead of a layout with stars
        detailsLayout.add(title, author, ratingText);

        // Add image and details to the card
        this.add(bookImage, detailsLayout);
        // Combine all components into the card
        this.addClickListener(_ -> navigateToBook(book));
    }
}