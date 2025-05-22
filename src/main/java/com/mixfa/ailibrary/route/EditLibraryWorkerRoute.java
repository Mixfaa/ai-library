package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.components.CloseDialogButton;
import com.mixfa.ailibrary.route.components.GridWithPagination;
import com.mixfa.ailibrary.route.components.OpenDialogButton;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.LibraryWorkerServce;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.repo.AccountRepo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.function.IntFunction;

@Route("edit-lib-workers")
@RolesAllowed(Role.ADMIN_ROLE)
public class EditLibraryWorkerRoute extends AppLayout {
    private final AccountRepo accountRepo;
    private final LibraryWorkerServce libraryWorkerServce;
    private final SearchEngine.ForLibraries librarySearchEngine;

    public EditLibraryWorkerRoute(AccountRepo accountRepo, LibraryWorkerServce libraryWorkerServce, SearchEngine.ForLibraries librarySearchEngine) {
        this.accountRepo = accountRepo;
        this.libraryWorkerServce = libraryWorkerServce;
        this.librarySearchEngine = librarySearchEngine;

        SideBarInitializer.init(this);
        setContent(makeContent());
    }

    private Dialog makeEditWorkersDialog(Library library) {
        var dialog = new Dialog();
        dialog.getFooter().add(new CloseDialogButton(dialog));

        var usernameField = new TextField("Username");

        IntFunction<Page<Account>> fetchFunc = page -> accountRepo.findByUsernameContainsIgnoreCase(usernameField.getValue(), PageRequest.of(page, 15));
        var usersGrid = new GridWithPagination<Account>(Account.class, 15, fetchFunc);
        var fetchUsersButton = new Button("Search", _ -> usersGrid.refresh());
        usersGrid.addColumn(Account::getUsername).setHeader("Username");
        usersGrid.addColumn(Account::getEmail).setHeader("Email");
        usersGrid.addComponentColumn(account -> {
            if (account.getRole().isAdmin()) return new Span("Admin");

            if (account.isWorkerOfLibrary(library))
                return new Button("Remove from workers", _ -> {
                    try {
                        libraryWorkerServce.removeFromLibrary(account.getId());
                        Notification.show("Library worker removed");
                        usersGrid.refresh();
                    } catch (Exception e) {
                        Notification.show("Error occured");
                    }
                });
            else return new Button("Add as library worker", _ -> {
                try {
                    libraryWorkerServce.addToLibrary(account.getId(), library.name());
                    Notification.show("Library worker added");
                    usersGrid.refresh();
                } catch (Exception e) {
                    Notification.show("Error occured");
                }
            });
        });

        dialog.add(usernameField);
        dialog.add(fetchUsersButton);
        dialog.add(usersGrid);
        dialog.setWidth("1200px");
        return dialog;
    }

    private Component makeContent() {
        var libNameField = new TextField("Library Name");

        IntFunction<Page<Library>> fetchLibrariesFun = page -> librarySearchEngine.find(SearchOption.Libraries.byName(libNameField.getValue()), PageRequest.of(page, 10));

        var libraryiesGrid = VaadinCommons.makeLibrariesPageableGrid(fetchLibrariesFun);
        var fetchLibsButton = new Button("Search", _ -> libraryiesGrid.refresh());

        var dialogs = new HashMap<Library, Dialog>();
        libraryiesGrid.addComponentColumn(library -> {
            var dialog = dialogs.computeIfAbsent(library, this::makeEditWorkersDialog);
            return new OpenDialogButton("Edit library workers", dialog);
        });

        libNameField.addValueChangeListener(e -> {
            var libs = librarySearchEngine.find(
                    SearchOption.Libraries.byName(e.getValue()),
                    PageRequest.of(0, 10)
            );
            libraryiesGrid.setItems(libs.getContent());
        });

        return new VerticalLayout(
                new HorizontalLayout(libNameField, fetchLibsButton) {{
                    setAlignItems(Alignment.BASELINE);
                }},
                libraryiesGrid
        );
    }
}
