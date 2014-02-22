package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Event;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple wrapper for information about provinces.
 */
public class ProvinceInfo {

    /** Province id. */
    public final String id;

    /** Province id. */
    public final String name;

    /** Pixel color of this province. */
    public final int color;

    /** List of pixels of this province. */
    public final List<Integer> points = new ArrayList<>();

    /** Center of the province. */
    public final Point center = new Point(-1, -1);

    /** Controller of this province. */
    public String controller = null;

    /** Owner of this province. */
    public String owner = null;

    /** Religion of this province. */
    public String religion = null;

    /** Culture of this province. */
    public String culture = null;

    /** Events that happened to this province. */
    List<Pair<Date,Event>> events = new ArrayList<>();

    /** Colonial region the province belongs to, if any. */
    public ColRegionInfo colRegion = null;

    /**
     * Only constructor.
     * @param id province id
     * @param name province name
     * @param color color of pixels associated to this province
     */
    public ProvinceInfo(final String id, final String name, final int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    /**
     * Adds event related to this province to its {@link #events}.
     * @param date date of event
     * @param event province event
     */
    public void addEvent(final Date date, final Event event) {
        events.add(new Pair<>(date, event));
    }

    /**
     * Computes the center of the province as a mean of pixel coordinates.
     * @param mapWidth width of the map
     */
    public void calculateCenter(final int mapWidth) {
        if (points.isEmpty()) {
            return;
        }
        int x = 0;
        int y = 0;
        for (Integer p : points) {
            x += p % mapWidth;
            y += p / mapWidth;
        }
        center.move(x / points.size(), y / points.size());
    }

    /**
     * Returns html representation of {@link #events}.
     * @return html representation of events
     */
    private String eventLog() {
        final StringBuilder s = new StringBuilder();
        for(Pair<Date, Event> pair : events) {
            s.append(String.format("[%1$s]: %2$#s<br>", pair.getFirst(), pair.getSecond()));
        }
        return s.toString();
    }

    /**
     * Returns html page with province info.
     * @return html page with province info
     */
    public String getLog() {
        return String.format(
                "<html><body>" +
                    "<p>%8$s=%1$s<br>%9$s=%2$s<br>%10$s=%3$s<br>%11$s=%4$s<br>%12$s=%5$s<br>%13$s=%6$s</p>" +
                    "<p>%7$s</p>" +
                "</body></html>",
                id, name, controller, owner, religion, culture, eventLog(),
                l10n("province.log.id"), l10n("province.log.name"),
                l10n("province.log.controller"), l10n("province.log.owner"),
                l10n("province.log.religion"), l10n("province.log.culture"));
    }

    /**
     * Removes event related to this province to its {@link #events}.
     * @param event event to remove
     */
    public void remove(final Event event) {
        for (int i = events.size() - 1; i > 0; --i) {
            Pair<Date, Event> pair = events.get(i);
            if (pair.getSecond().equals(event)) {
                events.remove(i);
            }
        }
    }

    /**
     * Resets the province for new replay.
     */
    public void reset() {
        controller = null;
        owner = null;
        culture = null;
        religion = null;
        events.clear();
    }

    @Override
    public String toString() {
        return String.format(
                "%7$s=%1$s\n%8$s=%2$s\n%9$s=%3$s\n%10$s=%4$s\n%11$s=%5$s\n%12$s=%6$s",
                id, name, controller, owner, religion, culture,
                l10n("province.log.id"), l10n("province.log.name"),
                l10n("province.log.controller"), l10n("province.log.owner"),
                l10n("province.log.religion"), l10n("province.log.culture"));
    }
}
