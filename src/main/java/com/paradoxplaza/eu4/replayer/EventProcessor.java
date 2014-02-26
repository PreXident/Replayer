package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.AlwaysNotable;
import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Culture;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.events.ProvinceEvent;
import com.paradoxplaza.eu4.replayer.events.Religion;
import com.paradoxplaza.eu4.replayer.events.Subject;
import com.paradoxplaza.eu4.replayer.events.TagChange;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        final CountryInfo overlord = topOverlord(newController);
        if (newController != null) {
            newController.controls.add(province.id);
            if (newController.overlord != null
                    && replayerController.subjectsAsOverlords
                    && (!replayerController.focusing
                        || overlord.tag.equals(replayerController.focusTag)
                        || newController.tag.equals(replayerController.focusTag))) {
                color = overlord.color;
            } else if (!replayerController.focusing
                    || newController.tag.equals(replayerController.focusTag)) {
                color = newController.color;
            }
        }
        if (replayerController.focusing
                && !replayerController.focusTag.equals(newControllerTag)
                && !replayerController.focusTag.equals(prevControllerTag)
                && (newController == null || !replayerController.focusTag.equals(overlord.tag))
                && (previousController == null || !replayerController.focusTag.equals(topOverlord(previousController).tag))) {
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

    public boolean changeOverlord(final Date date,
            final String subjectTag, final String newOverlordTag) {
        final CountryInfo subject = replayerController.countries.get(subjectTag);
        final CountryInfo overlord = replayerController.countries.get(newOverlordTag);
        subject.overlord = newOverlordTag;
        if (overlord != null) {
            overlord.subjects.add(subjectTag);
        }
        if (replayerController.subjectsAsOverlords) {
            int color = replayerController.focusing ? replayerController.landColor : subject.color;
            final CountryInfo topOverlord = topOverlord(subject);
            if (overlord != null
                    && (!replayerController.focusing
                        || replayerController.focusTag.equals(topOverlord.tag))) {
                color = topOverlord.color;
            }
            final List<String> controlled = new ArrayList<>(subject.controls);
            final List<String> owned = new ArrayList<>(subject.owns);
            //add all provinces belonging to subjects
            final LinkedList<String> queue = new LinkedList<>(subject.subjects);
            while (!queue.isEmpty()) {
                final String tag = queue.pop();
                final CountryInfo subsubject = replayerController.countries.get(tag);
                queue.addAll(subsubject.subjects);
                controlled.addAll(subsubject.controls);
                owned.addAll(subsubject.owns);
            }
            //change colors
            for (String id : controlled) {
                final ProvinceInfo province = replayerController.provinces.get(id);
                for(int p : province.points) {
                    if ( p / replayerController.bufferWidth % 2 == 0) {
                        setColor(replayerController.politicalBuffer, p, color);
                    }
                }
            }
            for (String id : owned) {
                final ProvinceInfo province = replayerController.provinces.get(id);
                for(int p : province.points) {
                    if ( p / replayerController.bufferWidth % 2 == 1
                            || !replayerController.notableEvents.contains("Controller")) {
                        setColor(replayerController.politicalBuffer, p, color);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Changes province owner.
     * @param date date of change
     * @param provinceID province id
     * @param newOwnerTag new owner
     * @param newControllerTag new controller
     * @return true if event should be logged, false otherwise
     */
    public boolean changeOwner(final Date date,
            final String provinceID, final String newOwnerTag, final String newControllerTag) {
        final ProvinceInfo province = replayerController.provinces.get(provinceID);
        final CountryInfo previousOwner = replayerController.countries.get(province.owner);
        final String previousOwnerTag = province.owner;
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
        int ownerColor = replayerController.landColor;
        if (newOwner != null) {
            newOwner.owns.add(province.id);
            final CountryInfo topOverlord = topOverlord(newOwner);
            if (newOwner.overlord != null
                    && replayerController.subjectsAsOverlords
                    && (!replayerController.focusing
                        || topOverlord.tag.equals(replayerController.focusTag)
                        || newOwner.tag.equals(replayerController.focusTag))) {
                ownerColor = topOverlord.color;
            } else if (!replayerController.focusing
                    || newOwner.tag.equals(replayerController.focusTag)) {
                ownerColor = newOwner.color;
            }
        }
        int controllerColor = replayerController.landColor;
        if (newController != null) {
            newController.controls.add(province.id);
            final CountryInfo topOverlord = topOverlord(newController);
            if (newController.overlord != null
                    && replayerController.subjectsAsOverlords
                    && (!replayerController.focusing
                        || topOverlord.tag.equals(replayerController.focusTag)
                        || newController.tag.equals(replayerController.focusTag))) {
                controllerColor = topOverlord.color;
            } else if (!replayerController.focusing
                    || newController.tag.equals(replayerController.focusTag)) {
                controllerColor = newController.color;
            }
        }
        if (replayerController.focusing
                && !replayerController.focusTag.equals(newOwnerTag)
                && !replayerController.focusTag.equals(previousOwnerTag)
                && (newController == null || !replayerController.focusTag.equals(newController.overlord))
                && (previousOwner == null || !replayerController.focusTag.equals(topOverlord(previousOwner).tag))) {
            return false;
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
        to.controls.clear();
        to.controls.addAll(from.controls);
        to.owns.clear();
        to.owns.addAll(from.owns);
        to.subjects.clear();
        to.subjects.addAll(from.subjects);
        to.overlord = from.overlord;
        if (replayerController.focusing) {
            if (replayerController.focusTag.equals(oldTag)) {
                replayerController.focusTag = newTag;
            } else if (!replayerController.focusTag.equals(newTag)) {
                return false;
            }
        }
        final int color = to.overlord == null || !replayerController.subjectsAsOverlords ? to.color : topOverlord(to).color;
        //set new controller for controlled provinces
        for (String id : to.controls) {
            final ProvinceInfo province = replayerController.provinces.get(id);
            province.controller = to.tag;
        }
        //set new owner for owned provinces
        for (String id : to.owns) {
            final ProvinceInfo province = replayerController.provinces.get(id);
            province.owner = to.tag;
        }
        final List<String> controlled = new ArrayList<>(to.controls);
        final List<String> owned = new ArrayList<>(to.owns);
        //iterate subjects and change overlord
        //if needed add their provinces to controlled and owned to change colors
        final LinkedList<String> subsubjects = new LinkedList<>(); //no overlord change
        for (String tag : to.subjects) {
            final CountryInfo subject = replayerController.countries.get(tag);
            subject.overlord = newTag;
            if (replayerController.subjectsAsOverlords) {
                subsubjects.addAll(subject.subjects);
                controlled.addAll(subject.controls);
                owned.addAll(subject.owns);
            }
        }
        while (!subsubjects.isEmpty()) {
            final String tag = subsubjects.pop();
            final CountryInfo subsubject = replayerController.countries.get(tag);
            subsubjects.addAll(subsubject.subjects);
            controlled.addAll(subsubject.controls);
            owned.addAll(subsubject.owns);
        }
        //change colors of provinces
        for (String id : controlled) {
            final ProvinceInfo province = replayerController.provinces.get(id);
            for(int p : province.points) {
                if ( p / replayerController.bufferWidth % 2 == 0) {
                    setColor(replayerController.politicalBuffer, p, color);
                }
            }
        }
        for (String id : owned) {
            final ProvinceInfo province = replayerController.provinces.get(id);
            for(int p : province.points) {
                if ( p / replayerController.bufferWidth % 2 == 1
                        || !replayerController.notableEvents.contains("Controller")) {
                    setColor(replayerController.politicalBuffer, p, color);
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
     * Processes province event.
     * @param date date of the event
     * @param event province event
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final ProvinceEvent event) {
        final ProvinceInfo province = replayerController.provinces.get(event.id);
        province.addEvent(date, event);
        return true;
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
     * Processes become subject event.
     * @param date date of the event
     * @param subject become subject event
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Subject subject) {
        final CountryInfo subjectCountry = replayerController.countries.get(subject.tag);
        subject.oldOverlord = subjectCountry.overlord;
        final CountryInfo oldOverlord = replayerController.countries.get(subject.oldOverlord);
        if (oldOverlord != null) {
            oldOverlord.subjects.remove(subject.tag);
        }
        return changeOverlord(date, subject.tag, subject.newOverlord);
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
    public final void processEvents(final Date date, final Iterable<? extends Event> events) {
        if (events == null) {
            System.out.printf("[%1$s]: %2$s\n", date, l10n("event.nothing"));
            return;
        }
        boolean logChange = false;
        for(Event event : events) {
            if (!replayerController.notableEvents.contains(event.getClass().getSimpleName()) && !(event.getClass().isAnnotationPresent(AlwaysNotable.class))) {
                continue;
            }
            final boolean appendToLog = event.beProcessed(date, this);
            if (appendToLog) {
                logChange = true;
                System.out.printf("[%1$s]: %2$s\n", date, event);
                replayerController.logContent.append(String.format("[%1$s]: %2$#s<br>", date, event));
            }
        }
        if (logChange) {
            updateLog();
        } else {
            System.out.printf("[%1$s]: %2$s\n", date, l10n("event.nothing"));
        }
    }

    /**
     * Returns top overlord of the subject or subject itself if it is in fact independent.
     * @param subject subject country to process
     * @return top overlord of the subject
     */
    public CountryInfo topOverlord(final CountryInfo subject) {
        if (subject == null) {
            return null;
        }
        CountryInfo cand = subject;
        while (cand.overlord != null) {
            cand = replayerController.countries.get(cand.overlord);
        }
        return cand;
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
     * Unprocesses province event.
     * @param date date of the event
     * @param event province event
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final ProvinceEvent event) {
        final ProvinceInfo province = replayerController.provinces.get(event.id);
        province.remove(event);
        return true;
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
     * Unprocesses become subject event.
     * @param date date of the event
     * @param subject become subject event
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Subject subject) {
        final CountryInfo newOverlord = replayerController.countries.get(subject.newOverlord);
        if (newOverlord != null) {
            newOverlord.subjects.remove(subject.tag);
        }
        return changeOverlord(date, subject.tag, subject.oldOverlord);
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
            System.out.printf("![%1$s]: %2$s\n", date, l10n("event.nothing"));
            return;
        }
        boolean logChange = false;
        //we need to process events in reversed order
        LinkedList<Event> list = new LinkedList<>();
        for(Event event : events) {
            if (!replayerController.notableEvents.contains(event.getClass().getSimpleName()) && !(event.getClass().isAnnotationPresent(AlwaysNotable.class))) {
                continue;
            }
            list.push(event);
        }
        for (Event event : list) {
            final boolean appendToLog = event.beUnprocessed(date, this);
            if (appendToLog) {
                logChange = true;
                System.out.printf("![%1$s]: %2$s\n", date, event);
                replayerController.logContent.append(String.format("<i>![%1$s]: %2$#s<i><br>", date, event));
            }
        }
        if (logChange) {
            updateLog();
        } else {
            System.out.printf("![%1$s]: %2$s\n", date, l10n("event.nothing"));
        }
    }

    /**
     * Sets ownerColor of the map.
     * Intented to be overridden by descendants if only
     * buffer/output image/nothing should be updated.
     * @param buffer contains the map
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
