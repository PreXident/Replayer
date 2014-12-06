package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Deque;

/**
 * Context during transforming binary save to textual save.
 */
public interface IParserContext {

    /**
     * Returns input stream. At least 4 byte window.
     * @return input stream
     */
    PushbackInputStream getInputStream();

    /**
     * Returns output stream.
     * @return outputstream
     */
    OutputStream getOutputStream();

    /**
     * Returns token context.
     * @return token context
     */
    Deque<Token> getContext();

    /**
     * Returns last processed token.
     * @return last processed token
     */
    Token getLastToken();

    /**
     * Returns current token.
     * @return current token
     */
    Token getCurrentToken();

    /**
     * Returns pretty print flag
     * @return pretty print flag
     */
    boolean getPrettyPrint();

    /**
     * Sets pretty print flag.
     * @param prettyPrint new pretty print flag
     */
    void setPrettyPrint(boolean prettyPrint);

    /**
     * Returns indentation level.
     * @return indentation level
     */
    int getIndent();

    /**
     * Increases indentation level.
     * @return new indentation level
     */
    int increaseIndent();

    /**
     * Decreases indentation level.
     * @return new indendation level
     */
    int decreaseIndent();
}
