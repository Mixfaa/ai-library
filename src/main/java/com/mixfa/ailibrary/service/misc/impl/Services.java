package com.mixfa.ailibrary.service.misc.impl;

import com.mixfa.ailibrary.misc.cache.CacheMaintainer;
import com.mixfa.ailibrary.service.ai.AiBookDescriptionService;
import com.mixfa.ailibrary.service.ai.AiFunctions;
import com.mixfa.ailibrary.service.filestorage.FileStorageService;
import com.mixfa.ailibrary.service.library.BookBorrowingService;
import com.mixfa.ailibrary.service.library.BookChatBotService;
import com.mixfa.ailibrary.service.library.BookService;
import com.mixfa.ailibrary.service.library.CommentService;
import com.mixfa.ailibrary.service.search.SearchEngine;
import com.mixfa.ailibrary.service.statistic.StatisticsService;
import com.mixfa.ailibrary.service.suggestion.SuggestionService;
import com.mixfa.ailibrary.service.user.UserDataService;
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
    private final SuggestionService suggestionService;
    private final FileStorageService fileStorageService;
    private final SearchEngine.ForBooks booksSearchEngine;
    private final SearchEngine.ForComments commentsSearchEngine;
    private final BookChatBotService bookChatBotService;
    private final AiFunctions aiFunctions;
    private final AiBookDescriptionService aiBookDescriptionService;
    private final BookBorrowingService bookBorrowingService;
    private final StatisticsService statisticsService;
    private final CacheMaintainer cacheMaintainer;
}
