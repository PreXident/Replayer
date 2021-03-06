package com.paradoxplaza.eu4.replayer.parser.defaultmap;

import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Processes seas_start={...}.
 */
class Seas extends CompoundState<Map<String, ProvinceInfo>> {

    static final Pattern NUMBER = Pattern.compile("\\d+");

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Seas(final State<Map<String, ProvinceInfo>> parent) {
        super(parent);
    }

    @Override
    public State<Map<String, ProvinceInfo>> processWord(final Map<String, ProvinceInfo> context, final String word) {
        if (!NUMBER.matcher(word).matches()) {
            throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_VALUE), word, "NUMBER"));
        }
        final ProvinceInfo province = context.get(word);
        if (province != null) {
            province.isSea = true;
        } else {
            System.out.println(String.format(l10n("parser.unknown.province"), word));
        }
        return this;
    }
}
