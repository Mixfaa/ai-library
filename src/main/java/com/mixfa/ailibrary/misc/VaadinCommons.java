package com.mixfa.ailibrary.misc;

import com.mixfa.ailibrary.controller.FileStorageContoller;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.route.components.BookCommentsComponent;
import com.mixfa.ailibrary.route.components.BookDetailsComponent;
import com.mixfa.ailibrary.route.components.CloseDialogButton;
import com.mixfa.ailibrary.route.components.GridWithPagination;
import com.mixfa.ailibrary.service.FileStorageService;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.IntFunction;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@Slf4j
@UtilityClass
public class VaadinCommons {
    public static Button orderBookButton(Library lib, Book book, LibraryService libraryService, Locale locale) {
        var bookAvail = lib.findBookAvailability(book).orElseThrow();
        return new Button("Order book", _ -> {
            try {
                libraryService.tryOrderBook(lib.name(), book.id());
                Notification.show("Book successfully ordered");
            } catch (UserFriendlyException ex) {
                Notification.show(ex.format(locale));
            }
        });
    }

    public static <T extends Component> T applyMainStyle(T component) {
        component.getStyle().set("max-width", "1500px")
                .set("margin", "0 auto")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "12px")
                .set("box-shadow", "0 8px 24px rgba(0, 0, 0, 0.12)")
                .set("padding", "24px");

        return component;
    }

    public static Dialog editImagesDialog(List<String> imagesList, FileStorageService fileStorageService) {
        return new Dialog("Edit images") {{
            this.getFooter().add(new CloseDialogButton(this));

            var urlsGrid = new Grid<String>(String.class, false) {{
                addColumn(Utils::value).setHeader("URL");
                addComponentColumn(url ->
                        new Button("remove", _ -> imagesList.remove(url))
                );
            }};

            var singleFileBuffer = new FileBuffer();
            var upload = new Upload(singleFileBuffer);
            upload.setAcceptedFileTypes("image/*");
            upload.setMaxFileSize(5 * 1000000);
            upload.addSucceededListener(_ -> {
                var fileName = StringUtils.defaultIfEmpty(singleFileBuffer.getFileName(), "image");
                try {
                    var fileData = fileStorageService.write(fileName, singleFileBuffer.getInputStream());
                    var url = FileStorageContoller.makeFileStaticURL(fileData);
                    imagesList.add(url);
                    urlsGrid.setItems(imagesList);

                    Notification.show("Image uploaded successfully");
                } catch (Exception e) {
                    log.error(e.getMessage());
                    Notification.show("Error while writing file");
                }
            });

            this.add(new VerticalLayout(upload, urlsGrid));
        }};
    }

    public static Dialog bookPreviewDialog(Book book, Services services) {
        var commentService = services.commentService();
        var dialog = new Dialog("Book preview " + book.title());

        var content = new HorizontalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        var image = new Image(book.imageUrl(), "Book cover");
        image.setWidth("200px");

        var details = new VerticalLayout(
                new H3(book.title()),
                new Div(new Text("Authors: " + book.authorsString())),
                new Div(new Text("Genres: " + book.genresString())),
                new Div(new Text("Rating: " + commentService.getBookRate(book.id()))),
                new Div(new Text("Took/Read: " + fmt("{0}/{1}", book.tookCount(), book.readCount())))
        );

        content.add(image, details);
        dialog.add(new BookDetailsComponent(book, services), new BookCommentsComponent(book, commentService));
        dialog.getFooter().add(new CloseDialogButton(dialog));

        return dialog;
    }

    public static <T> void configureDefaultBookGridEx(Grid<T> grid, Function<T, Book> tranformer) {
        grid.addColumn(b -> tranformer.apply(b).title()).setHeader("Title");
        grid.addColumn(b -> tranformer.apply(b).authorsString()).setHeader("Authors");
    }

    public static <T> void configureBookGridPreviewEx(Grid<T> grid, Function<T, Book> tranformer, Services services) {
        var bookPreviewDialogCache = new LinkedHashMap<Book, Dialog>();
        grid.addComponentColumn(b -> new Button("Preview", _ -> {
            var book = tranformer.apply(b);
            var dialog = bookPreviewDialogCache.computeIfAbsent(book, key -> bookPreviewDialog(key, services));
            dialog.open();
        })).setHeader("Preview");
    }

    public static void configureDefaultBookGrid(Grid<Book> grid) {
        configureDefaultBookGridEx(grid, Utils::value);
    }

    public static void configureBookGridPreview(Grid<Book> grid, Services services) {
        configureBookGridPreviewEx(grid, Utils::value, services);
    }

    public static IntFunction<Page<Library>> makeLibrariesSearchFunc(SearchEngine.ForLibraries librariesSearchEngine, TextField queryField) {
        return page -> librariesSearchEngine.find(
                SearchOption.Libraries.byName(queryField.getValue()),
                PageRequest.of(page, 10)
        );
    }

    public static IntFunction<Page<Library>> makeLibrariesFetchFunc(SearchEngine.ForLibraries librariesSearchEngine) {
        return page -> librariesSearchEngine.find(
                SearchOption.empty(), PageRequest.of(page, 10)
        );
    }

    public static GridWithPagination<Library> makeLibrariesPageableGrid(IntFunction<Page<Library>> fetchFunc) {
        var grid = new GridWithPagination<>(Library.class, 10, fetchFunc);
        grid.addColumn(Library::name).setHeader("Name");
        grid.addColumn(Library::address).setHeader("Address");


        return grid;
    }
}
