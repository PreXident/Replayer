package com.paradoxplaza.eu4.replayer.parser.colregion;

import com.paradoxplaza.eu4.replayer.ColRegionInfo;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parses color={ NUM NUM NUM }.
 */
public class Provinces extends CompoundState<Map<String, ColRegionInfo>> {

    /** Colonial region name. */
    String name;

    /** Province ids.  */
    final Set<String> provinces =  new HashSet<>();

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Provinces(final State<Map<String, ColRegionInfo>> parent) {
        super(parent);
    }

    /**
     * Sets name of the colonial region.
     * @param name new name
     * @return this
     */
    public Provinces withName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    protected final void compoundReset() {
        //value could be null because reset() is called in super constructor
        if (provinces != null) {
            provinces.clear();
        }
    }

    @Override
    protected void endCompound(final Map<String, ColRegionInfo> context) {
        final ColRegionInfo info = new ColRegionInfo(name, provinces);
        context.put(name, info);
    }

    @Override
    public State<Map<String, ColRegionInfo>> processWord(final Map<String, ColRegionInfo> context, final String word) {
        provinces.add(word);
        return this;
    }
}
