package com.paradoxplaza.eu4.replayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains information about country.
 */
public class CountryInfo {

    /** Country TAG. */
    public final String tag;

    /** Country color. */
    public final int color;

    /** Country's overlord. */
    public String overlord = null;

    /** Set of provinces controlled by this country. */
    public final Set<String> controls = new HashSet<>();

    /** Set of provinces owned by this country. */
    public final Set<String> owns = new HashSet<>();

    /** Country tags changing to this country. */
    public List<String> tagChangeFrom = new ArrayList<>();

    /** Set of subject nations' tags. */
    public final Set<String> subjects = new HashSet<>();

    /** Administration technology level. */
    public int adm = 0;

    /** Diplomatic technology level. */
    public int dip = 0;

    /** Military technology level. */
    public int mil = 0;

    /**
     * Only contructor.
     * @param tag country tag
     * @param color country color
     */
    public CountryInfo(final String tag, final int color) {
        this.tag = tag;
        this.color = color;
    }

    /**
     * Resets the country for new replay.
     */
    public void reset() {
        controls.clear();
        owns.clear();
        tagChangeFrom.clear();
        overlord = null;
        subjects.clear();
        adm = 0;
        dip = 0;
        mil = 0;
    }
}
