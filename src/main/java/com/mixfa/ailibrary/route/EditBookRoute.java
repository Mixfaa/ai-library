package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Genre;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.components.CustomMultiSelectComboBox;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.BookService;
import com.mixfa.ailibrary.service.FileStorageService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Route("edit-book")
@RolesAllowed(Role.ADMIN_ROLE)
public class EditBookRoute extends AppLayout implements HasUrlParameter<String> {
    private final BookService bookService;
    private final FileStorageService fileStorageService;
    private Book book;

    private final CustomMultiSelectComboBox<String> authorsSelect = new CustomMultiSelectComboBox<>("Authors",
            Utils::value) {
        {
            setItemLabelGenerator(Utils::value);
        }
    };

    private final MultiSelectComboBox<Genre> genresSelect = new MultiSelectComboBox<>("Genres") {
        {
            setItemLabelGenerator(Genre::name);
            setItems(Genre.values());
        }
    };
    private final TextField isbnField = new TextField("ISBN");
    private final IntegerField publishYearField = new IntegerField("First publish year");
    private final TextField titleField = new TextField("Title");
    private final TextField descriptionField = new TextField("Description");
    private final List<String> imagesList = new ArrayList<>();


    public EditBookRoute(BookService bookService, FileStorageService fileStorageService) {
        this.bookService = bookService;
        this.fileStorageService = fileStorageService;
        SideBarInitializer.init(this);

        setContent(makeContent());
    }

    private Component makeContent() {
        var addImagesDialog = VaadinCommons.editImagesDialog(imagesList, fileStorageService);
        var editImagesBtn = new Button("Add images", _ -> addImagesDialog.open());

        var formLayout = new FormLayout(titleField, descriptionField, authorsSelect, genresSelect, isbnField, publishYearField, addImagesDialog, editImagesBtn, new Button("Save", _ -> {
            var updRequest = new Book.AddRequest(
                    titleField.getValue(),
                    authorsSelect.getValue().toArray(String[]::new),
                    genresSelect.getValue().toArray(Genre[]::new),
                    imagesList.toArray(String[]::new),
                    descriptionField.getValue(),
                    Long.parseLong(isbnField.getValue()),
                    publishYearField.getValue());
            try {
                bookService.editBook(book.id(), updRequest);
                Notification.show("Book successfully edited!");
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
                Notification.show("Error during updating book");
            }
        }));

        return new VerticalLayout(formLayout);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            book = bookService.findBookOrThrow(parameter);
            authorsSelect.setValue(book.authors());
            titleField.setValue(book.title());
            genresSelect.setValue(book.genres());
            descriptionField.setValue(book.description());
            imagesList.addAll(Arrays.asList(book.images()));
        } catch (Exception ex) {

        }
    }
}
