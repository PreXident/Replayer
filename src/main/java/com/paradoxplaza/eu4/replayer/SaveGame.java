package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.TagChange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents parsed save game.
 */
public class SaveGame {

    /** Current date, meaning the end date of the SaveGame. */
    public Date date = new Date();

    /** Starting date. */
    public Date startDate = new Date();

    /** Timeline containing displayable events. */
    final Map<Date, List<Event>> timeline = new HashMap<>();

    /** Set of country tags that appeard during game through tag changes. */
    public final Map<String, Date> tagChanges = new HashMap<>();

    /** Colors of dynamic countries. */
    public final Map<String, Integer> dynamicCountriesColors = new HashMap<>();

    /**
     * Adds event on given date to timeline at the end of the list.
     * Events with priority needs their overloadings!
     * @param date date of the event
     * @param event displayable event
     */
    public void addEvent(final Date date, final Event event) {
        final List<Event> list = getEventList(date);
        list.add(event);
    }

    /**
     * Adds TagChange on given date to timeline at the beginning of the list.
     * @param date date if the tagChange
     * @param tagChange TagChange to be added
     */
    public void addEvent(final Date date, final TagChange tagChange) {
        final List<Event> list = getEventList(date);
        list.add(0, tagChange); //tag changes need to be first on that day
    }

    /**
     * Returns list of events associated with the given date.
     * If there's no such list, it is created and inserted into timeline.
     * @param date date associated to the list
     * @return list of events associated with the given date
     */
    private List<Event> getEventList(final Date date) {
        List<Event> list = timeline.get(date);
        if (list == null) {
            list = new ArrayList<>();
            timeline.put(date, list);
        }
        return list;
    }

    /**
     * Returns mapping of dynamic countries' colors.
     * @return mapping of dynamic countries' colors
     */
    public Map<String, Integer> getDynamicCountriesColors() {
        return dynamicCountriesColors;
    }
}
