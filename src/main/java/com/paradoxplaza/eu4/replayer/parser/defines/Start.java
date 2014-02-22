package com.paradoxplaza.eu4.replayer.parser.defines;

import com.paradoxplaza.eu4.replayer.DefinesInfo;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.Map;

/**
 * Starting state for {@link ColRegionParser}.
 */
class Start extends StartAdapter<DefinesInfo> {

    /** Processes NDefines. */
    final Defines defines = new Defines(this);

    @Override
    public Start end(final DefinesInfo context) {
        return this;
    }

    @Override
    public State<DefinesInfo> processChar(final DefinesInfo context, final char token) {
        throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_VALUE), token, "NDefines"));
    }

    @Override
    public State<DefinesInfo> processWord(final DefinesInfo context, final String word) {
        if (word.equals("NDefines")) {
            return defines;
        }
        throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_VALUE), word, "NDefines"));
    }
}
