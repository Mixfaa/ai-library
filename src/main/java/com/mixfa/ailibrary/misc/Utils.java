package com.mixfa.ailibrary.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Genre;
import com.mixfa.ailibrary.model.user.AuthenticatedAccount;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
@SuppressWarnings("unchecked")
public class Utils {

    public static Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static int PAGE_SIZE = 15;

    public static @Nullable JsonNode findJsonNode(JsonNode root, Predicate<JsonNode> predicate) {
        for (JsonNode jsonNode : root)
            if (predicate.test(jsonNode)) return jsonNode;
        return null;
    }

    public static <V> V getFromLocalizedMap(Map<Locale, V> map, Locale key) {
        var value = map.get(key);
        if (value == null)
            value = map.get(DEFAULT_LOCALE);
        if (value == null) {
            var firstKey = map.keySet().stream().findFirst().orElse(null);
            return map.get(firstKey);
        }

        return value;
    }

    public static <V> V getFromLocalizedMap(Map<Locale, V> map) {
        var value = map.get(DEFAULT_LOCALE);
        if (value == null) {
            var firstKey = map.keySet().stream().findFirst().orElse(null);
            return map.get(firstKey);
        }

        return value;
    }

    public static <K, V> V getOrGetFirst(Map<K, V> map, K key) {
        var value = map.get(key);
        if (value == null) {
            var firstKey = map.keySet().stream().findFirst().orElse(null);
            return map.get(firstKey);
        }
        return value;
    }

    public static void appendBookDescForAi(Book book, StringBuilder sb) {
        sb.append("Title = ").append(
                Utils.getFromLocalizedMap(book.localizedTitle())
        ).append("\n");
        sb.append("Authors = ");
        for (String author : book.authors())
            sb.append(author).append(", ");

        sb.append("\nGenres = ");
        for (Genre genre : book.genres())
            sb.append(genre.name()).append(", ");
        sb.append("\n");
    }

    public static <T> T value(T value) {
        return value;
    }

    public static ObjectId idToObj(Object id) {
        if (id instanceof ObjectId objId) return objId;
        if (id instanceof String strId) return new ObjectId(strId);
        throw new IllegalArgumentException("Id must be object id or string");
    }

    public static String idToStr(Object id) {
        if (id instanceof String strId) return strId;
        if (id instanceof ObjectId objId) return objId.toHexString();
        throw new IllegalArgumentException("Id must be object id or string");
    }

    public static String fmt(String pattern, Object... args) {
        return MessageFormat.format(pattern, args);
    }

    public static boolean inBound(double lower, double upper, double value) {
        return value >= lower && value <= upper;
    }

    public static <T, R> R[] map(T[] array, Function<T, R> mapper, Class<R[]> clazz) {
        var copy = allocArray(clazz, array.length);
        for (int i = 0; i < array.length; i++) {
            copy[i] = mapper.apply(array[i]);
        }
        return copy;
    }

    public <T> void iteratePages(Function<Pageable, Page<T>> supplier, Function<Page<T>, Boolean> handler) {
        var pageRequest = PageRequest.of(0, PAGE_SIZE);
        var page = supplier.apply(pageRequest);

        while (handler.apply(page) && page.hasNext()) {
            page = supplier.apply(pageRequest.next());
        }
    }

    public AuthenticatedAccount getPrincipal() {
        return (AuthenticatedAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public <T> T[] allocArray(Class<? extends T[]> newType, int newLength) {
        return ((Object) newType == (Object) Object[].class)
                ? (T[]) new Object[newLength]
                : (T[]) Array.newInstance(newType.getComponentType(), newLength);
    }

    public <T> boolean anyMatch(T[] array, Predicate<T> predicate) {
        if (array == null || array.length == 0) return false;
        for (T t : array)
            if (predicate.test(t))
                return true;
        return false;
    }

    public <T> boolean noneMatch(T[] array, Predicate<T> predicate) {
        if (array == null || array.length == 0) return true;
        for (T t : array)
            if (predicate.test(t))
                return false;
        return true;
    }

    public <T> boolean allMatch(T[] array, Predicate<T> predicate) {
        if (array == null || array.length == 0) return false;
        for (T t : array)
            if (!predicate.test(t))
                return false;
        return true;
    }


    public <T> Optional<T> find(T[] array, Predicate<T> predicate) {
        if (array == null || array.length == 0) return Optional.empty();
        for (T t : array) {
            if (predicate.test(t)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    public <T> T[] filter(T[] array, Predicate<T> predicate) {
        if (array == null || array.length == 0) return null;
        var newLength = count(array, predicate);
        T[] copy = (T[]) allocArray(array.getClass(), newLength);

        int index = 0;
        for (T element : array) {
            if (predicate.test(element)) {
                copy[index] = element;
                ++index;
            }
        }

        return copy;
    }

    public <T> int count(T[] array, Predicate<T> predicate) {
        if (array == null || array.length == 0) return 0;
        int count = 0;
        for (T t : array) {
            if (predicate.test(t)) {
                ++count;
            }
        }
        return count;
    }

    public <T> T[] replace(T[] array, T targetElement, T replacement) {
        var copy = Arrays.copyOf(array, array.length);
        for (int i = 0; i < copy.length; i++) {
            var element = array[i];
            if (Objects.equals(element, targetElement)) {
                array[i] = replacement;
            }
        }
        return copy;
    }

    public <T> T[] replace(T[] array, T replacement, Predicate<T> predicate) {
        var copy = Arrays.copyOf(array, array.length);
        for (int i = 0; i < copy.length; i++) {
            var element = array[i];
            if (predicate.test(element)) {
                array[i] = replacement;
            }
        }
        return copy;
    }


}
