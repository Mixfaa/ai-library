package com.mixfa.ailibrary.model.library.content_provider;

import com.mixfa.ailibrary.model.library.BookContentProvider;

public record GoogleBookContentProvider(long isbn) implements BookContentProvider {
}
