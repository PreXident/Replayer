package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.FreeSubject;
import com.paradoxplaza.eu4.replayer.events.TagChange;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents parsed save game.
 */
public class SaveGame implements java.io.Serializable {

    /** Year 1 is speacial, we will treat it as null. */
    static private final Date YEAR1 = new Date((short) 1, (byte) 1, (byte) 1);

    /** Year 2 is speacial, we will treat it as null. */
    static private final Date YEAR2 = new Date((short) 1, (byte) 1, (byte) 1);

    /** Current date, meaning the end date of the SaveGame. */
    public Date date = new Date();

    /** Starting date. */
    public Date startDate = new Date();

    /** Timeline containing displayable events. */
    public final Map<Date, List<Event>> timeline = new HashMap<>();

    /** List of country tags that appeared during game through tag changes. */
    public final List<Pair<Date, TagChange>> tagChanges = new ArrayList<>();

    /** Colors of dynamic countries. */
    public final Map<String, Integer> dynamicCountriesColors = new HashMap<>();

    /**
     * Countries that are subject at the save game end.
     * Used during concatenation to detect subjects that broke
     * free/got annexed etc, so "free" event could be generated.
     */
    public final Set<String> subjects = new HashSet<>();

    /**
     * Adds event on given date to timeline at the end of the list.
     * Events with priority needs their overloadings!
     * @param date date of the event
     * @param event displayable event
     */
    public void addEvent(Date date, final Event event) {
        if (YEAR1.equals(date) || YEAR2.equals(date)) {
            date = null;
        }
        final List<Event> list = getEventList(date);
        list.add(event);
    }

    /**
     * Adds TagChange on given date to timeline at the beginning of the list.
     * @param date date if the tagChange
     * @param tagChange TagChange to be added
     */
    public void addEvent(Date date, final TagChange tagChange) {
        if (YEAR1.equals(date) || YEAR2.equals(date)) {
            date = null;
        }
        final List<Event> list = getEventList(date);
        list.add(0, tagChange); //tag changes need to be first on that day
    }

    /**
     * Adds subject tag to the list of subjects.
     * @param subject subject tag to add
     */
    public void addSubject(final String subject) {
        subjects.add(subject);
    }

    /**
     * Adds content of the other save to this save game.
     * @param other the other save game
     */
    public void concatenate(final SaveGame other) {
        tagChanges.addAll(other.tagChanges);
        dynamicCountriesColors.putAll(other.dynamicCountriesColors);
        for (Date d : new DateGenerator(date.prev(), other.date)) {
            getEventList(d).addAll(other.getEventList(d));
        }
        final Set<String> subjectsCopy = new HashSet<>(subjects);
        for (String subject : subjectsCopy) {
            if (!other.subjects.contains(subject)) {
                addEvent(other.date, new FreeSubject(subject));
                subjects.remove(subject);
            }
        }
        date = other.date;
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
