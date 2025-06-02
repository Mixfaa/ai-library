package com.mixfa.ailibrary.ui;

import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.service.misc.impl.Services;
import com.mixfa.ailibrary.service.user.AccountService;
import com.mixfa.ailibrary.ui.components.CloseDialogButton;
import com.mixfa.ailibrary.ui.components.GridWithPagination;
import com.mixfa.ailibrary.ui.components.OpenDialogButton;
import com.mixfa.ailibrary.ui.components.SideBarInitializer;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.HashMap;
import java.util.function.IntFunction;

@Route
@RolesAllowed(Role.ADMIN_ROLE)
public class EditUsersRoute extends AppLayout {
    private final Localizer localizer;
    private final AccountService accountService;

    public EditUsersRoute(AccountService accountService, Services services) {
        this.accountService = accountService;
        this.localizer = LocalizationProvider.getLocalizator();

        SideBarInitializer.init(this, localizer);

        setContent(makeContent());
    }

    private Dialog makeDialog(Account account, Runnable succcesfulCallback) {
        return new Dialog(localizer.get("usersedit.changerole")) {{
            getFooter().add(new CloseDialogButton(this, localizer));

            var passwordField = new PasswordField(localizer.get("usersedit.adminpassword"));
            var comboBox = new ComboBox<Role>(localizer.get("usersedit.role"), Role.values());
            comboBox.setValue(account.getRole());

            var changeButton = new Button(localizer.get("usersedit.changerole"), _ -> {
                var newRole = comboBox.getValue();
                var password = passwordField.getValue();
                try {
                    accountService.changeRole(account.getId(), newRole, password);
                    Notification.show(localizer.get("usersedit.rolechanged"));
                    succcesfulCallback.run();
                } catch (Exception ex) {
                    Notification.show(localizer.formatError(ex));
                }
            });

            add(new VerticalLayout(
                    passwordField, comboBox, changeButton
            ));
        }};
    }

    private Component makeContent() {

        var queryField = new TextField(localizer.get("usersedit.username"));

        IntFunction<Page<Account>> fetchFunc = page -> {
            var q = queryField.getValue();
            var searchOption = StringUtils.isBlank(q) ? SearchOption.empty() : SearchOption.Match.any(
                    Criteria.where(Account.Fields.username).regex(q, "i"),
                    Criteria.where(Account.Fields.email).regex(q, "i")
            );

            return accountService.findAccounts(searchOption, PageRequest.of(page, 20));
        };

        var usersGrid = new GridWithPagination<Account>(Account.class, 20, fetchFunc);
        var fetchButton = new Button(VaadinIcon.SEARCH.create(), _ -> usersGrid.refresh());

        queryField.addValueChangeListener(e -> usersGrid.refresh());
        usersGrid.addColumn(Account::getUsername).setHeader(localizer.get("usersedit.username"));
        usersGrid.addColumn(Account::getEmail).setHeader(localizer.get("usersedit.email"));
        usersGrid.addColumn(Account::getRole).setHeader(localizer.get("usersedit.role"));
        var dialogToUserCache = new HashMap<Account, Dialog>();
        usersGrid.addComponentColumn(account -> new OpenDialogButton(localizer.get("usersedit.changerolebtn"), dialogToUserCache.computeIfAbsent(account, key -> makeDialog(key, usersGrid::refresh))));

        return new VerticalLayout(new HorizontalLayout(queryField, fetchButton) {{
            setAlignItems(Alignment.BASELINE);
        }}, usersGrid);
    }

}
