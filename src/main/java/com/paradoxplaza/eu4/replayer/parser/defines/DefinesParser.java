package com.paradoxplaza.eu4.replayer.parser.defines;

import com.paradoxplaza.eu4.replayer.DefinesInfo;
import com.paradoxplaza.eu4.replayer.parser.EOLCommentParser;
import java.io.InputStream;

/**
 * Parses common/defines.lua.
 */
public class DefinesParser extends EOLCommentParser<DefinesInfo> {

    /**
     * Only constructor.
     * @param context input provinces to update, output colonial regions info
     * @param size input size
     * @param input input stream to parse
     */
    public DefinesParser(final DefinesInfo context, final long size, final InputStream input) {
        super(context, new Start(), size, input, "--");
    }
}
