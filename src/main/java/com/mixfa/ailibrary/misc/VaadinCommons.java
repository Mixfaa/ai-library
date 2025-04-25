package com.mixfa.ailibrary.misc;

import com.mixfa.ailibrary.controller.FileStorageContoller;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.route.comp.BookCommentsComponent;
import com.mixfa.ailibrary.route.comp.BookDetailsComponent;
import com.mixfa.ailibrary.route.comp.DialogCloseButton;
import com.mixfa.ailibrary.service.CommentService;
import com.mixfa.ailibrary.service.FileStorageService;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.UserDataService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@Slf4j
@UtilityClass
public class VaadinCommons {
    public static ComboBox<Locale> orderBookComboBox(Library lib, Book book, LibraryService libraryService, Locale userLocale) {
        var bookAvail = lib.findBookAvailability(book).orElseThrow();
        return new ComboBox<Locale>("Order book (choose locale)", bookAvail.localeToAmount().keySet()) {{
            setItemLabelGenerator(Locale::getDisplayLanguage);
            addValueChangeListener(e -> {
                try {
                    libraryService.tryOrderBook(lib.name(), book.id(), e.getValue());
                    Notification.show("Book successfully ordered");
                } catch (UserFriendlyException ex) {
                    Notification.show("Error: " + ex.format(userLocale));
                }
            });
        }};
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
            this.getFooter().add(new DialogCloseButton(this));

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

    public static Dialog bookPreviewDialog(Book book, Locale locale, CommentService commentService, UserDataService userDataService) {
        var dialog = new Dialog("Book preview " + book.titleString(locale));

        var content = new HorizontalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        var image = new Image(book.imageUrl(), "Book cover");
        image.setWidth("200px");

        var details = new VerticalLayout(
                new H3(book.titleString(locale)),
                new Div(new Text("Authors: " + book.authorsString())),
                new Div(new Text("Genres: " + book.genresString())),
                new Div(new Text("Rating: " + commentService.getBookRate(book.id()))),
                new Div(new Text("Took/Read: " + fmt("{0}/{1}", book.tookCount(), book.readCount())))
        );

        content.add(image, details);
        dialog.add(new BookDetailsComponent(book, commentService, userDataService), new BookCommentsComponent(book, commentService));
        dialog.getFooter().add(new DialogCloseButton(dialog));

        return dialog;
    }

    public static <T> void configureDefaultBookGridEx(Grid<T> grid, Function<T,Book> tranformer, Locale userLocale) {
        grid.addColumn(b -> tranformer.apply(b).titleString(userLocale));
        grid.addColumn(b -> tranformer.apply(b).authorsString());
    }

    public static <T> void configureBookGridPreviewEx(Grid<T> grid,  Function<T,Book> tranformer,Locale locale, CommentService commentService, UserDataService userDataService) {
        var bookPreviewDialogCache = new LinkedHashMap<Book, Dialog>();
        grid.addComponentColumn(b -> new Button("Preview", _ -> {
            var book = tranformer.apply(b);
            var dialog = bookPreviewDialogCache.computeIfAbsent(book, key -> bookPreviewDialog(key, locale, commentService, userDataService));
            dialog.open();
        }));
    }

    public static void configureDefaultBookGrid(Grid<Book> grid, Locale userLocale) {
       configureDefaultBookGridEx(grid, Utils::value, userLocale);
    }

    public static void configureBookGridPreview(Grid<Book> grid, Locale locale, CommentService commentService, UserDataService userDataService) {
        configureBookGridPreviewEx(grid, Utils::value, locale, commentService, userDataService);
    }
}
