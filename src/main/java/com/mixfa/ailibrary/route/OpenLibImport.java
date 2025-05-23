package com.mixfa.ailibrary.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.content_provider.GoogleBookContentProvider;
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
import java.util.function.Predicate;

@Route("open-lib-import")
@RolesAllowed(Role.ADMIN_ROLE)
public class OpenLibImport extends AppLayout {
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private final BookService bookService;

    public OpenLibImport(BookService bookService) {
        SideBarInitializer.init(this);

        setContent(makeContent());
        this.bookService = bookService;
    }

    public static Optional<String> searchBooks(String query) {
        String url = GOOGLE_BOOKS_API + "?q=intitle:" + URLEncoder.encode(query);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
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
            System.out.println(root);
        } catch (JsonProcessingException e) {
            return List.of();
        }
        JsonNode docs = root.get("items");

        if (docs == null || !docs.isArray()) {
            return Collections.emptyList();
        }

        Predicate<JsonNode> isSuitable = node -> {
            return node.get("accessInfo").get("embeddable").equals("true");
        };
        Function<JsonNode, String> titleProvider = node -> {
            return node.get("volumeInfo").get("title").asText();
        };
        Function<JsonNode, String[]> authorsProvider = node -> {
            var authorEl = node.get("volumeInfo").get("authors");
            try {
                return objectMapper.treeToValue(authorEl, String[].class);
            } catch (Exception _) {
                return null;
            }
        };
        Function<JsonNode, String[]> imagesProvider = node -> {
            return new String[]{node.get("volumeInfo").get("imageLinks").get("thumbnail").asText()};
        };

        Function<JsonNode, Long> isbnProvider = node -> {
            for (var isbnEl : node.get("volumeInfo").get("industryIdentifiers")) {
                if (isbnEl.get("type").asText().equals("ISBN_13"))
                    return isbnEl.get("identifier").asLong();
            }
            return null;
        };

        Function<JsonNode, Integer> publishYearProvider = node -> {
            return Integer.parseInt(node.get("volumeInfo").get("publishedDate").asText().substring(0, 4));
        };

        Function<JsonNode, String> descriptionProvider = node -> {
            return node.get("volumeInfo").get("description").asText();
        };

        List<Book.AddRequest> addRequests = new ArrayList<>();

        for (JsonNode doc : docs) {
//            if (!isSuitable.test(doc)) continue;

            try {
                var title = titleProvider.apply(doc);
                var authors = authorsProvider.apply(doc);
                var description = descriptionProvider.apply(doc);
                var images = imagesProvider.apply(doc);
                var isbn = isbnProvider.apply(doc);
                var publishYear = publishYearProvider.apply(doc);
                var bookContentProvider = new GoogleBookContentProvider(isbn);

                addRequests.add(
                        new Book.AddRequest(
                                title,
                                authors,
                                new String[0],
                                images,
                                description,
                                isbn,
                                publishYear,
                                bookContentProvider
                        )
                );
            } catch (Exception ex) {
                ex.printStackTrace();
                continue;
            }
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
