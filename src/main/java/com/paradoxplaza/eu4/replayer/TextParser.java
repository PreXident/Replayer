package com.paradoxplaza.eu4.replayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * Parser of text save games of EU4.
 */
public class TextParser {

    /** State of the parser. */
    State state = State.START;

    SaveGame saveGame = new SaveGame();

    /**
     * Only contructor for the TextParser class.
     */
    public TextParser() {
    }

    /**
     * Creates tokenizer for parsing save games.
     * @param input input stream with save game
     * @return tokenizer ready to be used
     */
    StreamTokenizer createTokenizer(final InputStream input) {
        final StreamTokenizer s = new StreamTokenizer(new BufferedReader(new InputStreamReader(input)));
        s.resetSyntax();
        s.commentChar('#');
        s.eolIsSignificant(false);
        s.lowerCaseMode(false);
        s.wordChars('.', '.');
        s.wordChars('0', '9');
        s.wordChars('a', 'z');
        s.wordChars('A', 'Z');
        s.wordChars('_', '_');
        s.whitespaceChars('\t', '\t');
        s.whitespaceChars(' ', ' ');
        s.whitespaceChars('\n', '\n');
        s.whitespaceChars('\r', '\r');
        s.quoteChar('"');
        return s;
    }

    /**
     * Parses the input stream with save game.
     * @param input
     */
    public void parse(final InputStream input) throws IOException {
        final StreamTokenizer s = createTokenizer(input);
        int counter = 0;
        try {
            boolean eof = false;
            do {
                int token = s.nextToken();
                switch (token) {
                    case StreamTokenizer.TT_EOF:
                        eof = true;
                        break;
                    case StreamTokenizer.TT_WORD:
                        state = state.processWord(saveGame, s.sval);
                        break;
                    default:
                        if (token == '"') {
                            state = state.processWord(saveGame, s.sval);
                        } else {
                            state = state.processChar(saveGame, (char) token);
                        }
                }
                ++counter;
            } while (!eof);
        } catch (IOException e) {
            throw new IOException(
                    String.format("Encountered IOException on line %1$d when processing token number %2$d:\n", s.lineno(), counter),
                    e);
        } catch (Exception e) {
            throw new IOException(
                    String.format("Encountered exception on line %1$d when processing token number %2$d:\n", s.lineno(), counter),
                    e);
        }
    }

    /** Represents state of the TextParser. */
    abstract static class State {
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
                if (token == '=') {
                    afterEquals = true;
                } else {
                    throw new RuntimeException(String.format("Invalid character \"%1$s\" after date, expected \"=\"", token));
                }
                return this;
            }

            @Override
            public State processWord(final SaveGame saveGame, final String word) {
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
    }
}
