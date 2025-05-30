package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.ui.BookRoute;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import static com.mixfa.ailibrary.misc.Utils.fmt;

public class BookCard extends Card {


    private void navigateToBook(Book book) {
        UI.getCurrent().navigate(BookRoute.class, book.id().toHexString());
    }

    public BookCard(Book book, Localizer localizer) {
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
        this.add(new Span(fmt(LocalizationProvider.getLocalizator().get("book.tookcount"), book.tookCount())));
    }
}