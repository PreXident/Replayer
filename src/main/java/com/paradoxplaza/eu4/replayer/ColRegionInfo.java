package com.paradoxplaza.eu4.replayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple representation of colonial region.
 */
public class ColRegionInfo {

    /** Region name. */
    final String name;

    /** Provinces belonging to this region. */
    public final Set<String> provinces = new HashSet<>();

    /**
     * Only constructor.
     * @param name region name
     * @param provincesIds provinces belonging to this region
     */
    public ColRegionInfo(final String name, final Collection<String> provincesIds) {
        this.name = name;
        this.provinces.addAll(provincesIds);
    }
}
