package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.controller.FileStorageContoller;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Genre;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.comp.CustomMultiSelectComboBox;
import com.mixfa.ailibrary.route.comp.DialogCloseButton;
import com.mixfa.ailibrary.route.comp.LocalizedDetails;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.BookService;
import com.mixfa.ailibrary.service.FileStorageService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private final CustomMultiSelectComboBox<Locale> localeSelect = new CustomMultiSelectComboBox<>("Locales",
            Locale::of) {
        {
            setItemLabelGenerator(Locale::toString);
            addValueChangeListener(event -> {
                localizedDetails.setLocales(event.getValue(), book);
            });
        }
    };

    private final MultiSelectComboBox<Genre> genresSelect = new MultiSelectComboBox<>("Genres") {
        {
            setItemLabelGenerator(Genre::name);
            setItems(Genre.values());
        }
    };

    private final LocalizedDetails localizedDetails = new LocalizedDetails();


    private List<String> imagesList = new ArrayList<>();


    public EditBookRoute(BookService bookService, FileStorageService fileStorageService) {
        this.bookService = bookService;
        this.fileStorageService = fileStorageService;
        SideBarInitializer.init(this);

        setContent(makeContent());
    }

    private Component makeContent() {
        var addImagesDialog = VaadinCommons.editImagesDialog(imagesList, fileStorageService);
        var editImagesBtn = new Button("Add images", _ -> addImagesDialog.open());

        var formLayout = new FormLayout(authorsSelect, localeSelect, genresSelect, addImagesDialog, editImagesBtn,
                localizedDetails.getComponent(), new Button("Save", _ -> {
            var updRequest = new Book.AddRequest(
                    localizedDetails.getTitles(),
                    authorsSelect.getValue().toArray(String[]::new),
                    genresSelect.getValue().toArray(Genre[]::new),
                    imagesList.toArray(String[]::new),
                    localizedDetails.getDescriptions());
            try {
                bookService.editBook(book.id(), updRequest);
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
            localeSelect.setValue(book.localizedTitle().keySet());
            genresSelect.setValue(book.genres());
            localizedDetails.setLocales(book);
            imagesList = new ArrayList<>(List.of(book.images()));
        } catch (Exception ex) {

        }
    }
}
