package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Culture;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.events.Religion;
import com.paradoxplaza.eu4.replayer.events.TagChange;
import java.util.LinkedList;

/**
 * Processes list of events that happened on certain date.
 * Updates log and writes to output image and buffer too.
 */
public class EventProcessor {

    /** Controller whose controls will be updated. */
    final ReplayerController replayerController;

    /**
     * Only constructor.
     * @param controller ReplayerController whose controls will be updated
     */
    public EventProcessor(final ReplayerController controller) {
        this.replayerController = controller;
    }

    /**
     * Changes province controller.
     * @param date date of change
     * @param provinceID province id
     * @param newControllerTag new controller
     * @return true if event should be logged, false otherwise
     */
    public boolean changeController(final Date date,
            final String provinceID, final String newControllerTag) {
        final CountryInfo newController = replayerController.countries.get(newControllerTag);
        if (newController != null && newController.expectingTagChange != null && newController.expectingTagChange.compareTo(date) > 0) {
            return false;
        }
        final ProvinceInfo province = replayerController.provinces.get(provinceID);
        final String prevControllerTag = province.controller;
        final CountryInfo previousController = replayerController.countries.get(province.controller);
        if (previousController != null) {
            previousController.controls.remove(province.id);
        }
        province.controller = newControllerTag;
        int color = replayerController.landColor;
        if (newController != null
                && (!replayerController.focusing
                    || newController.tag.equals(replayerController.focusTag))) {
            newController.controls.add(province.id);
            color = newController.color;
        }
        if (replayerController.focusing
                && !replayerController.focusTag.equals(newControllerTag)
                && !replayerController.focusTag.equals(prevControllerTag)) {
            return false;
        }
        for(int p : province.points) {
            if ( p / replayerController.bufferWidth % 2 == 0) {
                setColor(replayerController.politicalBuffer, p, color);
            }
        }
        return true;
    }

    /**
     * Changes province culture.
     * @param date date of change
     * @param provinceID province id
     * @param newCulture new culture
     * @return true if event should be logged, false otherwise
     */
    public boolean changeCulture(final Date date,
            final String provinceID, final String newCulture) {
        final ProvinceInfo province = replayerController.provinces.get(provinceID);
        province.culture = newCulture;
        final Integer Color = replayerController.cultures.get(newCulture);
        int color = Color == null ? replayerController.landColor : Color;
        for(int p : province.points) {
            setColor(replayerController.culturalBuffer, p, color);
        }
        return true;
    }

    /**
     * Changes province owner.
     * @param date date of change
     * @param provinceID province id
     * @param newOwnerTag new owner
     * @return true if event should be logged, false otherwise
     */
    public boolean changeOwner(final Date date,
            final String provinceID, final String newOwnerTag, final String newControllerTag) {
        final ProvinceInfo province = replayerController.provinces.get(provinceID);
        final CountryInfo previousOwner = replayerController.countries.get(province.owner);
        final String previousOwnerTag = province.owner;
        final String previousControllerTag = province.controller;
        final CountryInfo previousController = replayerController.countries.get(province.controller);
        final CountryInfo newOwner = replayerController.countries.get(newOwnerTag);
        final CountryInfo newController = replayerController.countries.get(newControllerTag);
        if (newOwner != null && newOwner.expectingTagChange != null && newOwner.expectingTagChange.compareTo(date) > 0) {
            return false;
        }
        if (previousOwner != null) {
            previousOwner.owns.remove(province.id);
        }
        if (previousController != null) {
            previousController.controls.remove(province.id);
        }
        province.owner = newOwnerTag;
        province.controller = newControllerTag;
        if (replayerController.focusing
                && !replayerController.focusTag.equals(newOwnerTag)
                && !replayerController.focusTag.equals(previousOwnerTag)) {
            return false;
        }
        int ownerColor = replayerController.landColor;
        if (newOwner != null
                && (!replayerController.focusing
                    || newOwner.tag.equals(replayerController.focusTag))) {
            newOwner.owns.add(province.id);
            ownerColor = newOwner.color;
        }
        int controllerColor = replayerController.landColor;
        if (newController != null
                && (!replayerController.focusing
                    || newController.tag.equals(replayerController.focusTag))) {
            newController.controls.add(province.id);
            controllerColor = newController.color;
        }
        for(int p : province.points) {
            if (replayerController.notableEvents.contains("Controller")
                    && p / replayerController.bufferWidth % 2 == 0) {
                setColor(replayerController.politicalBuffer, p, controllerColor);
            } else {
                setColor(replayerController.politicalBuffer, p, ownerColor);
            }
        }
        return true;
    }

