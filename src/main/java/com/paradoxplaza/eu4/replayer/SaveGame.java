package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Event;

import java.util.ArrayList;
import java.util.Date;
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

    /**
     * Adds event on given date to timeline.
     * @param date date of the event
     * @param event displayable event
     */
    public void addEvent(final Date date, final Event event) {
        List<Event> list = timeline.get(date);
        if (list == null) {
            list = new ArrayList<>();
            timeline.put(date, list);
        }
        list.add(event);
    }
}
