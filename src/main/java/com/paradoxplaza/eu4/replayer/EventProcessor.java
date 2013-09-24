package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.events.Religion;
import com.paradoxplaza.eu4.replayer.events.TagChange;

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
     * Processes Controller event.
     * @param date date of the event
     * @param controller controller change
     * @return true if event should be logged, false otherwise
     */
    private boolean processController(final Date date, final Controller controller) {
        final CountryInfo newController = replayerController.countries.get(controller.tag);
        if (newController != null && newController.expectingTagChange != null && newController.expectingTagChange.compareTo(date) > 0) {
            return false;
        }
        final ProvinceInfo province = replayerController.provinces.get(controller.id);
        final CountryInfo previousController = replayerController.countries.get(province.controller);
        if (previousController != null) {
            previousController.controls.remove(province.id);
        }
        province.controller = controller.tag;
        int color = replayerController.landColor;
        if (newController != null) {
            newController.controls.add(province.id);
            color = newController.color;
        }
        for(int p : province.points) {
            if ( p / replayerController.bufferWidth % 2 == 0) {
                setColor(replayerController.politicalBuffer, p, color);
            }
        }
        return true;
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
            boolean appendToLog = true;
            if (event instanceof Controller) {
                appendToLog = processController(date, (Controller) event);
            } else if (event instanceof Owner) {
                appendToLog = processOwner(date, (Owner) event);
            } else if (event instanceof TagChange) {
                appendToLog = processTagChange(date, (TagChange) event);
            } else if (event instanceof Religion) {
                appendToLog = processReligion(date, (Religion) event);
            }
            if (appendToLog) {
                logChange = true;
                System.out.println(String.format("[%1$s]: %2$s", date, event));
                replayerController.logContent.append(String.format("[%1$s]: %2$#s<br>", date, event));
            }
        }
        if (logChange) {
            updateLog();
        }
    }

    /**
     * Processes Owner event.
     * @param date date of the event
     * @param owner owner change event
     * @return true if event should be logged, false otherwise
     */
    private boolean processOwner(final Date date, final Owner owner) {
        final ProvinceInfo province = replayerController.provinces.get(owner.id);
        final CountryInfo previousOwner = replayerController.countries.get(province.owner);
        final String previousController = province.controller;
        final CountryInfo newOwner = replayerController.countries.get(owner.value);
        if (newOwner != null && newOwner.expectingTagChange != null && newOwner.expectingTagChange.compareTo(date) > 0) {
            return false;
        }
        if (previousOwner != null) {
            previousOwner.owns.remove(province.id);
            previousOwner.controls.remove(province.id);
        }
        province.owner = owner.value;
        province.controller = owner.value;
        int color = replayerController.landColor;
        if (newOwner != null) {
            newOwner.owns.add(province.id);
            newOwner.controls.add(province.id);
            color = newOwner.color;
        }
        for(int p : province.points) {
            if ( p / replayerController.bufferWidth % 2 == 1
                    || previousOwner == null || previousOwner.tag.equals(previousController)
                    || !replayerController.notableEvents.contains("Controller")) {
                setColor(replayerController.politicalBuffer, p, color);
            }
        }
        return true;
    }

    /**
     * Processes religion change event.
     * @param date date of the event
     * @param religion religion change event
     * @return true if event should be logged, false otherwise
     */
    private boolean processReligion(final Date date, final Religion religion) {
        final ProvinceInfo province = replayerController.provinces.get(religion.id);
        province.religion = religion.value;
        final Integer Color = replayerController.religions.get(religion.value);
        int color = Color == null ? replayerController.landColor : Color;
        for(int p : province.points) {
            setColor(replayerController.religiousBuffer, p, color);
        }
        return true;
    }

    /**
     * Processes tag change event.
     * @param date date of the event
     * @param tagChange tag change event
     * @return true if event should be logged, false otherwise
     */
    private boolean processTagChange(final Date date, final TagChange tagChange) {
        final CountryInfo from = replayerController.countries.get(tagChange.fromTag);
        final CountryInfo to = replayerController.countries.get(tagChange.toTag);
        to.controls.addAll(from.controls);
        to.owns.addAll(from.owns);
        to.expectingTagChange = null;
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
     * Sets color of the map.
     * Intented to be overridden by descendants if only
     * buffer/output image/nothing should be updated.
     * @param pos
     * @param color
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
