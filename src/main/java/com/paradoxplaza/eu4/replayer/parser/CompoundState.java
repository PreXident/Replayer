package com.paradoxplaza.eu4.replayer.parser;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;

/**
 * Processes compound values in format xxx = { COMPOUND }.
 */
public abstract class CompoundState<Context> extends State<Context> {

    /** What tokens can be expected. */
    protected enum Expecting {
        /** First =. */
        EQUALS {
            @Override
            public char toChar() {
                return '=';
            }

            @Override
            public String toString() {
                return "=";
            }
        },

        /** Opening {. */
        OPENING {
            @Override
            public char toChar() {
                return '{';
            }

            @Override
            public String toString() {
                return "{";
            }
        },

        /** Final } or inner word. */
        CLOSING {
            @Override
            public char toChar() {
                return '}';
            }

            @Override
            public String toString() {
                return "}";
            }
        };

        /**
         * Returns char representing expected token.
         * @return char representing expected token
         */
        public abstract char toChar();
    }

    /** What token is expected. */
    protected Expecting expecting;

    /**
     * Only constructor.
     * @param parent parent state
     */
    public CompoundState(final State<Context> parent) {
        super(parent);
    }

    /**
     * Called from reset.
     * Descendants tend to forget to call super.reset(), which causes errors,
     * so this is the place to reset state.
     */
    protected void compoundReset() {
        //nothing
    }

    /**
     * Informs descendant that element is finished.
     * @param context parse context
     */
    protected void endCompound(final Context context) {
        //nothing
    }

    @Override
    public State<Context> processChar(final Context context, final char token) {
        if (token != expecting.toChar()) {
            throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_KEYWORD), token, expecting));
        }
        switch (expecting) {
            case OPENING:
                expecting = Expecting.CLOSING;
                return this;
            case EQUALS:
                expecting = Expecting.OPENING;
                return this;
            case CLOSING:
                endCompound(context);
                reset();
                return parent;
            default:
                assert false : l10n("parser.token.expect.unknown");
                return this;
        }
    }

    @Override
    protected final void reset() {
        expecting = Expecting.EQUALS;
        compoundReset();
    }
}
