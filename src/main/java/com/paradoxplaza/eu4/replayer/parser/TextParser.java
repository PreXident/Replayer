package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;
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
    State state = State.newStart();

    /** Processed savegame. */
    SaveGame saveGame = new SaveGame();

    /**
     * Only contructor for the TextParser class.
     */
    public TextParser() {
        //nothing
    }

    /**
     * Returns processed save game.
     * @return processed save game
     */
    public SaveGame getSaveGame() {
        return saveGame;
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
        s.wordChars('-', '-');
        s.wordChars(128, Integer.MAX_VALUE);
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
                    case StreamTokenizer.TT_EOL:
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
            state.end(saveGame);
        } catch (IOException e) {
            throw new IOException(
                    String.format("Encountered IOException on line %1$d when processing token number %2$d:\n", s.lineno(), counter),
                    e);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Encountered exception on line %1$d when processing token number %2$d:\n", s.lineno(), counter),
                    e);
        }
    }
}
