package com.mixfa.ailibrary.model.suggestion;

public interface SuggsetionHint {
    public String makeHint();

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

    public class Composition implements SuggsetionHint {
        private final String hint;

        public Composition(Iterable<SuggsetionHint> hints) {
            var sb = new StringBuilder();

            for (SuggsetionHint suggsetionHint : hints)
                sb.append(suggsetionHint.makeHint()).append("\n");

            this.hint = sb.toString();
        }

        public Composition(SuggsetionHint... hints) {
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
}
