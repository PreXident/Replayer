/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Flag;

/** Represents state of the TextParser. */
abstract class State {
    
    /** Starting state. */
    static State START = new Start();

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

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            return token == '{' ? new Ignore() : this;
        }

        @Override
        public State processWord(final SaveGame saveGame, final String word) {
            switch (word) {
                case "date":
                    return new Date(saveGame.date);
                case "start_date":
                    return new Date(saveGame.startDate);
                case "flags":
                    return new Flags();
                default:
                    return this;
            }
        }
    }

    /**
     * Processes dates in format xxx=Y.M.D.
     */
    static class Date extends State {

        /** Flag if equals has been encountered. */
        boolean afterEquals = false;
        /** Date where to set the value. */
        com.paradoxplaza.eu4.replayer.Date date;

        /**
         * Only constructor.
         * @param date where to store extracted value
         */
        public Date(final com.paradoxplaza.eu4.replayer.Date date) {
            this.date = date;
        }

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            if (afterEquals) {
                throw new RuntimeException(String.format("Invalid character \"%1$s\" after equals, expected date", token));
            }
            if (token == '=') {
                afterEquals = true;
                return this;
            } else {
                throw new RuntimeException(String.format("Invalid character \"%1$s\" after date, expected \"=\"", token));
            }
        }

        @Override
        public State processWord(final SaveGame saveGame, final String word) {
            if (!afterEquals) {
                throw new RuntimeException(String.format("Invalid word \"%1$s\" after date, expected \"=\"", word));
            }
            date.setDate(word);
            return State.START;
        }
    }

    /**
     * Ignores everything till matching "}".
     */
    static class Ignore extends State {

        /** Number of opened "{". */
        int rightCount = 1;

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            switch (token) {
                case '{':
                    ++rightCount;
                    return this;
                case '}':
                    return --rightCount == 0 ? State.START : this;
                default:
                    return this;
            }
        }
    }

    /**
     * Starts processing global flags.
     */
    static class Flags extends State {

        boolean afterEquals = false;

        @Override
        public State processChar(final SaveGame saveGame, final char token) {
            if (afterEquals) {
                if (token == '{') {
                    return new InFlags();
                } else {
                    throw new RuntimeException(String.format("Invalid character \"%1$s\" after equals, expected {", token));
                }
            } else {
                if (token == '=') {
                    afterEquals = true;
                    return this;
                } else {
                    throw new RuntimeException(String.format("Invalid character \"%1$s\" after date, expected \"=\"", token));
                }
            }
        }

        @Override
        public State processWord(final SaveGame saveGame, final String word) {
            switch (word) {
                case "date":
                    return new Date(saveGame.date);
                case "start_date":
                    return new Date(saveGame.startDate);
                case "flags":
                    return new Flags();
                default:
                    return this;
            }
        }

        static class InFlags extends State {

            enum Expecting {

                IDENT, EQUALS, DATE
            }
            Expecting expecting = Expecting.IDENT;
            String flag;

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
                            return State.START;
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
