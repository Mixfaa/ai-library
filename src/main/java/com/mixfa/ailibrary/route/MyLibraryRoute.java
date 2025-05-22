package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.BookStatus;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.LibraryWorker;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.components.GridWithPagination;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Locale;
import java.util.function.IntFunction;

@Route("my-library")
@RolesAllowed({Role.ADMIN_ROLE, Role.WORKER_ROLE})
public class MyLibraryRoute extends AppLayout {
    private final Library library;
    private final LibraryService libraryService;
    private final Locale userLocale;

    public MyLibraryRoute(Services services) {
        this.libraryService = services.libService();
        this.userLocale = services.userDataService().getLocale();
        SideBarInitializer.init(this);
        var account = Account.getAuthenticatedAccount();
        if (!(account instanceof LibraryWorker libraryWorker)) {
            UI.getCurrent().navigate(MainRoute.class);
            throw new SecurityException("You do not have permission to access this route");
        }

        library = libraryWorker.getLibrary();
        setContent(makeContent());
    }

    private Component makeContent() {
        var layout = new VerticalLayout();


        var queryField = new TextField("Search");
        IntFunction<Page<BookStatus>> fetchFunc = page -> libraryService.findTakenBooks(queryField.getValue(), library.name(), PageRequest.of(page, 10));
        var takenBooksGrid = new GridWithPagination<>(BookStatus.class, 10, fetchFunc);
        queryField.addValueChangeListener(e -> {
            var page = libraryService.findTakenBooks(e.getValue(), library.name(), PageRequest.of(0, 10));
            takenBooksGrid.setItems(page.getContent());
        });
        takenBooksGrid.refresh();
        VaadinCommons.configureDefaultBookGridEx(takenBooksGrid, BookStatus::book);
        takenBooksGrid.addColumn(bookStatus -> bookStatus.owner().getUsername()).setHeader("Owner");
        takenBooksGrid.addColumn(BookStatus::tookDate).setHeader("Took Date");
        takenBooksGrid.addColumn(BookStatus::returnDate).setHeader("Return Date");

        takenBooksGrid.addComponentColumn(bookStatus -> {
            var comboBox = new ComboBox<>("Change status", BookStatus.Status.values());
            comboBox.setValue(bookStatus.status());

            comboBox.addValueChangeListener(
                    e -> {
                        try {
                            libraryService.updateBookStatusData(bookStatus.id(), e.getValue());
                            Notification.show("Book status changed");
                        } catch (UserFriendlyException ex) {
                            Notification.show(ex.format(userLocale));
                        }
                    }
            );

            return comboBox;
        });


        layout.add(new Text("Manage library orders"), queryField, takenBooksGrid.component());

        return VaadinCommons.applyMainStyle(layout);
    }
}
