package com.mixfa.ailibrary.ui;

import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.statistics.StatisticRecord;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.service.misc.impl.Services;
import com.mixfa.ailibrary.service.statistic.StatisticsService;
import com.mixfa.ailibrary.service.statistic.impl.DocxStatisticsWritter;
import com.mixfa.ailibrary.ui.components.CloseDialogButton;
import com.mixfa.ailibrary.ui.components.DateRangePicker;
import com.mixfa.ailibrary.ui.components.SideBarInitializer;
import com.mixfa.ailibrary.ui.components.model.LocalDateRange;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.time.temporal.ChronoUnit;

@Route
@RolesAllowed(Role.ADMIN_ROLE)
public class StatisticsRoute extends AppLayout {
    private final StatisticsService statisticsService;
    private final Localizer localizer = LocalizationProvider.getLocalizator();

    public StatisticsRoute(Services services) {
        this.statisticsService = services.statisticsService();
        SideBarInitializer.init(this, localizer);

        setContent(makeContent());
    }

    private Dialog makeShowStatisticsDialog(StatisticRecord statistics) {
        var dialog = new Dialog();
        dialog.setWidth("1200px");
        dialog.getFooter().add(new CloseDialogButton(dialog, localizer));

        dialog.add(new Div(statistics.from().format(Utils.getDateTimeFormatter()) + " - " + statistics.to().format(Utils.getDateTimeFormatter())));

        var grid = new Grid<>(StatisticRecord.BookStatistic.class, false);
        VaadinCommons.configureDefaultBookGridEx(grid, StatisticRecord.BookStatistic::book,localizer);
        grid.addColumn(StatisticRecord.BookStatistic::moneyPaidString).setHeader(localizer.get("statistics.moneypaid"));
        grid.addColumn(StatisticRecord.BookStatistic::borrowingCount).setHeader(localizer.get("statistics.borrowingcount"));
        grid.setItems(statistics.statistics());
        grid.recalculateColumnWidths();
        dialog.add(grid);

        dialog.getFooter().add(
                new Anchor() {{
                    var docxResource = new StreamResource(statistics.title() + ".docx", () -> {
                        try {
                            // Call your method to generate the DOCX content into a ByteArrayOutputStream
                            var outputStream = DocxStatisticsWritter.createReport(statistics);
                            return new ByteArrayInputStream(outputStream.toByteArray());
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (e instanceof UserFriendlyException ufex)
                                Notification.show(localizer.formatError(ufex));
                            else
                                Notification.show("Error generating report: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                            return null;
                        }
                    });

                    setHref(docxResource);
                    setTitle(localizer.get("statistics.downloadreport"));
                    setText(localizer.get("statistics.downloadreport"));
                }}
        );

        return dialog;
    }

    private Component makeContent() {
        var periodPicker = new DateRangePicker(localizer.get("statistics.selectperiod"), localizer);

        var periodBinder = new Binder<LocalDateRange>()
                .forField(periodPicker)
                .asRequired(localizer.get("statistics.periodrequired"))
                .withValidator(
                        localDateRange -> localDateRange.startDate() == null
                                || localDateRange.endDate() == null
                                || ChronoUnit.DAYS.between(
                                localDateRange.startDate(),
                                localDateRange.endDate()) <= 60,
                        localizer.get("statistics.periodmaxdays"))
                .withValidator(
                        localDateRange -> localDateRange.startDate() == null
                                || localDateRange.endDate() == null
                                || localDateRange.startDate()
                                .isBefore(localDateRange.endDate()),
                        localizer.get("statistics.startdateearlier"))
                .bind(ObjectUtils::CONST, (_, _) -> {
                });


        var fetchButton = new Button(localizer.get("statistics.showstatistics"), _ -> {
            var validationResult = periodBinder.validate().getResult();
            if (validationResult.isEmpty() || !validationResult.get().isError()) {
                var period = (LocalDateRange) periodBinder.getField().getValue();


                if (period == null || period.startDate() == null || period.endDate() == null) {
                    Notification.show(localizer.get("statistics.entervalidperiod"));
                    return;
                }
                var statistics = statisticsService.getStatistics(period.startDate(), period.endDate());

                makeShowStatisticsDialog(statistics).open();
            }
        });

        return new VerticalLayout(new HorizontalLayout(periodPicker), fetchButton);
    }
}
