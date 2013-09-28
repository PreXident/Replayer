package com.paradoxplaza.eu4.replayer.parser.defaultmap;

import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.util.Map;
import java.util.Set;

/**
 * Starting state of default.map parser.
 */
public class Start extends StartAdapter<Pair<Set<Integer>, Map<String, ProvinceInfo>>> {

    /** State ignoring everything till matching }. */
    final Ignore<Pair<Set<Integer>, Map<String, ProvinceInfo>>> ignore = new Ignore<>(this);

    /** Processes seas_start. */
    final Seas seas = new Seas(this);

    @Override
    public Start end(final Pair<Set<Integer>, Map<String, ProvinceInfo>> context) {
        return this;
    }

    @Override
    public State<Pair<Set<Integer>, Map<String, ProvinceInfo>>> processChar(final Pair<Set<Integer>, Map<String, ProvinceInfo>> context, final char token) {
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "sea_starts"));
    }

    @Override
    public State<Pair<Set<Integer>, Map<String, ProvinceInfo>>> processWord(final Pair<Set<Integer>, Map<String, ProvinceInfo>> context, final String word) {
        switch (word) {
            case "sea_starts":
                return seas;
            case "lakes":
                return seas;
            default:
                return ignore;
        }
    }
}
