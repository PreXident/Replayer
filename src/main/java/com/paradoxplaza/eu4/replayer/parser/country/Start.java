package com.paradoxplaza.eu4.replayer.parser.country;

import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Starting state for CulturesParser.
 */
class Start extends StartAdapter<Ref<Integer>> {

    /** State processing culture groups. */
    final Color countryColor = new Color(this);

    /** State ignoring uninteresting info. */
    final Ignore<Ref<Integer>> ignore = new Ignore<>(this);

    @Override
    public Start end(final Ref<Integer> context) {
        return this;
    }

    @Override
    public State<Ref<Integer>> processChar(final Ref<Integer> context, final char token) {
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, token, "color"));
    }

    @Override
    public State<Ref<Integer>> processWord(final Ref<Integer> context, final String word) {
        if (word.equals("color")) {
            return countryColor;
        } else  {
            return ignore;
        }
    }
}
