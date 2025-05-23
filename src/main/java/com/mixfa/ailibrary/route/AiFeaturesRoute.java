package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.suggestion.*;
import com.mixfa.ailibrary.route.components.CloseDialogButton;
import com.mixfa.ailibrary.route.components.CustomMultiSelectComboBox;
import com.mixfa.ailibrary.route.components.SideBarInitializer;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.SuggestionService;
import com.mixfa.ailibrary.service.UserDataService;
import com.mixfa.ailibrary.service.impl.Services;
import com.mixfa.ailibrary.service.repo.BookRepo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@PermitAll
@Route("ai-features")
public class AiFeaturesRoute extends AppLayout {
    private final SuggestionService suggestionService;
    private final SearchEngine.ForBooks bookSearchEngine;
    private final UserDataService userDataService;

    private final Locale userLocale;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final BookRepo bookRepo;
    private final Services services;

    public AiFeaturesRoute(Services services, BookRepo bookRepo) {
        this.suggestionService = services.suggestionService();
        this.bookSearchEngine = services.booksSearchEngine();
        this.userDataService = services.userDataService();
        this.bookRepo = bookRepo;
        this.userLocale = userDataService.getLocale();
        this.services = services;
        SideBarInitializer.init(this);

        setContent(makeContent());
    }


    private Component makeIncludeHints(List<SuggsetionHint> suggestionHints) {
        final var READ_BOOKS = "Use your read books";

        var includeReadBooksCheckBox = new Checkbox(READ_BOOKS);
        includeReadBooksCheckBox.addValueChangeListener(e -> {
            suggestionHints.removeIf(suggsetionHint -> ReadBooksHint.class.isInstance(suggestionHints));
            if (e.getValue())
                suggestionHints.add(new ReadBooksHint(userDataService.readBooks().get()));
        });

        var likedBookSelect = new CustomMultiSelectComboBox<String>("Liked books", Utils::value);
        likedBookSelect.setWidth("50%");
        likedBookSelect.addValueChangeListener(e -> {
            suggestionHints.removeIf(suggsetionHint -> LikedBooksHint.class.isInstance(suggestionHints));
            suggestionHints.add(new LikedBooksHint(e.getValue().toArray(String[]::new)));
        });

        var dislikedBookSelect = new CustomMultiSelectComboBox<String>("Disliked books", Utils::value);
        dislikedBookSelect.setWidth("50%");
        dislikedBookSelect.addValueChangeListener(e -> {
            suggestionHints.removeIf(suggsetionHint -> DislikedBooksHint.class.isInstance(suggestionHints));
            suggestionHints.add(new LikedBooksHint(e.getValue().toArray(String[]::new)));
        });

        // select for liked and disliked books
        return new VerticalLayout(includeReadBooksCheckBox, likedBookSelect, dislikedBookSelect);
    }

    private Component makeContent() {
        var header = new Paragraph("Suggestions service");

        var searchOptions = new ArrayList<SearchOption>();
        var suggestionHints = new ArrayList<SuggsetionHint>();

        var optionsDialog = new Dialog("Configure options");
        optionsDialog.setWidth("1200px");
        optionsDialog.getFooter().add(new CloseDialogButton(optionsDialog));

        var optionsAccordion = new Accordion();

        optionsAccordion.add("Include user statistics", makeIncludeHints(suggestionHints));

        optionsDialog.add(optionsAccordion);

        var optionsDialogButton = new Button("Configure suggestion options", _ -> optionsDialog.open());

        var getSuggestionsButton = new Button("Get suggestions", _ -> {

            Notification.show("Your request submitted");
            executor.execute(() -> {
                final SuggestedBook[] suggestions;
                try {
                    suggestions = suggestionService.getSuggestions(
                            SearchOption.composition(searchOptions),
                            SuggsetionHint.composition(suggestionHints)
                    );
                    System.out.println("Suggestions ready " + Arrays.toString(suggestions));
                    log.info("Suggestions are ready: {}", suggestions);
                } catch (Throwable e) {
                    UI.getCurrent().access(() -> Notification.show("Error occurred", 5000, Notification.Position.MIDDLE));
                    log.error("Error while getting suggestions", e);
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

                this.getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Your suggestions are ready!");

                    var suggestionsDialog = new Dialog("Suggestions");
                    suggestionsDialog.setWidth("1500px");
                    suggestionsDialog.getFooter().add(new CloseDialogButton(suggestionsDialog));

                    var suggestionsGrid = new Grid<>(SuggestedBook.class, false);

                    suggestionsGrid.addColumn(SuggestedBook::title).setHeader("Title");
                    suggestionsGrid.addColumn(SuggestedBook::reason).setHeader("Reason");

                    VaadinCommons.<SuggestedBook>configureBookGridPreviewEx(suggestionsGrid, sb -> {
                        return bookRepo.findById(sb.bookId()).orElseThrow();
                    }, services);

                    suggestionsGrid.setItems(suggestions);
                    suggestionsDialog.add(suggestionsGrid);
                    suggestionsDialog.setCloseOnOutsideClick(false);
                    suggestionsDialog.open();
                }));
            });
        });

        return VaadinCommons.applyMainStyle(new VerticalLayout(header, optionsDialogButton, getSuggestionsButton));
    }
}
