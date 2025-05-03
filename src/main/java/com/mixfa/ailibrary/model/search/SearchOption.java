package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

public interface SearchOption {
    List<AggregationOperation> makePipeline();

    default boolean isEmpty() {
        return false;
    }

    interface Books {
        static SearchOption fromRequest(Book.SearchRequest request) {
            return new SimpleSearchRequestOption(request);
        }

        static SearchOption presentInLibs(Iterable<String> libsNames) {
            return new PresentInLibraries(libsNames);
        }

        static SearchOption presentInLibs(String... libs) {
            return new PresentInLibraries(libs);
        }

        static SearchOption byTitle(String query) {
            return new AnyTitleSearchOption(query);
        }
    }

    interface Libraries {
        static SearchOption containsBook(Book book) {
            return new LibContainsBook(book);
        }

        static SearchOption byName(String query) {
            return new LibraryByName(query);
        }
    }

    class EmptyOption implements SearchOption {
        @Override
        public List<AggregationOperation> makePipeline() {
            return List.of();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        static final EmptyOption instance = new EmptyOption();
    }

    static SearchOption empty() {
        return EmptyOption.instance;
    }

    static SearchOption composition(SearchOption... options) {
        return new SearchOptionComposition(options);
    }

    static SearchOption composition(Iterable<SearchOption> options) {
        return new SearchOptionComposition(options);
    }
}