    /**
     * Changes province religion.
     * @param date date of change
     * @param provinceID province id
     * @param newReligion new religion
     * @return true if event should be logged, false otherwise
     */
    public boolean changeReligion(final Date date,
            final String provinceID, final String newReligion) {
        final ProvinceInfo province = replayerController.provinces.get(provinceID);
        province.religion = newReligion;
        final Integer Color = replayerController.religions.get(newReligion);
        int color = Color == null ? replayerController.landColor : Color;
        for(int p : province.points) {
            setColor(replayerController.religiousBuffer, p, color);
        }
        return true;
    }

    /**
     * Changes country tag.
     * @param date date of change
     * @param newTag change tag to this
     * @param oldTag change tag from this
     * @return true if event should be logged, false otherwise
     */
    public boolean changeTag(final Date date,
            final String newTag, final String oldTag) {
        final CountryInfo from = replayerController.countries.get(oldTag);
        final CountryInfo to = replayerController.countries.get(newTag);
        to.controls.addAll(from.controls);
        to.owns.addAll(from.owns);
        if (replayerController.focusing) {
            if (replayerController.focusTag.equals(oldTag)) {
                replayerController.focusTag = newTag;
            } else if (!replayerController.focusTag.equals(newTag)) {
                return false;
            }
        }
        for (String id : to.controls) {
            final ProvinceInfo province = replayerController.provinces.get(id);
            province.controller = to.tag;
            for(int p : province.points) {
                if ( p / replayerController.bufferWidth % 2 == 0) {
                    setColor(replayerController.politicalBuffer, p, to.color);
                }
            }
        }
        for (String id : to.owns) {
            final ProvinceInfo province = replayerController.provinces.get(id);
            province.owner = to.tag;
            for(int p : province.points) {
                if ( p / replayerController.bufferWidth % 2 == 1
                        || !replayerController.notableEvents.contains("Controller")) {
                    setColor(replayerController.politicalBuffer, p, to.color);
                }
            }
        }
        return true;
    }

    /**
     * Generic visit method for events that do not need unique processing.
     * @param date date of event
     * @param event event to process
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Event event) {
        return true;
    }

    /**
     * Processes Controller event.
     * @param date date of the event
     * @param controller controller change
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Controller controller) {
        final ProvinceInfo province = replayerController.provinces.get(controller.id);
        province.addEvent(date, controller);
        controller.previousValue = province.controller;
        return changeController(date, controller.id, controller.tag);
    }

    /**
     * Processes Culture event.
     * @param date date of the event
     * @param culture culture change
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Culture culture) {
        final ProvinceInfo province = replayerController.provinces.get(culture.id);
        province.addEvent(date, culture);
        culture.previousValue = province.culture;
        return changeCulture(date, culture.id, culture.value);
    }

    /**
     * Processes Owner event.
     * @param date date of the event
     * @param owner owner change event
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Owner owner) {
        final ProvinceInfo province = replayerController.provinces.get(owner.id);
        province.addEvent(date, owner);
        owner.previousValue = province.owner;
        owner.previousController = province.controller;
        return changeOwner(date, owner.id, owner.value, owner.value);
    }

    /**
     * Processes religion change event.
     * @param date date of the event
     * @param religion religion change event
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Religion religion) {
        final ProvinceInfo province = replayerController.provinces.get(religion.id);
        province.addEvent(date, religion);
        religion.previousValue = province.religion;
        return changeReligion(date, religion.id, religion.value);
    }

    /**
     * Processes tag change event.
     * @param date date of the event
     * @param tagChange tag change event
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final TagChange tagChange) {
        final CountryInfo to = replayerController.countries.get(tagChange.toTag);
        to.expectingTagChange = null;
        return changeTag(date, tagChange.toTag, tagChange.fromTag);
    }

    /**
     * Process list of events that happended on certain date.
     * @param date date of events
     * @param events list of events
     */
    public final void processEvents(final Date date, final Iterable<Event> events) {
        if (events == null) {
            System.out.println(String.format("[%1$s]: %2$s", date, "nothing happened"));
            return;
        }
        boolean logChange = false;
        for(Event event : events) {
            if (!replayerController.notableEvents.contains(event.getClass().getSimpleName()) && !(event instanceof TagChange)) {
                continue;
            }
            final boolean appendToLog = event.beProcessed(date, this);
            if (appendToLog) {
                logChange = true;
                System.out.println(String.format("[%1$s]: %2$s", date, event));
                replayerController.logContent.append(String.format("[%1$s]: %2$#s<br>", date, event));
            }
        }
        if (logChange) {
            updateLog();
        } else {
            System.out.println(String.format("[%1$s]: %2$s", date, "nothing happened"));
        }
    }

