package com.paradoxplaza.eu4.replayer.parser;

import java.io.InputStream;

/**
 * Parser of text files with non-standard eol comments.
 */
public class EOLCommentParser<Context> extends TextParser<Context> {

    /** Flag indicating the parser is inside a eol-comment. */
    boolean inComment = false;

    final String startComment;

    /**
     * Only contructor for the EOLCommentParser class.
     * @param context output context
     * @param start starting stare
     * @param size size of parsed file
     * @param input input stream to parse
     */
    public EOLCommentParser(final Context context, final StartAdapter<Context> start, final long size, final InputStream input, final String startComment) {
        super(context, start, size, input);
        this.startComment = startComment;
    }

    @Override
    protected void encounterTT_EOL(int token) {
        //if eof is significant, TT_EOL is returned when eof is encountered
        if (token < 0) {
            eof = true;
        }
        inComment = false;
        tokenizer.eolIsSignificant(false);
        super.encounterTT_EOL(token);
    }

    @Override
    protected void encounterTT_NUMBER(int token) {
        if (!inComment) {
            super.encounterTT_NUMBER(token);
        }
    }

    @Override
    protected void encounterTT_WORD(int token) {
        if (!inComment) {
            if (tokenizer.sval.startsWith(startComment)) {
                inComment = true;
                tokenizer.eolIsSignificant(true);
            } else {
                super.encounterTT_WORD(token);
            }
        }
    }

    @Override
    protected void encounterDEFAULT(int token) {
        if (!inComment) {
            super.encounterDEFAULT(token);
        }
    }
}
