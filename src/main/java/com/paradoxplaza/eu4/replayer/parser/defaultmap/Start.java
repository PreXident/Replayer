package com.paradoxplaza.eu4.replayer.parser.defaultmap;

import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.Map;

/**
 * Starting state of default.map parser.
 */
public class Start extends StartAdapter<Map<String, ProvinceInfo>> {

    /** State ignoring everything till matching }. */
    final Ignore<Map<String, ProvinceInfo>> ignore = new Ignore<>(this);

    /** Processes seas_start. */
    final Seas seas = new Seas(this);

    @Override
    public Start end(final Map<String, ProvinceInfo> context) {
        return this;
    }

    @Override
    public State<Map<String, ProvinceInfo>> processChar(final Map<String, ProvinceInfo> context, final char token) {
        throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_KEYWORD), token, "sea_starts|lakes"));
    }

    @Override
    public State<Map<String, ProvinceInfo>> processWord(final Map<String, ProvinceInfo> context, final String word) {
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
