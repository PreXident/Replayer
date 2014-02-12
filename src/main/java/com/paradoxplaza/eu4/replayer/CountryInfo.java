package com.paradoxplaza.eu4.replayer;

import java.util.HashSet;
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

    /** Date the country will be created by tag change. */
    public Date expectingTagChange = null;

    /** Set of subject nations' tags. */
    public final Set<String> subjects = new HashSet<>();

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
        expectingTagChange = null;
        overlord = null;
        subjects.clear();
    }
}
