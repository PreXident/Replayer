package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Flag;
import com.paradoxplaza.eu4.replayer.events.NewEmperor;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Represents state of the TextParser.
 */
abstract class State {

    /** Error message. */
    static final String INVALID_TOKEN_EXPECTED_VALUE = "Invalid token \"%1$s\" after date, expected %2$s";

    /** Error message. */
    static final String INVALID_TOKEN_EXPECTED_KEYWORD = "Invalid token \"%1$s\" after date, expected \"%2$s\"";

    /**
     * Returns new starting state.
     * @return new starting state
     */
    static public State newStart() {
        return new Start();
    }

    /** Intented for subclasses. Parent state to which the control should return. */
    final State start;

    /**
     * Only constructor. Sets start field.
     * @param start
     */
    protected State(final State start) {
        this.start = start;
        reset();
    }

    /**
     * Processes end of file.
     * @param saveGame SaveGame to apply changes
     * @return new state
     * @throws RuntimeException if end of file was unexpected or some info is missing
     */
    public State end(final SaveGame saveGame) {
        throw new RuntimeException("Unexpected end of file!");
    }

    /** Processes charancter and changes savegame.
     * @param saveGame SaveGame to apply changes
     * @param char token from input
     * @return new state
     */
    public State processChar(final SaveGame saveGame, final char token) {
        return this;
    }

    /** Processes word and changes savegame.
     * @param saveGame SaveGame to apply changes
     * @param word token from input
     * @return new state
     */
    public State processWord(final SaveGame saveGame, final String word) {
        return this;
    }

    /**
     * Resets inner state. Intented to be overridden.
     */
    protected void reset() {
        //nothing to reset
    }

    /**
     * Represents new starting state of TextParser.
     */
    static class Start extends State {

        /** State processing dates. */
        final DateState date = new DateState(this);

        /** State processing flags. */
        final Flags flags = new Flags(this);

        /** State ignoring everything till matching }. */
        final Ignore ignore = new Ignore(this);

        /** State processing emperors. */
        final Emperor emperor = new Emperor(this);

        /** Current date in save game. */
        Ref<Date> currentDate = new Ref<>();

        /** Save game's starting date. */
        Ref<Date> startDate = new Ref<>();

        /**
         * Only constructor. Sets start to null.
         */
        public Start() {
            super(null);
        }

        @Override
        public State end(final SaveGame saveGame) {
            if (currentDate.val == null) {
                throw new RuntimeException("Current date was not set!");
            }
            if (startDate.val == null) {
                throw new RuntimeException("Start date was not set!");
            }
            saveGame.date = currentDate.val;
            saveGame.startDate = startDate.val;
            return this;
        }

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            return token == '{' ? ignore : this;
        }

        @Override
        public State processWord(final SaveGame saveGame, final String word) {
            switch (word) {
                case "date":
                    return date.withOutput(currentDate);
                case "start_date":
                    return date.withOutput(startDate);
                case "flags":
                    return flags;
                case "old_emperor":
                    return emperor;
                default:
                    return this;
            }
        }

