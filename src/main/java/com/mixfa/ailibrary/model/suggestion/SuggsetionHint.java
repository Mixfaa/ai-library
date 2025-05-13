package com.mixfa.ailibrary.model.suggestion;

public interface SuggsetionHint {
    public String makeHint();

    public static SuggsetionHint composition(Iterable<SuggsetionHint> hints) {
        return new SuggestionHintComposition(hints);
    }

    public static SuggsetionHint empty() {
        return EmptyHint.instance;
    }

    static class EmptyHint implements SuggsetionHint {
        private EmptyHint() {
        }

        @Override
        public String makeHint() {
            return "";
        }

        public static final SuggsetionHint instance = new EmptyHint();
    }
}
