package com.mixfa.ailibrary.model.content_provider;

import com.mixfa.ailibrary.model.BookContentProvider;

public record GoogleBookContentProvider(long isbn) implements BookContentProvider {
}
