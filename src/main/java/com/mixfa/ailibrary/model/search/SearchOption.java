package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public interface SearchOption {
    List<AggregationOperation> makePipeline();

    default boolean isEmpty() {
        var pipeline = makePipeline();
        return pipeline == null || pipeline.isEmpty();
    }

    final class Books {
        public static SearchOption byTitle(String query) {
            return new AnyTitleSearchOption(query);
        }

        public static SearchOption byAuthor(Collection<String> authors) {
            return new ByAuthorsSearch(authors);
        }

        public static SearchOption byGenre(Collection<String> genres) {
            return new ByGenresSearch(genres);
        }

        public static SearchOption byISBN(long isbn) {
            return new ISBNSearch(isbn);
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
        return new SearchOptionComposition(Arrays.asList(options));
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
                    Criteria::orOperator,
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

    public static final class EmptyOption implements SearchOption {
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

    @RequiredArgsConstructor
    public static abstract class ImmutableAdapter implements SearchOption {
        private final List<AggregationOperation> pipeline;

        @Override
        final public List<AggregationOperation> makePipeline() {
            return pipeline == null ? List.of() : pipeline;
        }

        @Override
        final public boolean isEmpty() {
            return pipeline == null || pipeline.isEmpty();
        }
    }
}
