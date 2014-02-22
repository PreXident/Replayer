package com.paradoxplaza.eu4.replayer.parser;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.PositionInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import javafx.concurrent.Task;

/**
 * Parser of text save games of EU4.
 */
public class TextParser<Context> extends Task<Context> {

    /** State of the parser. */
    com.paradoxplaza.eu4.replayer.parser.State<Context> state;

    /** Parsing context. */
    final Context context;

    /** Total size of input stream. */
    final long size;

    /** Underlying input stream. */
    final PositionInputStream stream;

    /** Tokenizer used for parsing. */
    final StreamTokenizer tokenizer;

    /** Flag indicating the parser encountered eof.  */
    boolean eof = false;

    /**
     * Only contructor for the TextParser class.
     * @param context output context
     * @param start starting stare
     * @param size size of parsed file
     * @param input input stream to parse
     */
    public TextParser(final Context context, final StartAdapter<Context> start, final long size, final InputStream input) {
        this.context = context;
        state = start;
        this.size = size;
        this.stream = new PositionInputStream(input);
        tokenizer = start.createTokenizer(stream);
    }

    /**
     * Returns processed context.
     * @return processed context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Parses the input.
     * @return context
     * @throws IOException if any IO error occurs
     */
    @Override
    protected final Context call() throws IOException {
        int counter = 0;
        try {
            do {
                updateProgress(stream.getPosition(), size);
                int token = tokenizer.nextToken();
                switch (token) {
                    case StreamTokenizer.TT_EOF:
                        encounterTT_EOF(token);
                        break;
                    case StreamTokenizer.TT_EOL:
                        encounterTT_EOL(token);
                        break;
                    case StreamTokenizer.TT_NUMBER:
                        encounterTT_NUMBER(token);
                        break;
                    case StreamTokenizer.TT_WORD:
                        encounterTT_WORD(token);
                        break;
                    default:
                        encounterDEFAULT(token);
                        break;
                }
                ++counter;
            } while (!eof && !isCancelled());
            state.end(context);
        } catch (Exception e) {
            final RuntimeException newEx = new RuntimeException(
                    String.format(l10n("parser.exception"), tokenizer.lineno(), counter), e);
            newEx.printStackTrace();
            throw newEx;
        }
        return context;
    }

    /**
     * Called when TT_EOF is encountered during parsing.
     * @param token encountered token
     */
    protected void encounterTT_EOF(int token) {
        eof = true;
    }

    /**
     * Called when TT_EOL is encountered during parsing.
     * @param token encountered token
     */
    protected void encounterTT_EOL(int token) {
        //nothing special
    }

    /**
     * Called when TT_NUMBER is encountered during parsing.
     * @param token encountered token
     */
    protected void encounterTT_NUMBER(int token) {
        state = state.processNumber(context, tokenizer.nval);
    }

    /**
     * Called when TT_WORD is encountered during parsing.
     * @param token encountered token
     */
    protected void encounterTT_WORD(int token) {
        state = state.processWord(context, tokenizer.sval);
    }

    /**
     * Called when TT_WORD is encountered during parsing.
     * @param token encountered token
     */
    protected void encounterDEFAULT(int token) {
        if (token == '"') {
            state = state.processWord(context, tokenizer.sval);
        } else {
            state = state.processChar(context, (char) token);
        }
    }
}
