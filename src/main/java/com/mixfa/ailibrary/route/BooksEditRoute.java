package com.mixfa.ailibrary.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Genre;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.comp.CustomMultiSelectComboBox;
import com.mixfa.ailibrary.route.comp.GridPagination;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.*;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@Slf4j
@Route("books-edit")
@RolesAllowed(Role.ADMIN_ROLE)
public class BooksEditRoute extends AppLayout {
    private final BookService bookService;
    private final SearchEngine.ForBooks bookSearchService;
    private final FileStorageService fileStorageService;
    private final TextField searchField = new TextField("Search query");

    private final Grid<Book> foundBooksGrid = new Grid<>();
    private final GridPagination<Book> gridPagination = new GridPagination<>(foundBooksGrid, 10, this::fetchBooks);
    private final ObjectMapper objectMapper;

    private final Locale userLocale;
    private final CommentService commentService;
    private final UserDataService userDataService;

    private Page<Book> fetchBooks(int page) {
        var query = searchField.getValue();

        var pg = PageRequest.of(page, 10);
        var books = query.isBlank() ? bookSearchService.findAll(pg)
                : bookSearchService.find(SearchOption.Books.byTitle(query), pg);

        return books;
    }

    private FormLayout makeAddForm() {

        var authorsSelect = new CustomMultiSelectComboBox<String>("Authors", Utils::value);
        var localeSelect = new CustomMultiSelectComboBox<Locale>("Locales", Locale::of);

        var genresSelect = new MultiSelectComboBox<Genre>("Genres") {
            {
                setItemLabelGenerator(Genre::name);
                setItems(Genre.values());
            }
        };

        var localizedTitles = new HashMap<Locale, String>();
        var localizedDescriptions = new HashMap<Locale, String>();

        List<String> imagesList = new ArrayList<>();
        var addImagesDialog = VaadinCommons.editImagesDialog(imagesList, fileStorageService);
        var editImagesBtn = new Button("Add images", _ -> addImagesDialog.open());

        var editLocalizedDataBtn = new Button("Edit localized data", _ -> {
            var editLocalizedDataDialog = new Dialog("Localized data") {
                {
                    var closeBtn = new Button("Close", _ -> close());

                    var locales = localeSelect.getSelectedItems();

                    var dialogLayout = new FormLayout();
                    for (Locale locale : locales) {
                        var localeName = locale.toString();
                        var titleField = new TextField(fmt("Title ({0})", localeName),
                                event -> localizedTitles.put(locale, event.getValue()));
                        titleField.setValue(localizedTitles.getOrDefault(locale, ""));
                        dialogLayout.add(titleField);

                        var descriptionField = new TextField(fmt("Description ({0})", localeName),
                                event -> localizedDescriptions.put(locale, event.getValue()));
                        descriptionField.setValue(localizedDescriptions.getOrDefault(locale, ""));
                        dialogLayout.add(descriptionField);

                        dialogLayout.setColspan(titleField, 1);
                        dialogLayout.setColspan(descriptionField, 1);
                    }

                    dialogLayout.setResponsiveSteps(
                            new FormLayout.ResponsiveStep("0", 2));

                    add(dialogLayout);

                    getFooter().add(closeBtn);
                }
            };
            editLocalizedDataDialog.open();
        });

        var submit = new Button("Submit", VaadinIcon.MAGIC.create(), _ -> {
            var addRequest = new Book.AddRequest(
                    localizedTitles,
                    authorsSelect.getSelectedItems().toArray(String[]::new),
                    genresSelect.getSelectedItems().toArray(Genre[]::new),
                    imagesList.toArray(String[]::new),
                    localizedDescriptions);

            try {
                bookService.addBook(addRequest);
                Notification.show("Book added successfully");
            } catch (Exception e) {
                String msg = "Error during registering new book";
                if (e instanceof UserFriendlyException ufEx) {
                    msg = ufEx.format(Locale.ENGLISH);
                }
                Notification.show(msg);
                log.error(e.getLocalizedMessage());
            }
        });

        return new FormLayout() {
            {
                add(authorsSelect, localeSelect, genresSelect, editLocalizedDataBtn, editImagesBtn, submit);
                setResponsiveSteps(new ResponsiveStep("0", 4));
                setColspan(authorsSelect, 1);
                setColspan(localeSelect, 1);
                setColspan(genresSelect, 1);

                addClassNames(
                        LumoUtility.Border.ALL,
                        LumoUtility.BorderColor.CONTRAST_20,
                        LumoUtility.BorderRadius.MEDIUM,
                        LumoUtility.Padding.MEDIUM);
            }
        };
    }

    private FormLayout makeSearch() {
        var searchBtn = new Button("search", _ -> {
            var books = fetchBooks(0);
            foundBooksGrid.setItems(books.getContent());
        });

        return new FormLayout() {
            {
                add(searchField, searchBtn);
            }
        };
    }

    private Component makeImportForm() {
        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);
        upload.setMaxFiles(1);
        upload.addSucceededListener(_ -> {
            try {
                var requests = objectMapper.readValue(buffer.getInputStream(), new TypeReference<List<Book.AddRequest>>() {
                });
                for (Book.AddRequest request : requests) {
                    bookService.addBook(request);
                }
            } catch (IOException e) {
                Notification.show("Can`t parse file ");
                e.printStackTrace();
            } catch (Exception e) {
                Notification.show("Error, when adding book");
                e.printStackTrace();
            }
        });
        return new FormLayout() {
            {
                add(new Paragraph("Import books from json"));
                add(upload);
            }
        };
    }

    private Component makeContent() {
        var layout = new VerticalLayout();

        layout.add(makeAddForm(), makeSearch(), foundBooksGrid, gridPagination, makeImportForm());

        return layout;
    }

    public BooksEditRoute(BookService bookService, SearchEngine.ForBooks bookSearchService, FileStorageService fileStorageService, ObjectMapper objectMapper, CommentService commentService, UserDataService userDataService) {
        this.bookService = bookService;
        this.bookSearchService = bookSearchService;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
        this.commentService = commentService;
        this.userDataService = userDataService;
        this.userLocale = userDataService.getLocale();
        SideBarInitializer.init(this);

        foundBooksGrid.addColumn(book -> Utils.getFromLocalizedMap(book.localizedTitle())).setHeader("Title");
        foundBooksGrid.addColumn(book -> String.join(", ", book.authors())).setHeader("Author");
        foundBooksGrid.addComponentColumn(book -> new Button("Delete", _ -> {
            try {
                bookService.removeBook(book.id().toHexString());
                foundBooksGrid.setItems(fetchBooks(gridPagination.getCurrentPage()).getContent());
            } catch (Exception e) {
            }
        }));
        foundBooksGrid.addComponentColumn(book -> new Button("Edit",
                _ -> UI.getCurrent().navigate(EditBookRoute.class, book.id().toHexString())));
        var dialogCache = new LinkedHashMap<Book, Dialog>();
        foundBooksGrid.addComponentColumn(book -> new Button("Preview", _ -> {
            var dialog = dialogCache.computeIfAbsent(book, (key) -> VaadinCommons.bookPreviewDialog(book, userLocale, commentService, userDataService));
            dialog.open();
        }));

        setContent(makeContent());

    }
}
