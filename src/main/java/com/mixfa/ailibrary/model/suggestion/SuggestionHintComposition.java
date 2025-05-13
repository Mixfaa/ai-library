package com.mixfa.ailibrary.model.suggestion;

public class SuggestionHintComposition implements SuggsetionHint {
    private final String hint;

    public SuggestionHintComposition(Iterable<SuggsetionHint> hints) {
        var sb = new StringBuilder();

        for (SuggsetionHint suggsetionHint : hints)
            sb.append(suggsetionHint.makeHint()).append("\n");

        this.hint = sb.toString();
    }

    @Override
    public String makeHint() {
        return hint;
    }
}