package com.mixfa.ailibrary.model.library.content_provider;

import com.mixfa.ailibrary.model.library.BookContentProvider;

public record PdfFileContentProvider(String link) implements BookContentProvider {}
