package com.paradoxplaza.eu4.replayer;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains information about country.
 */
public class CountryInfo {

    /** Country TAG. */
    final String tag;

    /** Country color. */
    final int color;

    /** Set of provinces controlled by this country. */
    final Set<String> controls = new HashSet<>();

    /** Set of provinces owned by this country. */
    final Set<String> owns = new HashSet<>();

    /** Flag indicating whether the country will be created by tag change. */
    boolean expectingTagChange = false;

    /**
     * Only contructor.
     * @param tag country tag
     */
    public CountryInfo(final String tag, final int color) {
        this.tag = tag;
        this.color = color;
    }
}
