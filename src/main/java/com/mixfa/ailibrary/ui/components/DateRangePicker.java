package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.ui.components.model.LocalDateRange;
import com.mixfa.ailibrary.ui.localization.Localizator;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;

import com.mixfa.ailibrary.ui.localization.LocalizationProvider;

public class DateRangePicker extends CustomField<LocalDateRange> {

    private DatePicker start;
    private DatePicker end;

    public DateRangePicker(String label, Localizator localizator) {
        this(localizator);
        setLabel(label);
    }

    public DateRangePicker(Localizator localizator) {

        start = new DatePicker();
        String startDateText = localizator.get("daterange.startdate");
        start.setPlaceholder(startDateText);
        // Sets title for screen readers
        start.setAriaLabel(startDateText);

        end = new DatePicker();
        String endDateText = localizator.get("daterange.enddate");
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
