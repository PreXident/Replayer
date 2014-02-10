package com.paradoxplaza.eu4.replayer.parser;

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
            boolean eof = false;
            do {
                updateProgress(stream.getPosition(), size);
                int token = tokenizer.nextToken();
                switch (token) {
                    case StreamTokenizer.TT_EOF:
                        eof = true;
                        break;
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_NUMBER:
                        state = state.processNumber(context, tokenizer.nval);
                        break;
                    case StreamTokenizer.TT_WORD:
                        state = state.processWord(context, tokenizer.sval);
                        break;
                    default:
                        if (token == '"') {
                            state = state.processWord(context, tokenizer.sval);
                        } else {
                            state = state.processChar(context, (char) token);
                        }
                }
                ++counter;
            } while (!eof && !isCancelled());
            state.end(context);
        } catch (IOException e) {
            final IOException newEx = new IOException(
                    String.format("Encountered IOException on line %1$d when processing token number %2$d:\n", tokenizer.lineno(), counter), e);
            newEx.printStackTrace();
            throw newEx;
        } catch (Exception e) {
            final RuntimeException newEx = new RuntimeException(
                    String.format("Encountered exception on line %1$d when processing token number %2$d:\n", tokenizer.lineno(), counter), e);
            newEx.printStackTrace();
            throw newEx;
        }
        return context;
    }
}
