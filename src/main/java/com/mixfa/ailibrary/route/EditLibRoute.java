package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.components.CustomMultiSelectComboBox;
import com.mixfa.ailibrary.route.components.CloseDialogButton;
import com.mixfa.ailibrary.route.components.GridWithPagination;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.UserDataService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

@Setter
@Getter
@Accessors(fluent = true)
class LibraryBuilder {
    private String name;
    private String address;
    private List<BookAvailabilityBuilder> bookAvailabilityBuilders;

    public LibraryBuilder(Library source) {
        this.name = source.name();
        this.address = source.address();
        this.bookAvailabilityBuilders = Arrays.stream(source.booksAvailabilities()).map(BookAvailabilityBuilder::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void removeForBook(Book book) {
        bookAvailabilityBuilders.removeIf(it -> it.book == book);
    }

    public BookAvailabilityBuilder getForBook(Book book) {
        for (var bookAvailabilityBuilder : bookAvailabilityBuilders)
            if (bookAvailabilityBuilder.book.id().equals(book.id()))
                return bookAvailabilityBuilder;

        var newBuilder = new BookAvailabilityBuilder(book);
        bookAvailabilityBuilders.add(newBuilder);
        return newBuilder;
    }

    public Library create() {
        var avails = bookAvailabilityBuilders.stream()
                .map(it -> new Library.BookAvailability(it.book, it.localeToAmount))
                .toArray(Library.BookAvailability[]::new);
        return new Library(name, address, avails);
    }

    @Setter
    @Getter
    @Accessors(fluent = true)
    static class BookAvailabilityBuilder {
        private final Book book;
        private final Map<Locale, Long> localeToAmount;

        public BookAvailabilityBuilder(Library.BookAvailability bookAvailability) {
            this.book = bookAvailability.book();
            this.localeToAmount = new HashMap<>(bookAvailability.localeToAmount());
        }

        public BookAvailabilityBuilder(Book book) {
            this.book = book;
            this.localeToAmount = new HashMap<>();
            for (Locale locale : book.localizedTitle().keySet())
                localeToAmount.put(locale, 1L);
        }
    }
}

@Route("edit-lib")
@RolesAllowed(Role.ADMIN_ROLE)
public class EditLibRoute extends AppLayout implements HasUrlParameter<String> {
    private LibraryBuilder libraryBuilder;
    private final LibraryService libraryService;
    private final SearchEngine.ForBooks bookSearchService;
    private final TextField nameField = new TextField("Library name");
    private final TextField addressField = new TextField("Address");
    private final Locale userLocale;

    public EditLibRoute(LibraryService libraryService, SearchEngine.ForBooks bookSearchService, UserDataService userDataService) {
        this.libraryService = libraryService;
        this.bookSearchService = bookSearchService;
        this.userLocale = userDataService.getLocale();
        SideBarInitializer.init(this);

        setContent(makeContent());
    }

    private Component makeContent() {
        var searchBookField = new TextField("Search for book");

        IntFunction<Page<Book>> fetchFunc = (page) -> {
            return bookSearchService.find(SearchOption.Books.byTitle(searchBookField.getValue()),
                    PageRequest.of(page, 10));
        };

        var booksGrid = new GridWithPagination<>(Book.class, 10, fetchFunc);

        VaadinCommons.configureDefaultBookGrid(booksGrid, userLocale);

        booksGrid.addComponentColumn(book -> new Button("edit", _ -> {
            var dialog = new Dialog(Utils.fmt("Edit book availability ({0})", Utils.getFromLocalizedMap(book.localizedTitle())));

            var localesSelect = new CustomMultiSelectComboBox<>("Locales", Locale::of);

            var bookAvailabilityBuilder = libraryBuilder.getForBook(book);
            var localeAmountLayout = new VerticalLayout();
            localesSelect.addValueChangeListener(event -> {
                localeAmountLayout.removeAll();

                for (var locale : event.getValue()) {
                    var numberInput = new NumberField(Utils.fmt("Locale ({0})", locale.toString()), numEvent -> {
                        bookAvailabilityBuilder.localeToAmount().put(locale, numEvent.getValue().longValue());
                    });
                    numberInput
                            .setValue(bookAvailabilityBuilder.localeToAmount().getOrDefault(locale, 0L).doubleValue());
                    numberInput.setStep(1.0);
                    localeAmountLayout.add(numberInput);
                }

            });
            localesSelect.setValue(bookAvailabilityBuilder.localeToAmount().keySet());

            dialog.getFooter().add(new Button("Remove", _ -> libraryBuilder.removeForBook(book)), new CloseDialogButton(dialog));

            dialog.add(new VerticalLayout(localesSelect, localeAmountLayout));

            dialog.open();
        }));

        var searchBtn = new Button("Search", _ -> booksGrid.setItems(fetchFunc.apply(0).getContent()));

        return new VerticalLayout(
                new FormLayout(nameField, addressField), searchBookField, searchBtn, booksGrid.component(),
                new Button("Save", _ -> {
                    libraryService.editOfflineLib(libraryBuilder.name(), libraryBuilder.create());
                    Notification.show("Library updated");
                }));
    }

    @Override
    public void setParameter(BeforeEvent event, String libname) {
        try {
            libraryBuilder = new LibraryBuilder(libraryService.findOrThrow(libname));

            nameField.setValue(libraryBuilder.name());
            addressField.setValue(libraryBuilder.address());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
