package com.paradoxplaza.eu4.replayer.parser.religion;

import java.util.Map;

import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * StartAdapter state for ReligionParser.
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
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, token, "RELIGION_NAME"));
    }

    @Override
    public State<Map<String, Integer>> processWord(final Map<String, Integer> context, final String word) {
        return religionGroup.withName(word);
    }
}
