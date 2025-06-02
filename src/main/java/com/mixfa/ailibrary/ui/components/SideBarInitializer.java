package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.ui.*;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SideBarInitializer {


    public static void init(AppLayout layout, Localizer localizer) {
        var sideNav = new SideNav();
        var authentication = Account.getAuthenticated();

        var authTestItem = new SideNavItem(Utils.fmt(localizer.get("sidebar.authenticated"), authentication.getUsername()));
        sideNav.addItem(authTestItem);
        sideNav.addItem(
                new SideNavItem(localizer.get("sidebar.catalog"), MainRoute.class, VaadinIcon.BOOK.create()),
                new SideNavItem(localizer.get("sidebar.mydetails"), UserDetailsRoute.class, VaadinIcon.USER.create()),
                new SideNavItem(localizer.get("sidebar.aisuggestions"), AiFeaturesRoute.class, VaadinIcon.MAGIC.create())
        );

        if (authentication.role().isAdmin()) {
            sideNav.addItem(
                    new SideNavItem(localizer.get("sidebar.statistics"), StatisticsRoute.class, VaadinIcon.MONEY_EXCHANGE.create()),
                    new SideNavItem(localizer.get("sidebar.editusers"), EditUsersRoute.class, VaadinIcon.USER.create()),
                    new SideNavItem(localizer.get("sidebar.editbooks"), BooksEditRoute.class, VaadinIcon.BOOK.create()),
                    new SideNavItem(localizer.get("sidebar.importfromopenlib"), OpenLibImport.class, VaadinIcon.MAGIC.create())

            );
        }

        layout.addToDrawer(sideNav);
        layout.addToNavbar(new DrawerToggle(), new H2(localizer.get("sidebar.apptitle")));
    }
}
