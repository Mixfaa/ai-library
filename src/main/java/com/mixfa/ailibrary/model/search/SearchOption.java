package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public interface SearchOption {
    List<AggregationOperation> makePipeline();

    default boolean isEmpty() {
        return false;
    }

    final class Books {
        public static SearchOption fromRequest(Book.SearchRequest request) {
            return new SimpleSearchRequestOption(request);
        }

        public static SearchOption presentInLibs(Collection<String> libsNames) {
            return new PresentInLibraries(libsNames);
        }

        public static SearchOption presentInLibs(String... libs) {
            return new PresentInLibraries(libs);
        }

        public static SearchOption byTitle(String query) {
            return new AnyTitleSearchOption(query);
        }

        public static SearchOption byAuthor(Collection<String> authors) {
            return new ByAuthorsSearch(authors);
        }

        public static SearchOption byGenre(Collection<String> genres) {
            return new ByGenresSearch(genres);
        }
    }

    final class Libraries {
        public static SearchOption containsBook(Book book) {
            return new LibContainsBook(book.id());
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

    static SearchOption match(CriteriaDefinition criteriaDefinition) {
        return () -> List.of(Aggregation.match(criteriaDefinition));
    }

    final class Match {
        public static SearchOption all(Criteria... criterias) {
            return withOperator(
                    Criteria::andOperator,
                    criterias
            );
        }

        public static SearchOption any(Criteria... criterias) {
            return withOperator(
                    Criteria::andOperator,
                    criterias
            );
        }

        public static SearchOption nor(Criteria... criterias) {
            return withOperator(
                    Criteria::norOperator,
                    criterias
            );
        }

        public static SearchOption withOperator(
                BiFunction<Criteria, Criteria[], Criteria> operator,
                Criteria... criterias) {
            return () -> List.of(
                    Aggregation.match(operator.apply(new Criteria(), criterias))
            );
        }
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

    abstract class SimpleBase implements SearchOption {
        private final List<AggregationOperation> pipeline = pipeline();

        abstract List<AggregationOperation> pipeline();

        @Override
        public List<AggregationOperation> makePipeline() {
            if (pipeline == null) return List.of();
            return pipeline;
        }

        @Override
        public boolean isEmpty() {
            return pipeline == null || pipeline.isEmpty();
        }
    }
}
