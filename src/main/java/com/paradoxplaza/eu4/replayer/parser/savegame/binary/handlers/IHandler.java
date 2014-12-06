package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import java.io.IOException;

/**
 * Handles token from binary stream.
 */
public interface IHandler {

    /**
     * Handles token in binary save context.
     * @param context binary save context
     * @throws IOException if any IO error occurs
     */
    void handleToken(IParserContext context) throws IOException;

    /**
     * Handler gets notified that handling is over.
     */
    void handled();
}
