package com.mixfa.ailibrary.route;

import com.mixfa.ailibrary.misc.Services;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Library;
import com.mixfa.ailibrary.model.search.PresentInLibraries;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.search.SimpleSearchRequestOption;
import com.mixfa.ailibrary.model.suggestion.ReadBooksHint;
import com.mixfa.ailibrary.model.suggestion.SuggestedBook;
import com.mixfa.ailibrary.model.suggestion.SuggsetionHint;
import com.mixfa.ailibrary.route.comp.DialogCloseButton;
import com.mixfa.ailibrary.route.comp.SideBarInitializer;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.SuggestionService;
import com.mixfa.ailibrary.service.UserDataService;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@PermitAll
@Route("ai-features")
public class AiFeaturesRoute extends AppLayout {
    private final SuggestionService suggestionService;
    private final SearchEngine.ForLibraries librarySearchEngine;
    private final UserDataService userDataService;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public AiFeaturesRoute(Services services, UserDataService userDataService) {
        this.suggestionService = services.suggestionService();
        this.librarySearchEngine = services.librariesSearchEngine();

        SideBarInitializer.init(this);

        setContent(makeContent());
        this.userDataService = userDataService;
    }

    private Component makeSearchInLibrariesSection(List<SearchOption> options) {
        var layout = new VerticalLayout();

        var searchField = new TextField("Search for libraries");
        var librariesGrid = VaadinCommons.makeLibrariesPageableGrid(
                VaadinCommons.makeLibrariesSearchFunc(librarySearchEngine, searchField)
        );
        searchField.addValueChangeListener(_ -> librariesGrid.refresh());
        librariesGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        librariesGrid.addSelectionListener(e -> {
            options.removeIf(searchOption -> PresentInLibraries.class.isInstance(searchField));

            options.add(
                    new PresentInLibraries(e.getAllSelectedItems().stream().map(Library::name).toArray(String[]::new))
            );
        });

        return layout;
    }

    private Component makeTookMinCount(List<SearchOption> options) {
        return new HorizontalLayout(
                new IntegerField("Enter minnimum took count") {{
                    this.setMin(0);
                    this.addValueChangeListener(e -> {
                        options.removeIf(searchOption -> SimpleSearchRequestOption.class.isInstance(searchOption));
                        options.add(
                                new SimpleSearchRequestOption(
                                        Book.SearchRequest.builder()
                                                .minTookCount(e.getValue())
                                                .build()
                                )
                        );
                    });
                }}
        );
    }

    private Component makeIncludeHints(List<SuggsetionHint> suggestionHints) {
        final var READ_BOOKS = "Use your read books";

        var includeReadBooksCheckBox = new Checkbox(READ_BOOKS);
        includeReadBooksCheckBox.addValueChangeListener(e -> {
            suggestionHints.removeIf(suggsetionHint -> ReadBooksHint.class.isInstance(suggestionHints));
            if (e.getValue())
                suggestionHints.add(new ReadBooksHint(userDataService.readBooks().get()));
        });

        return includeReadBooksCheckBox;
    }

    private Component makeContent() {
        var header = new Paragraph("Suggestions service");

        // display options
        // select/configure options
        // get suggstions

        var searchOptions = new ArrayList<SearchOption>();
        var suggestionHints = new ArrayList<SuggsetionHint>();

        var optionsDialog = new Dialog("Configure options");
        optionsDialog.getFooter().add(new DialogCloseButton(optionsDialog));

        var optionsAccordion = new Accordion();

        optionsAccordion.add("Search in libraries", makeSearchInLibrariesSection(searchOptions));
        optionsAccordion.add("Took minnimum count", makeTookMinCount(searchOptions));
        optionsAccordion.add("Include user statistics", makeIncludeHints(suggestionHints));

        optionsDialog.add(optionsAccordion);

        var optionsDialogButton = new Button("Configure suggestion options", _ -> optionsDialog.open());

        var getSuggestionsButton = new Button("Get suggestions", _ -> {

            Notification.show("Your request submitted");
            executor.submit(() -> {
                var suggestions = suggestionService.getSuggestions(
                        SearchOption.composition(searchOptions),
                        SuggsetionHint.composition(suggestionHints)
                );

                UI.getCurrent().access(() -> {
                    Notification.show("Your suggestions are ready!");

                    var suggestionsDialog = new Dialog("Suggestions");
                    suggestionsDialog.getFooter().add(new DialogCloseButton(suggestionsDialog));

                    var suggestionsGrid = new Grid<>(SuggestedBook.class, false);

                    suggestionsGrid.addColumn(SuggestedBook::title).setHeader("Title");
                    suggestionsGrid.addColumn(SuggestedBook::reason).setHeader("Reason");

                    suggestionsGrid.setItems(suggestions);
                    suggestionsDialog.setCloseOnOutsideClick(false);
                    suggestionsDialog.open();
                });
            });
        });

        return VaadinCommons.applyMainStyle(new

                VerticalLayout(header, optionsDialogButton, getSuggestionsButton));
    }
}
