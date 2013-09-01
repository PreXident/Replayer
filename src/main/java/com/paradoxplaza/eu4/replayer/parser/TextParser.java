package com.paradoxplaza.eu4.replayer.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;

/**
 * Parser of text save games of EU4.
 */
public class TextParser<Context> {

    /** State of the parser. */
    State<Context> state;

    /** Parsing context. */
    final Context context;

    /**
     * Only contructor for the TextParser class.
     */
    public TextParser(final Context context) {
        this.context = context;
    }

    /**
     * Returns processed context.
     * @return processed context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Parses the input stream.
     * @param input input stream to parse
     */
    public void parse(final Start<Context> start, final InputStream input) throws IOException {
        state = start;
        final StreamTokenizer t = start.createTokenizer(input);
        t.quoteChar('"');
        int counter = 0;
        try {
            boolean eof = false;
            do {
                int token = t.nextToken();
                switch (token) {
                    case StreamTokenizer.TT_EOF:
                        eof = true;
                        break;
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_NUMBER:
                        state = state.processNumber(context, t.nval);
                        break;
                    case StreamTokenizer.TT_WORD:
                        state = state.processWord(context, t.sval);
                        break;
                    default:
                        if (token == '"') {
                            state = state.processWord(context, t.sval);
                        } else {
                            state = state.processChar(context, (char) token);
                        }
                }
                ++counter;
            } while (!eof);
            state.end(context);
        } catch (IOException e) {
            throw new IOException(
                    String.format("Encountered IOException on line %1$d when processing token number %2$d:\n", t.lineno(), counter), e);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Encountered exception on line %1$d when processing token number %2$d:\n", t.lineno(), counter), e);
        }
    }
}
