package com.mixfa.ailibrary.route.comp;

import com.helger.commons.collection.map.MapEntry;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

record LocalizedDetail(
        TextField titleField,
        TextField descField
) {
}

public class LocalizedDetails {
    private final FormLayout formLayout = new FormLayout();
    private final Map<Locale, LocalizedDetail> localizedDetailsMap = new HashMap<>();

    public Component getComponent() {
        return formLayout;
    }

    public void setLocales(Book book) {
        setLocales(book.localizedTitle().keySet(), book);
    }

    public void setLocales(Set<Locale> newLocales, Book book) {
        var locales = localizedDetailsMap.keySet();

        var differencePlus = newLocales.stream().filter(el -> !locales.contains(el)).toList();
        var differenceMinus = locales.stream().filter(el -> !newLocales.contains(el)).toList();

        for (Locale removedLocale : differenceMinus) {
            var detail = localizedDetailsMap.get(removedLocale);
            formLayout.remove(detail.titleField(), detail.descField());
            this.localizedDetailsMap.remove(removedLocale);
        }

        for (Locale addedLocale : differencePlus) {
            var newDetail = new LocalizedDetail(
                    new TextField(Utils.fmt("Title ({0})", addedLocale.toString())) {{
                        setValue(book.localizedTitle().getOrDefault(addedLocale, ""));
                    }},
                    new TextField(Utils.fmt("Description ({0})", addedLocale.toString())) {{
                        setValue(book.localizedDescription().getOrDefault(addedLocale, ""));
                    }}
            );

            formLayout.add(newDetail.titleField(), newDetail.descField());
            localizedDetailsMap.put(addedLocale, newDetail);
        }
    }

    private Map<Locale, String> get(Function<LocalizedDetail, String> mapper) {
        return localizedDetailsMap
                .entrySet()
                .stream()
                .map(entry -> new MapEntry<>(entry.getKey(), mapper.apply(entry.getValue())))
                .filter(it -> it.getValue() != null)
                .collect(Collectors.toMap(MapEntry::getKey, MapEntry::getValue));
    }

    public Map<Locale, String> getDescriptions() {
        return get(it -> it.titleField().getValue());
    }

    public Map<Locale, String> getTitles() {
        return get(it -> it.descField().getValue());
    }
}
