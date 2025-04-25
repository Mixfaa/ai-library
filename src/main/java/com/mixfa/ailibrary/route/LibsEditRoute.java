package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.comp.GridPagination;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.repo.LibraryRepo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Route("libs-edit")
@RolesAllowed(Role.ADMIN_ROLE)
public class LibsEditRoute extends AppLayout {
    private final LibraryService libraryService;
    private final LibraryRepo libraryRepo;

    private final TextField queryField = new TextField("Library name");
    private final Grid<Library> libsGrid = new Grid<>(Library.class, false);
    private final GridPagination<Library> libsGridPagination = new GridPagination<>(libsGrid, 1, this::fetchLibs);

    private Page<Library> fetchLibs(int page) {
        return libraryRepo.findAllByNameContains(queryField.getValue(),
                PageRequest.of(page, 10));
    }

    public LibsEditRoute(LibraryService libraryService, LibraryRepo libraryRepo) {
        this.libraryService = libraryService;
        this.libraryRepo = libraryRepo;
        SideBarInitializer.init(this);

        setContent(makeContent());

        libsGrid.addColumn(Library::name).setHeader("Name");
        libsGrid.addColumn(Library::address).setHeader("Address");
        libsGrid.addComponentColumn(library -> new Button("Delete", _ -> {
            libraryService.deleteOfflineLib(library.name());
            var libs = libraryRepo.findAllByNameContains(queryField.getValue(), PageRequest.ofSize(10));
            libsGrid.setItems(libs.getContent());
        }));

        libsGrid.addComponentColumn(library -> new Button("Edit", _ -> {
            UI.getCurrent().navigate(EditLibRoute.class, library.name());
        }));
    }

    private FormLayout makeAddForm() {
        return new FormLayout() {
            {
                var nameField = new TextField("Library name");
                var addressField = new TextField("Address");
                var submitBtn = new Button("Register", _ -> {
                    var name = nameField.getValue();
                    var address = addressField.getValue();

                    try {
                        libraryService.registerOfflineLib(new Library.AddRequest(name, address));
                        Notification.show(Utils.fmt("Library {0} registered", name));
                    } catch (Exception e) {
                        System.out.println(e.getLocalizedMessage());
                    }
                });
                add(nameField, addressField, submitBtn);
            }
        };
    }

    private Component makeSearchForm() {
        var form = new FormLayout() {
            {
                var searchBtn = new Button("Search", _ -> libsGrid.setItems(fetchLibs(0).getContent()));

                add(queryField, searchBtn, new FormLayout(libsGrid, libsGridPagination));
            }
        };
        return new VerticalLayout(form, libsGrid);
    }

    private Component makeContent() {
        return new VerticalLayout(
                makeAddForm(),
                makeSearchForm());
    }
}
