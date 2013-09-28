package com.paradoxplaza.eu4.replayer.parser.culture;

import com.paradoxplaza.eu4.replayer.CountryInfo;
import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.util.Map;

/**
 * Starting state for CulturesParser.
 */
class Start extends StartAdapter<Pair<Map<String, CountryInfo>, Map<String, Integer>>> {

    /** State processing culture groups. */
    final CultureGroup cultureGroup = new CultureGroup(this);

    @Override
    public Start end(final Pair<Map<String, CountryInfo>, Map<String, Integer>> context) {
        return this;
    }

    @Override
    public State<Pair<Map<String, CountryInfo>, Map<String, Integer>>> processChar(final Pair<Map<String, CountryInfo>, Map<String, Integer>> context, final char token) {
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, token, "RELIGION_NAME"));
    }

    @Override
    public State<Pair<Map<String, CountryInfo>, Map<String, Integer>>> processWord(final Pair<Map<String, CountryInfo>, Map<String, Integer>> context, final String word) {
        return cultureGroup.withName(word);
    }
}
