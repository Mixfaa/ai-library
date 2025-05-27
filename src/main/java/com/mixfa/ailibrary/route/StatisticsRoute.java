package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Money;
import com.mixfa.ailibrary.model.statistics.StatisticsRecord;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.components.CloseDialogButton;
import com.mixfa.ailibrary.route.components.DateRangePicker;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.route.components.model.LocalDateRange;
import com.mixfa.ailibrary.service.StatisticsService;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.ObjectUtils;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Currency;

@Route
@RolesAllowed(Role.ADMIN_ROLE)
public class StatisticsRoute extends AppLayout {
    private final StatisticsService statisticsService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public StatisticsRoute(Services services) {
        this.statisticsService = services.statisticsService();
        SideBarInitializer.init(this);

        setContent(makeContnet());
    }

    private static Dialog makeShowStatisticsDialog(StatisticsRecord statistics) {
        var dialog = new Dialog();
        dialog.setWidth("1200px");
        dialog.getFooter().add(new CloseDialogButton(dialog));

        dialog.add(new Text(statistics.from().format(formatter) + " - " + statistics.to().format(formatter)));

        var grid = new Grid<>(StatisticsRecord.BookStatistics.class, false);
        VaadinCommons.configureDefaultBookGridEx(grid, StatisticsRecord.BookStatistics::book);

        grid.addColumn(stat -> {
            var paidAmount = stat.moneyPaid().amount();
            var currency = Utils.findCurrencyByCodeOrThrow(stat.moneyPaid().currency());
            var digits = currency.getDefaultFractionDigits();

            return digits <= 0 ? paidAmount : paidAmount / Math.pow(10, digits);
        }).setHeader("Money paid");
        grid.addColumn(StatisticsRecord.BookStatistics::borrowingCount).setHeader("Borrowing count");
        grid.setItems(statistics.statistics());
        dialog.add(grid);

        return dialog;
    }

    private Component makeContnet() {
        var periodPicker = new DateRangePicker("Select Period");
        var currencySelect = new Select<Currency>() {{
            setLabel("Currency");
            setItems(Currency.getAvailableCurrencies());
            setItemLabelGenerator(Currency::getDisplayName);
        }};

        var periodBinder = new Binder<LocalDateRange>()
                .forField(periodPicker)
                .asRequired("Period is required")
                .withValidator(
                        localDateRange -> localDateRange.startDate() == null
                                || localDateRange.endDate() == null
                                || ChronoUnit.DAYS.between(
                                localDateRange.startDate(),
                                localDateRange.endDate()) <= 60,
                        "Dates cannot be more than 60 days apart")
                .withValidator(
                        localDateRange -> localDateRange.startDate() == null
                                || localDateRange.endDate() == null
                                || localDateRange.startDate()
                                .isBefore(localDateRange.endDate()),
                        "Start date must be earlier than end date")
                .bind(ObjectUtils::CONST, (_, _) -> {
                });


        var fetchButton = new Button("Show Statistics", _ -> {
            var validationResult = periodBinder.validate().getResult();
            if (validationResult.isEmpty() || !validationResult.get().isError()) {
                var period = (LocalDateRange) periodBinder.getField().getValue();

                var currency = currencySelect.getValue();
                if (currency == null) {
                    Notification.show("Enter currency");
                    return;
                }

                var statistics = statisticsService.getStatistics(period.startDate(), period.endDate(), currency.getNumericCode());

                makeShowStatisticsDialog(statistics).open();
            }
        });


        return new VerticalLayout(new HorizontalLayout(periodPicker, currencySelect), fetchButton);
    }
}