    /**
     * Generic visit method for events that do not need unique unprocessing.
     * @param date date of event
     * @param event event to unprocess
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Event event) {
        return true;
    }

    /**
     * Unprocesses Controller event.
     * @param date date of the event
     * @param controller controller change
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Controller controller) {
        final ProvinceInfo province = replayerController.provinces.get(controller.id);
        province.remove(controller);
        return changeController(date, controller.id, controller.previousValue);
    }

    /**
     * Unprocesses Culture event.
     * @param date date of the event
     * @param culture culture change
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Culture culture) {
        final ProvinceInfo province = replayerController.provinces.get(culture.id);
        province.remove(culture);
        return changeCulture(date, culture.id, culture.previousValue);
    }

    /**
     * Unprocesses Owner event.
     * @param date date of the event
     * @param owner owner change event
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Owner owner) {
        final ProvinceInfo province = replayerController.provinces.get(owner.id);
        province.remove(owner);
        return changeOwner(date, owner.id, owner.previousValue, owner.previousController);
    }

    /**
     * Unprocesses religion change event.
     * @param date date of the event
     * @param religion religion change event
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Religion religion) {
        final ProvinceInfo province = replayerController.provinces.get(religion.id);
        province.remove(religion);
        return changeReligion(date, religion.id, religion.previousValue);
    }

    /**
     * Unprocesses tag change event.
     * @param date date of the event
     * @param tagChange tag change event
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final TagChange tagChange) {
        final CountryInfo to = replayerController.countries.get(tagChange.toTag);
        to.expectingTagChange = date;
        return changeTag(date, tagChange.fromTag, tagChange.toTag);
    }

    /**
     * Process list of events that happended on certain date.
     * @param date date of events
     * @param events list of events
     */
    public final void unprocessEvents(final Date date, final Iterable<Event> events) {
        if (events == null) {
            System.out.println(String.format("![%1$s]: %2$s", date, "nothing happened"));
            return;
        }
        boolean logChange = false;
        //we need to process events in reversed order
        LinkedList<Event> list = new LinkedList<>();
        for(Event event : events) {
            if (!replayerController.notableEvents.contains(event.getClass().getSimpleName()) && !(event instanceof TagChange)) {
                continue;
            }
            list.push(event);
        }
        for (Event event : list) {
            final boolean appendToLog = event.beUnprocessed(date, this);
            if (appendToLog) {
                logChange = true;
                System.out.println(String.format("![%1$s]: %2$s", date, event));
                replayerController.logContent.append(String.format("<i>![%1$s]: %2$#s<i><br>", date, event));
            }
        }
        if (logChange) {
            updateLog();
        } else {
            System.out.println(String.format("![%1$s]: %2$s", date, "nothing happened"));
        }
    }

    /**
     * Sets ownerColor of the map.
     * Intented to be overridden by descendants if only
     * buffer/output image/nothing should be updated.
     * @param pos index into buffer
     * @param color color to paint with
     */
    protected void setColor(int[] buffer, final int pos, final int color) {
        buffer[pos] = color;
        if (replayerController.buffer == buffer) {
            replayerController.output.getPixelWriter().setArgb(
                    pos % replayerController.bufferWidth,
                    pos / replayerController.bufferWidth,
                    color);
        }
    }

    /**
     * Updates the controller log.
     * Intended to be overridden by descendants if update is not desirable.
     */
    protected void updateLog() {
        replayerController.updateLog();
    }
}
