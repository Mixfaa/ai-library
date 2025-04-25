package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.suggestion.SuggestedBook;
import com.mixfa.ailibrary.model.suggestion.SuggsetionHint;

public interface SuggestionService {
    SuggestedBook[] getSuggestions();

    SuggestedBook[] getSuggestions(SearchOption searchOptions);

    SuggestedBook[] getSuggestions(SuggsetionHint suggsetionHint);

    SuggestedBook[] getSuggestions(SearchOption searchOptions, SuggsetionHint suggsetionHint);
}
