package com.mixfa.ailibrary.ui;

import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.suggestion.*;
import com.mixfa.ailibrary.service.misc.impl.Services;
import com.mixfa.ailibrary.service.repo.BookRepo;
import com.mixfa.ailibrary.service.search.SearchEngine;
import com.mixfa.ailibrary.service.suggestion.SuggestionService;
import com.mixfa.ailibrary.service.user.UserDataService;
import com.mixfa.ailibrary.ui.components.CloseDialogButton;
import com.mixfa.ailibrary.ui.components.CustomMultiSelectComboBox;
import com.mixfa.ailibrary.ui.components.SideBarInitializer;
import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.Component;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Slf4j
@PermitAll
@Route("ai-features")
public class AiFeaturesRoute extends AppLayout {
    private final SuggestionService suggestionService;
    private final SearchEngine.ForBooks bookSearchEngine;
    private final UserDataService userDataService;

    private final Localizer localizer;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final BookRepo bookRepo;
    private final Services services;

    public AiFeaturesRoute(Services services, BookRepo bookRepo) {
        this.localizer = LocalizationProvider.getLocalizator();
        this.suggestionService = services.suggestionService();
        this.bookSearchEngine = services.booksSearchEngine();
        this.userDataService = services.userDataService();
        this.bookRepo = bookRepo;
        this.services = services;
        SideBarInitializer.init(this, localizer);

        setContent(makeContent());
    }


    private Component makeIncludeHints(List<SuggsetionHint> suggestionHints) {
        var includeReadBooksCheckBox = new Checkbox(localizer.get("aifeatures.usereadbooks"));
        includeReadBooksCheckBox.addValueChangeListener(e -> {
            suggestionHints.removeIf(suggsetionHint -> ReadBooksHint.class.isInstance(suggestionHints));
            if (e.getValue())
                suggestionHints.add(new ReadBooksHint(userDataService.readBooks().get()));
        });

        var likedBookSelect = new CustomMultiSelectComboBox<String>(localizer.get("aifeatures.likedbooks"), Function.identity());
        likedBookSelect.setWidth("50%");
        likedBookSelect.addValueChangeListener(e -> {
            suggestionHints.removeIf(suggsetionHint -> LikedBooksHint.class.isInstance(suggestionHints));
            suggestionHints.add(new LikedBooksHint(e.getValue().toArray(String[]::new)));
        });

        var dislikedBookSelect = new CustomMultiSelectComboBox<String>(localizer.get("aifeatures.dislikedbooks"), Function.identity());
        dislikedBookSelect.setWidth("50%");
        dislikedBookSelect.addValueChangeListener(e -> {
            suggestionHints.removeIf(suggsetionHint -> DislikedBooksHint.class.isInstance(suggestionHints));
            suggestionHints.add(new LikedBooksHint(e.getValue().toArray(String[]::new)));
        });

        // select for liked and disliked books
        return new VerticalLayout(includeReadBooksCheckBox, likedBookSelect, dislikedBookSelect);
    }

    private Component makeContent() {


        var header = new Paragraph(localizer.get("aifeatures.title"));

        var suggestionHints = new ArrayList<SuggsetionHint>();

        var optionsDialog = new Dialog(localizer.get("aifeatures.configureoptions"));
        optionsDialog.setWidth("1200px");
        optionsDialog.getFooter().add(new CloseDialogButton(optionsDialog, localizer));

        var optionsAccordion = new Accordion();

        optionsAccordion.add(localizer.get("aifeatures.includeuserstats"), makeIncludeHints(suggestionHints));

        optionsDialog.add(optionsAccordion);

        var optionsDialogButton = new Button(localizer.get("aifeatures.configureoptions"), _ -> optionsDialog.open());
        var getSuggestionsButton = new Button(localizer.get("aifeatures.getsuggestions"), _ -> {

            Notification.show(localizer.get("aifeatures.requestsubmitted"));
            executor.execute(() -> {
                final SuggestedBook[] suggestions;
                try {
                    suggestions = suggestionService.getSuggestions(
                            SearchOption.empty(),
                            SuggsetionHint.composition(suggestionHints),
                            localizer.locale()
                    );
                    log.info("Suggestions are ready: {}", suggestions);
                } catch (Exception e) {
                    this.getUI().ifPresent(ui ->
                            ui.access(() -> Notification.show(localizer.get("aifeatures.erroroccurred"), 5000, Notification.Position.MIDDLE)));
                    log.error("Error while getting suggestions", e);
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

                this.getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show(localizer.get("aifeatures.suggestionready"));

                    var suggestionsDialog = new Dialog(localizer.get("aifeatures.suggestionsdialogtitle"));
                    suggestionsDialog.setWidth("1500px");
                    suggestionsDialog.getFooter().add(new CloseDialogButton(suggestionsDialog, localizer));

                    var suggestionsGrid = new Grid<>(SuggestedBook.class, false);

                    suggestionsGrid.addColumn(SuggestedBook::title).setHeader(localizer.get("aifeatures.grid.title"));
                    suggestionsGrid.addColumn(SuggestedBook::reason).setHeader(localizer.get("aifeatures.grid.reason"));

                    VaadinCommons.<SuggestedBook>configureBookGridPreviewEx(suggestionsGrid, sb -> {
                        return bookRepo.findById(sb.bookId()).orElseThrow();
                    }, services, localizer);

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
