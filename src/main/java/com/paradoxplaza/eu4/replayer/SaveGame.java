package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents parsed save game.
 */
public class SaveGame {

    /** Current date. */
    public Date date = new Date();

    /** Starting date. */
    public Date startDate = new Date();

    /** Timeline containing displayable events. */
    final Map<Date, List<Event>> timeline = new HashMap<>();

    /** Set of country tags that appeard during game through tag changes. */
    public final Set<String> tagChanges = new HashSet<>();

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
