package com.mixfa.ailibrary.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.suggestion.ReadBooksHint;
import com.mixfa.ailibrary.model.suggestion.SuggestedBook;
import com.mixfa.ailibrary.model.suggestion.SuggsetionHint;
import com.mixfa.ailibrary.service.AiFunctions;
import com.mixfa.ailibrary.service.SuggestionService;
import com.mixfa.ailibrary.service.UserDataService;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionServiceImpl implements SuggestionService {
    private final UserDataService userDataService;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final AiFunctions aiFunctions;

    private static final String RESPONSE_JSON_SCHEMA = JsonSchemaGenerator.generateForType(SuggestedBook[].class);
    private static final SystemMessage CONFIG_MESSAGE = new SystemMessage(
            String.join(
                    "\n",
                    "You are not chat assistent, you are part of system",
                    "In response provide: bookId, title and reason(why user should read book)",
                    "You must use ID from book description (from search function), not 1,2,3",
                    "You must not wrap json in ```json tag",
                    "You must ALWAYS respond with Json Array, even if single book",
                    "You are not allowed to respond with question or anything except what you were asked for",
                    RESPONSE_JSON_SCHEMA
            )
    );

    private static final UserMessage SEARCH_MESSAGE = new UserMessage("Suggest 3 books to read, based on user`s read books and available books, to list availiable books, use search function");

    private final Retry retry = Retry.of("suggestionService",
            RetryConfig.<SuggestedBook[]>custom()
                    .failAfterMaxAttempts(true)
                    .waitDuration(Duration.ofMillis(250))
                    .retryOnResult(ArrayUtils::isEmpty)
                    .build());

    private final Function<Prompt, SuggestedBook[]> getAndParseFunc = Retry.decorateFunction(retry, this::getAndParse);

    @Override
    public SuggestedBook[] getSuggestions() {
        return getSuggestions(SearchOption.empty());
    }

    @Override
    public SuggestedBook[] getSuggestions(SearchOption searchOptions) {
        var readBooks = userDataService.readBooks().get();
        SuggsetionHint hint = SuggsetionHint.empty();
        if (readBooks.length != 0)
            hint = new ReadBooksHint(readBooks);

        return getSuggestions(SearchOption.empty(), hint);
    }

    @Override
    public SuggestedBook[] getSuggestions(SuggsetionHint suggsetionHint) {
        return getSuggestions(SearchOption.empty(), suggsetionHint);
    }


    private SuggestedBook[] getAndParse(Prompt prompt) {
        var searchResult = chatModel.call(prompt);
        log.info("LLM respond:");
        System.out.println(searchResult);

        var textToParse = searchResult.getResult().getOutput().getText();

        final var jsonPrefix = "```json";
        if (textToParse.startsWith(jsonPrefix))
            textToParse = textToParse.substring(jsonPrefix.length());
        final var jsonPostfix = "```";
        if (textToParse.endsWith(jsonPostfix))
            textToParse = textToParse.substring(0, textToParse.length() - jsonPostfix.length());

        var output = textToParse;

        try {
            var rootTree = objectMapper.readTree(output);

            if (rootTree.isObject()) {
                var tree = Utils.findJsonNode(rootTree, JsonNode::isArray);

                if (tree != null)
                    return new SuggestedBook[]{objectMapper.treeToValue(tree, SuggestedBook.class)};
            }

            return objectMapper.treeToValue(rootTree, SuggestedBook[].class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SuggestedBook[] getSuggestions(SearchOption searchOptions, SuggsetionHint suggsetionHint) {
        var userContext = new UserMessage(suggsetionHint.makeHint());

        var searchTool = aiFunctions.searchFunctionWith(searchOptions);

        var searchPromptOptions = OpenAiChatOptions.builder()
                .toolCallbacks(searchTool)
                .build();

        var prompt = new Prompt(List.of(CONFIG_MESSAGE, SEARCH_MESSAGE, userContext), searchPromptOptions);
        return getAndParseFunc.apply(prompt);
    }
}