        @Override
        protected void reset() {
            if (currentDate != null) {
                currentDate.setVal(null);
            }
            if (startDate != null) {
                startDate.setVal(null);
            }
        }
    }

    /**
     * Parent of states processing xxx=VALUE.
     * @param <T> final value type
     */
    static abstract class ValueState<T> extends State {

        /** What tokens can be expected. */
        enum Expecting { EQUALS, VALUE }

        /** What token is expected now. */
        Expecting expecting;

        /** Where to set the value. */
        Ref<T> output;

        /**
         * Only constructor.
         * @param start parent state
         */
        public ValueState(final State start) {
            super(start);
        }

        /**
         * Sets output to given reference.
         * @param output where to store output value
         * @return this
         */
        public ValueState<T> withOutput(final Ref<T> output) {
            this.output = output;
            return this;
        }

        protected abstract T createOutput(final String word);

        @Override
        public final State processChar(final SaveGame saveGame, final char token) {
            switch (expecting) {
                case EQUALS:
                    if (token == '=') {
                        expecting = Expecting.VALUE;
                        return this;
                    } else {
                        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "="));
                    }
                case VALUE:
                    throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, token, "date"));
                default:
                    assert false : "Expecting unknown token";
                    return this;
            }
        }

        @Override
        public final State processWord(final SaveGame saveGame, final String word) {
            switch (expecting) {
                case EQUALS:
                    throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, "="));
                case VALUE:
                    output.val = createOutput(word);
                    reset();
                    return start;
                default:
                    assert false : "Expecting unknown token";
                    return this;
            }
        }

        @Override
        protected final void reset() {
            expecting = Expecting.EQUALS;
        }
    }

    /**
     * Processes dates in format xxx=Y.M.D.
     */
    static class DateState extends ValueState<Date> {

        /**
         * Only constructor.
         * @param start parent state
         */
        public DateState(final State start) {
            super(start);
        }

        @Override
        public DateState withOutput(final Ref<Date> output) {
            return (DateState) super.withOutput(output);
        }

        @Override
        protected Date createOutput(final String word) {
            return new Date(word);
        }
    }

    /**
     * Processes string values in format xxx=STRING.
     */
    static class StringState extends ValueState<String> {

        /**
         * Only constructor.
         * @param start parent state
         */
        public StringState(final State start) {
            super(start);
        }

        @Override
        public StringState withOutput(final Ref<String> output) {
            return (StringState) super.withOutput(output);
        }

        @Override
        protected String createOutput(final String word) {
            return word;
        }
    }

    /**
     * Ignores everything till matching "}".
     */
    static class Ignore extends State {

        /** Number of opened "{". */
        int rightCount;

        /**
         * Only constructor.
         * @param start parent state
         */
        public Ignore(final Start start) {
            super(start);
        }

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            switch (token) {
                case '{':
                    ++rightCount;
                    return this;
                case '}':
                    if (--rightCount == 0) {
                        reset();
                        return start;
                    } else {
                        return this;
                    }
                default:
                    return this;
            }
        }

        @Override
        protected void reset() {
            rightCount = 1;
        }
    }

    /**
     * Processes compound values in format xxx = { COMPOUND }.
     */
    static abstract class CompoundState extends State {

        /** What tokens can be expected. */
        enum Expecting {

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

            public abstract char toChar();
        };

        /** What token is expected. */
        Expecting expecting;

        /**
         * Only constructor.
         * @param start parent state
         */
        public CompoundState(final Start start) {
            super(start);
        }

        /**
         * Informs descendant that element is finished.
         * @param saveGame
         */
        protected abstract void endCompound(final SaveGame saveGame);

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            if (token != expecting.toChar()) {
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, expecting));
            }
            switch (expecting) {
                case OPENING:
                    expecting = Expecting.CLOSING;
                    return this;
                case EQUALS:
                    expecting = Expecting.OPENING;
                    return this;
                case CLOSING:
                    endCompound(saveGame);
                    reset();
                    return start;
                default:
                    assert false : "Expecting unknown token";
                    return this;
            }
        }

        @Override
        protected void reset() {
            expecting = Expecting.EQUALS;
        }
    }

    /**
     * Processes global flags.
     */
    static class Flags extends CompoundState {

        /** Identifier of currently processed flag. */
        String flag;

        /** Date of currently processed flag. */
        Ref<Date> date = new Ref<>();

        /** State to process individual flags. */
        DateState inFlags = new DateState(this).withOutput(date);

        /**
         * Only constructor.
         * @param start parent state
         */
        public Flags(final Start start) {
            super(start);
        }

        /**
         * If any flag is processed, it's inserted into saveGame.
         * @param saveGame SaveGame to modify
         */
        @Override
        protected void endCompound(final SaveGame saveGame) {
            if (flag != null && date.val != null) {
                saveGame.addEvent(date.val, new Flag(flag));
            }
        }

        @Override
        public State processWord(final SaveGame saveGame, final String word) {
            endCompound(saveGame);
            flag = word;
            return inFlags;
        }

        @Override
        protected final void reset() {
            super.reset();
            flag = null;
            if (date != null) {
                date.val = null;
            }
        }
    }

    static class Emperor extends CompoundState {

        Ref<String> id = new Ref<>();
        Ref<String> tag = new Ref<>();
        Ref<Date> date = new Ref<>();

        DateState dateState = new DateState(this).withOutput(date);
        StringState stringState = new StringState(this);

        public Emperor(final Start start) {
            super(start);
        }

        /**
         * If any flag is processed, it's inserted into saveGame.
         * @param saveGame SaveGame to modify
         */
        @Override
        protected void endCompound(final SaveGame saveGame) {
            if (id.val != null && tag.val != null && date.val != null) {
                saveGame.addEvent(date.val, new NewEmperor(id.val, tag.val));
            } else {
                throw new RuntimeException("Incomplete old_emperor!");
            }
        }

        @Override
        public State processWord(final SaveGame saveGame, final String word) {
            if (expecting != Expecting.CLOSING) {
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, expecting));
            }
            switch(word) {
                case "id":
                    return stringState.withOutput(id);
                case "country":
                    return stringState.withOutput(tag);
                case "date":
                    return dateState;
                default:
                    throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, "id|country|date"));
            }
        }

        @Override
        protected void reset() {
            super.reset();
            if (id != null) {
                id.val = null;
            }
            if (tag != null) {
                tag.val = null;
            }
            if (date != null) {
                date.val = null;
            }
        }
    }
}
