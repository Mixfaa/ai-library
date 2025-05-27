package com.mixfa.ailibrary.route.components;

import com.mixfa.ailibrary.route.components.model.LocalDateRange;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;

import java.time.LocalDate;

public class DateRangePicker extends CustomField<LocalDateRange> {

    private DatePicker start;
    private DatePicker end;

    public DateRangePicker(String label) {
        this();
        setLabel(label);
    }

    public DateRangePicker() {
        start = new DatePicker();
        start.setPlaceholder("Start date");
        // Sets title for screen readers
        start.setAriaLabel("Start date");

        end = new DatePicker();
        end.setPlaceholder("End date");
        end.setAriaLabel("End date");

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
