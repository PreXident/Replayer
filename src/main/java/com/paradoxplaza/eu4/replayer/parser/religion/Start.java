package com.paradoxplaza.eu4.replayer.parser.religion;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.Map;

/**
 * Starting state for ReligionParser.
 */
class Start extends StartAdapter<Map<String, Integer>> {

    /** State processing religion groups. */
    final ReligionGroup religionGroup = new ReligionGroup(this);

    @Override
    public Start end(final Map<String, Integer> context) {
        return this;
    }

    @Override
    public State<Map<String, Integer>> processChar(final Map<String, Integer> context, final char token) {
        throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_VALUE), token, "RELIGION_NAME"));
    }

    @Override
    public State<Map<String, Integer>> processWord(final Map<String, Integer> context, final String word) {
        return religionGroup.withName(word);
    }
}
