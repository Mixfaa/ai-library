package com.mixfa.ailibrary.route.components;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.route.BookRoute;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import java.util.Locale;

public class BookCard extends Card {
    public BookCard(Book book) {
        this(book, com.mixfa.ailibrary.misc.Utils.DEFAULT_LOCALE);
    }

    private void navigateToBook(Book book) {
        UI.getCurrent().navigate(BookRoute.class, book.id().toHexString());
    }

    public BookCard(Book book, Locale locale) {
        this.addThemeVariants(CardVariant.LUMO_STRETCH_MEDIA);
        var image = new Image(book.imageUrl(), "");
        image.getStyle()
                .set("display", "block")
                .set("margin", "auto");

        image.setHeight("250px");
        image.setWidth("150px");
        image.addClickListener(_ -> navigateToBook(book));
        this.setMedia(image);
        this.setWidth("350px");
        this.setTitle(book.title());
        this.setSubtitle(new Span(String.join(", ", book.authors())));
        this.add(new Span("Took/Read %d/%d".formatted(book.tookCount(), book.readCount())));
    }
}