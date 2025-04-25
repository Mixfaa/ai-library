package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.Services;
import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.BookStatus;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.comp.GridWithPagination;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.UserDataService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Locale;
import java.util.function.IntFunction;

@Route("/manage-lib-orders")
@RolesAllowed(Role.ADMIN_ROLE)
public class ManageLibraryOrders extends AppLayout implements HasUrlParameter<String> {
    private final LibraryService libraryService;
    private final UserDataService userDataService;
    private final Locale userLocale;
    private Library library;


    public ManageLibraryOrders(Services services) {
        SideBarInitializer.init(this);
        this.libraryService = services.libService();
        this.userDataService = services.userDataService();
        this.userLocale = userDataService.getLocale();
    }

    private Component makeContent() {
        var layout = new VerticalLayout();

        IntFunction<Page<BookStatus>> fetchFunc = page -> libraryService.findAllTakenBooks(library.name(), PageRequest.of(page, 10));
        var takenBooksGrid = new GridWithPagination<>(BookStatus.class, 10, fetchFunc);
        takenBooksGrid.refresh();
        VaadinCommons.configureDefaultBookGridEx(takenBooksGrid, BookStatus::book, userLocale);
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


        layout.add(new Text("Manage library orders"), takenBooksGrid.component());

        return VaadinCommons.applyMainStyle(layout);
    }

    @Override
    public void setParameter(BeforeEvent event, String libraryId) {
        library = libraryService.findOrThrow(libraryId);

        setContent(makeContent());
    }
}
