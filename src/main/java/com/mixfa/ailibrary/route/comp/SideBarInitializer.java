package com.mixfa.ailibrary.route.comp;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.route.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SideBarInitializer {

    public static void init(AppLayout layout) {
        var sideNav = new SideNav();
        var authentication = Account.getAuthenticated();

        var authTestItem = new SideNavItem(Utils.fmt("Authenticated as {0}", authentication.role().name()));
        sideNav.addItem(authTestItem);
        sideNav.addItem(
                new SideNavItem("Catalog", MainRoute.class, VaadinIcon.BOOK.create()),
                new SideNavItem("Libraries", LibrariesRoute.class, VaadinIcon.ARCHIVES.create()),
                new SideNavItem("My details", UserDetailsRoute.class, VaadinIcon.USER.create())
        );

        if (authentication.role().isAdmin()) {
            sideNav.addItem(
                    new SideNavItem("Edit books", BooksEditRoute.class, VaadinIcon.BOOK.create()),
                    new SideNavItem("Edit libraries", LibsEditRoute.class, VaadinIcon.HOME.create())
            );
        }

        layout.addToDrawer(sideNav);
        layout.addToNavbar(new DrawerToggle(), new H2("AI Library"));
    }
}
