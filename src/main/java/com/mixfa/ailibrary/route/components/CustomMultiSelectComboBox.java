package com.mixfa.ailibrary.route.components;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class CustomMultiSelectComboBox<T> extends MultiSelectComboBox<T> {

    public CustomMultiSelectComboBox(String title, Function<String, T> converter) {
        super(title);

        setAllowCustomValue(true);
        addCustomValueSetListener(event -> {
            T newValue = converter.apply(event.getDetail());

            var items = new HashSet<>(getValue());
            items.add(newValue);
            super.setItems(items);
            super.setValue(items);
        });
    }

    @Override
    public void setValue(Set<T> ts) {
        if (ts == null) {
            super.setValue(ts);
            return;
        }
        super.setItems(ts);
        super.setValue(ts);
    }

    @Override
    public void setValue(Collection<T> ts) {
        if (ts == null) {
            super.setValue(ts);
            return;
        }
        super.setItems(ts);
        super.setValue(ts);
    } 
}
