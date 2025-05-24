package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.content_provider.GoogleBookContentProvider;
import com.mixfa.ailibrary.model.content_provider.PdfFileContentProvider;
import com.mixfa.ailibrary.route.components.GoogleBooksViewerComponent;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.BookService;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("book-contnet-route")
@PermitAll
public class BookContentRoute extends AppLayout implements HasUrlParameter<String> {
    private Book book;
    private final BookService bookService;

    public BookContentRoute(Services services) {
        this.bookService = services.bookService();
        SideBarInitializer.init(this);
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
        book = bookService.findBookOrThrow(bookId);

        setContent(new VerticalLayout(makeContent()));
    }
}
