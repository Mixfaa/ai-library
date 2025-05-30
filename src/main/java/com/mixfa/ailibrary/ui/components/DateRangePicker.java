package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.ui.components.model.LocalDateRange;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;

public class DateRangePicker extends CustomField<LocalDateRange> {

    private DatePicker start;
    private DatePicker end;

    public DateRangePicker(String label, Localizer localizer) {
        this(localizer);
        setLabel(label);
    }

    public DateRangePicker(Localizer localizer) {

        start = new DatePicker();
        String startDateText = localizer.get("daterange.startdate");
        start.setPlaceholder(startDateText);
        // Sets title for screen readers
        start.setAriaLabel(startDateText);

        end = new DatePicker();
        String endDateText = localizer.get("daterange.enddate");
        end.setPlaceholder(endDateText);
        end.setAriaLabel(endDateText);

        // Enable manual validation on both date pickers to
        // be able to override their invalid state
        start.setManualValidation(true);
        end.setManualValidation(true);

        add(start, new Text(" – "), end);
    }

    @Override
    protected LocalDateRange generateModelValue() {
        return new LocalDateRange(start.getValue(), end.getValue());
    }

    @Override
    protected void setPresentationValue(LocalDateRange dateRange) {
        start.setValue(dateRange.startDate());
        end.setValue(dateRange.endDate());
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
        start.setInvalid(invalid);
        end.setInvalid(invalid);
    }
}
