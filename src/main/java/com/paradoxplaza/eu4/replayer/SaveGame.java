package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Event;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents parsed save game.
 */
public class SaveGame {

    /** Current date. */
    public final Date date = new Date();

    /** Starting date. */
    public final Date startDate = new Date();

    /** Timeline containing displayable events. */
    final Map<Date, Event> timeline = new HashMap<>();

    /**
     * Adds event on given date to timeline.
     * @param date date of the event
     * @param event displayable event
     */
    public void addEvent(final Date date, final Event event) {
        timeline.put(date, event);
    }
}
