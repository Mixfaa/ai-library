package com.mixfa.ailibrary.ui;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.content_provider.GoogleBookContentProvider;
import com.mixfa.ailibrary.model.library.content_provider.PdfFileContentProvider;
import com.mixfa.ailibrary.ui.components.GoogleBooksViewerComponent;
import com.mixfa.ailibrary.ui.components.SideBarInitializer;
import com.mixfa.ailibrary.service.library.BookBorrowingService;
import com.mixfa.ailibrary.service.library.BookService;
import com.mixfa.ailibrary.service.misc.impl.Services;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizator;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("book-contnet-route")
@PermitAll
public class BookContentRoute extends AppLayout implements HasUrlParameter<String> {
    private Book book;
    private final BookService bookService;
    private final BookBorrowingService bookBorrowingService;
    private final Localizator localizator;

    public BookContentRoute(Services services) {
        this.localizator = LocalizationProvider.getLocalizator();
        this.bookService = services.bookService();
        this.bookBorrowingService = services.bookBorrowingService();
        SideBarInitializer.init(this, localizator);
    }

    private Component makeContent() {
        switch (book.contentProvider()) {
            case GoogleBookContentProvider googleBookContentProvider -> {
                return new GoogleBooksViewerComponent(googleBookContentProvider.isbn());
            }
            case PdfFileContentProvider pdfFileProvider -> {
                return new PdfViewer() {{
                    setAddDownloadButton(false);

                    setSrc(pdfFileProvider.link());
                }};
            }
            default -> throw new IllegalStateException("Unexpected value: " + book.contentProvider());
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String bookId) {

        if (!bookBorrowingService.hasAccessToBook(bookId)) {
            Notification.show("You are not allowed to access this book", 10000, Notification.Position.MIDDLE);

            setContent(new VerticalLayout() {{

                Div buttonContainer = new Div(new Button("Go to catalog", _ -> getUI().ifPresent(ui -> ui.navigate(MainRoute.class))));

                Style style = buttonContainer.getStyle();
                style.set("position", "absolute");
                style.set("top", "55%");
                style.set("left", "50%");
                style.set("transform", "translate(-50%, -50%)");

                setSizeFull();

                add(buttonContainer);

            }});
            return;
        }

        book = bookService.findBookOrThrow(bookId);
        setContent(new VerticalLayout(makeContent()));
    }
}
