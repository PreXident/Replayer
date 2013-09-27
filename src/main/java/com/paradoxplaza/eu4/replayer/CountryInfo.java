package com.paradoxplaza.eu4.replayer;

import java.util.Date;
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

    /** Set of provinces controlled by this country. */
    public final Set<String> controls = new HashSet<>();

    /** Set of provinces owned by this country. */
    public final Set<String> owns = new HashSet<>();

    /** Date the country will be created by tag change. */
    public Date expectingTagChange = null;

    /**
     * Only contructor.
     * @param tag country tag
     */
    public CountryInfo(final String tag, final int color) {
        this.tag = tag;
        this.color = color;
    }
}
