package com.mixfa.ailibrary.service.suggestion;

import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.suggestion.SuggestedBook;
import com.mixfa.ailibrary.model.suggestion.SuggsetionHint;

import java.util.Locale;

public interface SuggestionService {
    SuggestedBook[] getSuggestions(SearchOption searchOptions, SuggsetionHint suggsetionHint, Locale locale);
}
