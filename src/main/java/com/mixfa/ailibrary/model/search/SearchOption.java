package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

public interface SearchOption {
    List<AggregationOperation> makePipeline();

    default boolean isEmpty() {
        return false;
    }

    final class Books {
        public static SearchOption fromRequest(Book.SearchRequest request) {
            return new SimpleSearchRequestOption(request);
        }

        public static SearchOption presentInLibs(Iterable<String> libsNames) {
            return new PresentInLibraries(libsNames);
        }

        public static SearchOption presentInLibs(String... libs) {
            return new PresentInLibraries(libs);
        }

        public static SearchOption byTitle(String query) {
            return new AnyTitleSearchOption(query);
        }
    }

    final class Libraries {
        public static SearchOption containsBook(Book book) {
            return new LibContainsBook(book);
        }

        public static SearchOption byName(String query) {
            return new LibraryByName(query);
        }
    }

    interface Comments {
        public static SearchOption byBook(Book book) {
            return new CommentsByBook(book.id());
        }

        public static SearchOption byBook(Object bookId) {
            return new CommentsByBook(Utils.idToObj(bookId));
        }
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

    final class EmptyOption implements SearchOption {
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
}
