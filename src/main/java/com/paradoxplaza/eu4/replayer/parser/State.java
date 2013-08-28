package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Flag;

/**
 * Represents state of the TextParser.
 */
abstract class State {

    /**
     * Returns new starting state.
     * @return new starting state
     */
    static public State newStart() {
        return new Start();
    }

    /** Intented for subclasses. Parent state to which the control should return. */
    final Start start;

    /**
     * Only constructor. Sets start field.
     * @param start
     */
    protected State(final Start start) {
        this.start = start;
    }

    /** Processes charancter and changes savegame.
     * @param saveGame saveGame to apply changes
     * @param char token from input
     * @return new state
     */
    public State processChar(final SaveGame saveGame, final char token) {
        return this;
    }

    /** Processes word and changes savegame.
     * @param saveGame saveGame to apply changes
     * @param word token from input
     * @return new state
     */
    public State processWord(final SaveGame saveGame, final String word) {
        return this;
    }

    /**
     * Represents new starting state of TextParser.
     */
    static class Start extends State {

        /** State processing dates. */
        final Date date = new Date(this);

        /** State processing flags. */
        final Flags flags = new Flags(this);

        /** State ignoring everything till matching }. */
        final Ignore ignore = new Ignore(this);

        /**
         * Only constructor. Sets start to null.
         */
        public Start() {
            super(null);
        }

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            return token == '{' ? ignore : this;
        }

        @Override
        public State processWord(final SaveGame saveGame, final String word) {
            switch (word) {
                case "date":
                    return date.withOutput(saveGame.date);
                case "start_date":
                    return date.withOutput(saveGame.startDate);
                case "flags":
                    return flags;
                default:
                    return this;
            }
        }
    }

    /**
     * Processes dates in format xxx=Y.M.D.
     * It's mimicking non-static inner class because of need to declare enums.
     */
    static class Date extends State {

        /** What tokens can be expected. */
        enum Expecting { EQUALS, DATE }

        /** What token is expected now. */
        Expecting expecting = Expecting.EQUALS;

        /** Date where to set the value. */
        com.paradoxplaza.eu4.replayer.Date output;

        /**
         * Only constructor.
         * @param start parent state
         */
        public Date(final Start start) {
            super(start);
        }

        /**
         * Sets output date to given value.
         * @param output where to store output value
         * @return this
         */
        public Date withOutput(final com.paradoxplaza.eu4.replayer.Date output) {
            this.output = output;
            return this;
        }

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            if (expecting == Expecting.DATE) {
                throw new RuntimeException(String.format("Invalid character \"%1$s\" after equals, expected date", token));
            }
            if (token == '=') {
                expecting = Expecting.DATE;
                return this;
            } else {
                throw new RuntimeException(String.format("Invalid character \"%1$s\" after date, expected \"=\"", token));
            }
        }

        @Override
        public State processWord(final SaveGame saveGame, final String word) {
            if (expecting == Expecting.EQUALS) {
                throw new RuntimeException(String.format("Invalid word \"%1$s\" after date, expected \"=\"", word));
            }
            output.setDate(word);
            expecting = Expecting.EQUALS;
            return start;
        }
    }

    /**
     * Ignores everything till matching "}".
     * It's mimicking non-static inner class because of need to declare enums.
     */
    static class Ignore extends State {

        /** Number of opened "{". */
        int rightCount = 1;

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
                        rightCount = 1;
                        return start;
                    } else {
                        return this;
                    }
                default:
                    return this;
            }
        }
    }

    /**
     * Starts processing global flags.
     * It's mimicking non-static inner class because of need to declare enums.
     */
    static class Flags extends State {

        /** What tokens can be expected. */
        enum Expecting {
            EQUALS {
                @Override
                public String toString() {
                    return "=";
                }
            },
            OPENING {
                @Override
                public String toString() {
                    return "}";
                }
            }
        };

        /** What token is expected. */
        Expecting expecting = Expecting.EQUALS;

        /** State for processing individual flags. */
        final InFlags inFlags;

        /**
         * Only constructor.
         * @param start parent state
         */
        public Flags(final Start start) {
            super(start);
            inFlags = new InFlags(start);
        }

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            if (expecting == Expecting.OPENING) {
                if (token == '{') {
                    return inFlags;
                } else {
                    throw new RuntimeException(String.format("Invalid character \"%1$s\" after equals, expected {", token));
                }
            } else {
                if (token == '=') {
                    expecting = Expecting.OPENING;
                    return this;
                } else {
                    throw new RuntimeException(String.format("Invalid character \"%1$s\" after date, expected \"=\"", token));
                }
            }
        }

        @Override
        public State processWord(final SaveGame saveGame, final String word) {
            throw new RuntimeException(String.format("Invalid word \"%1$s\", expected \"%2$s\"", word, expecting));
        }

        /**
         * Processes individual flags.
         */
        static class InFlags extends State {

            /** What tokens can be expected. */
            enum Expecting {
                IDENT, EQUALS, DATE
            }

            /** What token is expected. */
            Expecting expecting = Expecting.IDENT;

            /** Identifier of currently processed flag. */
            String flag;

            public InFlags(final Start start) {
                super(start);
            }

            @Override
            public State processChar(final SaveGame saveGame, final char token) {
                switch (expecting) {
                    case EQUALS:
                        expecting = Expecting.DATE;
                        return this;
                    case DATE:
                        throw new RuntimeException(String.format("Invalid character \"%1$s\" after equals, expected date", token));
                    case IDENT:
                        if (token == '}') {
                            return start;
                        } else {
                            throw new RuntimeException(String.format("Invalid character \"%1$s\" in flags, expected \"}\"", token));
                        }
                    default:
                        assert false : "Expecting invalid token in flags!";
                        return this;
                }
            }

            @Override
            public State processWord(final SaveGame saveGame, final String word) {
                switch (expecting) {
                    case EQUALS:
                        throw new RuntimeException(String.format("Invalid word \"%1$s\" after flag, expected \"=\"", word));
                    case DATE:
                        saveGame.addEvent(new com.paradoxplaza.eu4.replayer.Date(word), new Flag(flag));
                        expecting = Expecting.IDENT;
                        return this;
                    case IDENT:
                        flag = word;
                        expecting = Expecting.EQUALS;
                        return this;
                    default:
                        assert false : "Expecting invalid token in flags!";
                        return this;
                }
            }
        }
    }
}
