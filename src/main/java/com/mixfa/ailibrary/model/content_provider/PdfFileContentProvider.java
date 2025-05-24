package com.mixfa.ailibrary.model.content_provider;

import com.mixfa.ailibrary.model.BookContentProvider;

public record PdfFileContentProvider(String link) implements BookContentProvider {}
