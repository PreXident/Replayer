package com.paradoxplaza.eu4.replayer.parser.colregion;

import com.paradoxplaza.eu4.replayer.ColRegionInfo;
import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.Map;

/**
 * Starting state for {@link ColRegionParser}.
 */
class Start extends StartAdapter<Map<String, ColRegionInfo>> {

    /** State processing colonial regions. */
    final ColRegion colRegion = new ColRegion(this);

    @Override
    public Start end(final Map<String, ColRegionInfo> context) {
        return this;
    }

    @Override
    public State<Map<String, ColRegionInfo>> processChar(final Map<String, ColRegionInfo> context, final char token) {
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, token, "COLONIAL_REGION_NAME"));
    }

    @Override
    public State<Map<String, ColRegionInfo>> processWord(final Map<String, ColRegionInfo> context, final String word) {
        return colRegion.withName(word);
    }
}
