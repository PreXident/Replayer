package com.paradoxplaza.eu4.replayer.parser;

/**
 * Ignores value, both ={...} and =VALUE.
 */
public class Ignore<Context> extends State<Context> {

    enum Expecting { EQUALS, SWITCH, CLOSING }

    Expecting expecting;

    /** Number of opened "{". */
    int rightCount;

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Ignore(final State<Context> parent) {
        super(parent);
    }

    @Override
    public State<Context> processChar(final Context context, final char token) {
        switch (expecting) {
            case EQUALS:
                if (token != '=') {
                    throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "="));
                }
                expecting = Expecting.SWITCH;
                return this;
            case SWITCH:
                if (token != '{') {
                    throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "{"));
                }
                expecting = Expecting.CLOSING;
                return this;
            case CLOSING:
                switch (token) {
                    case '{':
                        ++rightCount;
                        return this;
                    case '}':
                        if (--rightCount == 0) {
                            reset();
                            return parent;
                        } else {
                            return this;
                        }
                    default:
                        return this;
                }
            default:
                assert false : "Expecting unknown token.";
                return this;
        }
    }

    @Override
    public State<Context> processNumber(final Context context, final double number) {
        switch (expecting) {
            case EQUALS:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, number, '='));
            case SWITCH:
                reset();
                return parent;
            case CLOSING:
                return this;
            default:
                assert false : "Expecting unknown token.";
                return this;
        }
    }

    @Override
    public State<Context> processWord(final Context context, final String word) {
        switch (expecting) {
            case EQUALS:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, '='));
            case SWITCH:
                reset();
                return parent;
            case CLOSING:
                return this;
            default:
                assert false : "Expecting unknown token.";
                return this;
        }
    }

    @Override
    protected void reset() {
        rightCount = 1;
        expecting = Expecting.EQUALS;
    }
}
