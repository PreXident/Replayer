package com.paradoxplaza.eu4.replayer.parser.colregion;

import com.paradoxplaza.eu4.replayer.ColRegionInfo;
import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.util.Map;

/**
 * Parses COLONIAL_REGION_NAME={...}.
 */
class ColRegion extends CompoundState<Map<String, ColRegionInfo>> {

    /** Colonial region name. */
    String name;

    /** Parses religions. */
    final Provinces provinces = new Provinces(this);

    /** Ignores uninteresting info. */
    Ignore<Map<String, ColRegionInfo>> ignore = new Ignore<>(this);

    /**
     * Only constructor.
     * @param parent parent state
     */
    public ColRegion(final State<Map<String, ColRegionInfo>> parent) {
        super(parent);
    }

    /**
     * Sets name of the colonial region.
     * @param name new name
     * @return this
     */
    public ColRegion withName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public State<Map<String, ColRegionInfo>> processWord(final Map<String, ColRegionInfo> context, final String word) {
        switch (word) {
            case "provinces":
                return provinces.withName(name);
            default:
                return ignore;
        }
    }
}
