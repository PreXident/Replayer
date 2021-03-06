package com.paradoxplaza.eu4.replayer.parser;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;

/**
 * Represents state of the TextParser.
 */
public class State<Context> {

    /** Error message. */
    static protected final String INVALID_TOKEN_EXPECTED_VALUE = "parser.token.invalid.expected.value";

    /** Error message. */
    static protected final String INVALID_TOKEN_EXPECTED_KEYWORD = "parser.token.invalid.expected.keyword";

    /** Intented for subclasses. Parent state to which the control should return. */
    final protected State<Context> parent;

    /**
     * Only constructor. Sets parent state to which the control should return.
     * @param parent
     */
    protected State(final State<Context> parent) {
        this.parent = parent;
        reset();
    }

    /**
     * Processes end of file.
     * @param context Context to apply changes
     * @return new state
     * @throws RuntimeException if end of file was unexpected or some info is missing
     */
    public State<Context> end(final Context context) {
        throw new RuntimeException(l10n("parser.eof.unexpected"));
    }

    /** Processes character and changes context.
     * @param context Context to apply changes
     * @param token character from input
     * @return new state
     */
    public State<Context> processChar(final Context context, final char token) {
        return this;
    }

    /** Processes number and changes context.
     * @param context Context to apply changes
     * @param token number from input
     * @return new state
     */
    public State<Context> processNumber(final Context context, final double token) {
        return this;
    }

    /** Processes word and changes context.
     * @param context Context to apply changes
     * @param word token from input
     * @return new state
     */
    public State<Context> processWord(final Context context, final String word) {
        return this;
    }

    /**
     * Resets inner state. Intented to be overridden.
     */
    protected void reset() {
        //nothing to reset
    }
}
