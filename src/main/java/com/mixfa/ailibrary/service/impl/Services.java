package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.service.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class Services {
    private final UserDataService userDataService;
    private final CommentService commentService;
    private final BookService bookService;
    private final SearchEngine.ForBooks booksSearchEngine;
    private final SearchEngine.ForLibraries librariesSearchEngine;
    private final SuggestionService suggestionService;
    private final FileStorageService fileStorageService;
    private final LibraryService libService;
}
