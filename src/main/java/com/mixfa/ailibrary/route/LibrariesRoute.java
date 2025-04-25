package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.route.comp.GridPagination;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.SearchEngine;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.function.IntFunction;

@Route("/libraries")
@PermitAll
public class LibrariesRoute extends AppLayout {
    private final SearchEngine.ForLibraries librarySearchService;

    public LibrariesRoute(SearchEngine.ForLibraries librarySearchService) {
        this.librarySearchService = librarySearchService;
        SideBarInitializer.init(this);

        var searchField = new TextField("Search library");

        IntFunction<Page<Library>> fetchLibraries = (page) ->
                librarySearchService.find(SearchOption.Libraries.byName(searchField.getValue()), PageRequest.of(page, 10));

        var librariesGrid = new Grid<>(Library.class, false);
        var girdPagination = new GridPagination<>(librariesGrid, 10, fetchLibraries);
        librariesGrid.addColumn(Library::name).setHeader("Name");
        librariesGrid.addColumn(Library::address).setHeader("Address");
        librariesGrid.addComponentColumn(lib -> new Button("Goto", _ -> UI.getCurrent().navigate(LibraryRoute.class, lib.name())));

        if (Account.isAdminAuthenticated()) {
            librariesGrid.addComponentColumn(lib -> new Button("Manage orders", _ -> UI.getCurrent().navigate(ManageLibraryOrders.class, lib.name())));
        }
        girdPagination.refresh();

        var searchBtn = new Button("Search", _ -> {
            var books = fetchLibraries.apply(0);
            librariesGrid.setItems(books.getContent());
        });

        setContent(new VerticalLayout(searchField, searchBtn, librariesGrid, girdPagination));
    }
}
