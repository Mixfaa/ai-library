package com.mixfa.ailibrary.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Genre;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.BookService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Route("open-lib-import")
@RolesAllowed(Role.ADMIN_ROLE)
public class OpenLibImport extends AppLayout {
    private static final String OPEN_LIBRARY_API_URL = "https://openlibrary.org/search.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private final BookService bookService;

    public OpenLibImport(BookService bookService) {
        SideBarInitializer.init(this);

        setContent(makeContent());
        this.bookService = bookService;
    }

    public static Optional<String> searchBooks(String query) {
        final String fields = "title,author_name,cover_i,isbn,publish_year";
        String url = OPEN_LIBRARY_API_URL + "?q=" + URLEncoder.encode(query) + "&fields=" + URLEncoder.encode(fields);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                return Optional.ofNullable(response.body());
        } catch (Exception e) {
        }
        return Optional.empty();
    }

    public static List<Book.AddRequest> extractBookData(String jsonResponse) {
        JsonNode root = null;
        try {
            root = objectMapper.readTree(jsonResponse);
            System.out.println(root.toString());
        } catch (JsonProcessingException e) {
            return List.of();
        }
        JsonNode docs = root.get("docs");

        if (docs == null || !docs.isArray()) {
            return Collections.emptyList();
        }

        Function<JsonNode, String> titleProvider = node -> {
            var titleEl = node.get("title");
            if (titleEl == null) return null;
            if (!titleEl.isTextual()) return null;
            return titleEl.asText();
        };
        Function<JsonNode, String[]> authorsProvider = node -> {
            var authorEl = node.get("author_name");
            if (authorEl == null) return null;
            if (!authorEl.isArray()) return null;
            if (authorEl.size() == 0) return null;

            try {
                return objectMapper.treeToValue(authorEl, String[].class);
            } catch (Exception _) {
                return null;
            }
        };
        Function<JsonNode, String[]> imagesProvider = node -> {
            var coverEl = node.get("cover_i");
            if (coverEl == null) return null;

            var coverId = coverEl.asText();
            if (coverId.isBlank()) return null;
            return new String[]{"https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg"};
        };

        Function<JsonNode, Long> isbnProvider = node -> {
            var isbnEl = node.get("isbn");
            if (isbnEl == null || !isbnEl.isArray()) return null;

            return isbnEl.get(0).asLong();
        };

        Function<JsonNode, Integer> publishYearProvider = node -> {
            var publishYearEl = node.get("publish_year");
            if (publishYearEl == null) return null;
            if (publishYearEl.isArray()) return publishYearEl.get(0).asInt();
            if (publishYearEl.isNumber()) return publishYearEl.intValue();
            return null;
        };

        final var random = new Random();
        final var allGenres = new ArrayList<>(List.of(Genre.values()));
        Function<JsonNode, Genre[]> genresProvider = _ -> {
            Collections.shuffle(allGenres);
            return allGenres.stream()
                    .limit(random.nextInt(1, 4))
                    .toArray(Genre[]::new);
        };

        List<Book.AddRequest> addRequests = new ArrayList<>();

        for (JsonNode doc : docs) {
            var title = titleProvider.apply(doc);
            if (title == null) continue;

            String[] authors = null;
            authors = authorsProvider.apply(doc);

            if (authors == null) continue;

            String[] images = imagesProvider.apply(doc);
            if (images == null) continue;

            var genres = genresProvider.apply(doc);

            var isbn = isbnProvider.apply(doc);
            if (isbn == null) continue;

            var publishYear = publishYearProvider.apply(doc);
            if (publishYear == null) continue;

            addRequests.add(
                    new Book.AddRequest(
                            title,
                            authors,
                            genres,
                            images,
                            "No description",
                            isbn,
                            publishYear
                    )
            );
        }

        return addRequests;
    }

    private Component makeContent() {
        var searchField = new TextField("Search in open lib");
        var grid = new Grid<Book.AddRequest>(Book.AddRequest.class, false);
        var requestItems = new AtomicReference<List<Book.AddRequest>>(List.of());
        grid.addColumn(req -> req.title()).setHeader("title");
        grid.addColumn(req -> Arrays.toString(req.authors())).setHeader("authors");
        grid.addComponentColumn(req -> new Button("Add", _ -> {
            try {
                bookService.addBook(req);
                var newItems = requestItems.get();
                newItems.remove(req);

                grid.setItems(newItems);

                Notification.show("Book successfully added");
            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("Error: " + e.getMessage());
            }
        })).setHeader("Add to library");

        searchField.addValueChangeListener(e -> {
            var query = e.getValue();

            var contentOpt = searchBooks(query);
            if (contentOpt.isEmpty()) {
                Notification.show("Nothing found");
                return;
            }
            var bookRequests = extractBookData(contentOpt.get());
            if (bookRequests.isEmpty()) Notification.show("Nothing found");
            requestItems.set(bookRequests);
            grid.setItems(bookRequests);
        });

        return VaadinCommons.applyMainStyle(new VerticalLayout(searchField, grid));
    }
}
