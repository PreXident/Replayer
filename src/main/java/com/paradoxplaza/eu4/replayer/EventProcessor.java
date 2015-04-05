package com.paradoxplaza.eu4.replayer;

import com.paradoxplaza.eu4.replayer.events.AlwaysNotable;
import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Core;
import com.paradoxplaza.eu4.replayer.events.Culture;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Goods;
import com.paradoxplaza.eu4.replayer.events.Name;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.events.ProvinceEvent;
import com.paradoxplaza.eu4.replayer.events.Religion;
import com.paradoxplaza.eu4.replayer.events.Subject;
import com.paradoxplaza.eu4.replayer.events.TagChange;
import com.paradoxplaza.eu4.replayer.events.Technology;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Processes list of events that happened on certain date.
 * Updates log and writes to output image and buffer too.
 */
public class EventProcessor {

    /** Default listener that does nothing. */
    static public final IEventListener defaultListener = new IEventListener() {
        @Override
        public void appendLog(String text) { }

        @Override
        public void updateLog() { }

        @Override
        public void setColor(int[] buffer, int pos, int color) { }
    };

    /**
     * Returns color representing the technology levels as separate color parts.
     * @param adm adm level
     * @param dip dip level
     * @param mil mil level
     * @return color representing the technology levels as separate color parts
     */
    static int getSeparateTechColor(final int adm, final int dip, final int mil) {
        return Utils.toColor(mil * 7, adm * 7, dip * 7);
    }

    /**
     * Returns color representing the technology levels combined to green part.
     * @param adm adm level
     * @param dip dip level
     * @param mil mil level
     * @return color representing the technology levels combined to green part
     */
    static int getCombinedTechColor(final int adm, final int dip, final int mil) {
        final int val = adm + dip + mil;
        final int round = (int) (val * 8.0/3.0);
        return Utils.toColor(0, round, 0);
    }

    /** Replay to update. */
    Replay replay;

    /** IEventListener to log and buffer updates. */
    IEventListener listener = defaultListener;

    /**
     * Only constructor.
     * @param replay replaying replay
     */
    public EventProcessor(final Replay replay) {
        this.replay = replay;
    }

    /**
     * Returns current listener.
     * @return current listener
     */
    public IEventListener getListener() {
        return listener;
    }

    /**
     * Sets update log and buffer updates listener. Do not pass null!
     * @param listener new listener
     */
    public void setListener(final IEventListener listener) {
        this.listener = listener;
    }

    /**
     * Resets {@link #listener} to default listener that does nothing.
     */
    public void resetListener() {
        listener = defaultListener;
    }

    /**
     * Returns default color for province.
     * @param province given province
     * @return default color
     */
    private int getDefaultColor(final ProvinceInfo province) {
        return province.isSea ? replay.seaColor : replay.landColor;
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
        final CountryInfo newController = replay.countries.get(newControllerTag);
        final ProvinceInfo province = replay.provinces.get(provinceID);
        final String prevControllerTag = province.controller;
        final CountryInfo previousController = replay.countries.get(province.controller);
        if (previousController != null) {
            previousController.controls.remove(province.id);
        }
        province.controller = newControllerTag;
        int color = getDefaultColor(province);
        final CountryInfo overlord = topOverlord(newController);
        if (newController != null) {
            newController.controls.add(province.id);
            if (newController.overlord != null
                    && replay.subjectsAsOverlords
                    && (!replay.focusing
                        || replay.focusTags.contains(overlord.tag)
                        || replay.focusTags.contains(newController.tag))) {
                color = overlord.color;
            } else if (!replay.focusing
                    || replay.focusTags.contains(newController.tag)) {
                color = newController.color;
            }
        }
        if (replay.focusing
                && !replay.focusTags.contains(newControllerTag)
                && !replay.focusTags.contains(prevControllerTag)
                && (newController == null || !replay.focusTags.contains(overlord.tag))
                && (previousController == null || !replay.focusTags.contains(topOverlord(previousController).tag))) {
            return false;
        }
        for(int p : province.points) {
            if ( p / replay.bufferWidth % 2 == 0) {
                setColor(replay.politicalBuffer, p, color);
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
        final ProvinceInfo province = replay.provinces.get(provinceID);
        province.culture = newCulture;
        final Integer Color = replay.cultures.get(newCulture);
        int color = Color == null ? getDefaultColor(province) : Color;
        for(int p : province.points) {
            setColor(replay.culturalBuffer, p, color);
        }
        return true;
    }

    public boolean changeOverlord(final Date date,
            final String subjectTag, final String newOverlordTag) {
        final CountryInfo subject = replay.countries.get(subjectTag);
        final CountryInfo overlord = replay.countries.get(newOverlordTag);
        subject.overlord = newOverlordTag;
        if (overlord != null) {
            overlord.subjects.add(subjectTag);
        }
        if (replay.subjectsAsOverlords) {
            int color = replay.focusing ? replay.landColor : subject.color;
            final CountryInfo topOverlord = topOverlord(subject);
            if (overlord != null
                    && (!replay.focusing
                        || replay.focusTags.contains(topOverlord.tag))) {
                color = topOverlord.color;
            }
            final List<String> controlled = new ArrayList<>(subject.controls);
            final List<String> owned = new ArrayList<>(subject.owns);
            //add all provinces belonging to subjects
            final LinkedList<String> queue = new LinkedList<>(subject.subjects);
            while (!queue.isEmpty()) {
                final String tag = queue.pop();
                final CountryInfo subsubject = replay.countries.get(tag);
                queue.addAll(subsubject.subjects);
                controlled.addAll(subsubject.controls);
                owned.addAll(subsubject.owns);
            }
            //change colors
            for (String id : controlled) {
                final ProvinceInfo province = replay.provinces.get(id);
                for(int p : province.points) {
                    if ( p / replay.bufferWidth % 2 == 0) {
                        setColor(replay.politicalBuffer, p, color);
                    }
                }
            }
            for (String id : owned) {
                final ProvinceInfo province = replay.provinces.get(id);
                for(int p : province.points) {
                    if ( p / replay.bufferWidth % 2 == 1
                            || !replay.notableEvents.contains("Controller")) {
                        setColor(replay.politicalBuffer, p, color);
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
        final ProvinceInfo province = replay.provinces.get(provinceID);
        final CountryInfo previousOwner = replay.countries.get(province.owner);
        final String previousOwnerTag = province.owner;
        final CountryInfo previousController = replay.countries.get(province.controller);
        final CountryInfo newOwner = replay.countries.get(newOwnerTag);
        final CountryInfo newController = replay.countries.get(newControllerTag);
        if (previousOwner != null) {
            previousOwner.owns.remove(province.id);
        }
        if (previousController != null) {
            previousController.controls.remove(province.id);
        }
        province.owner = newOwnerTag;
        province.controller = newControllerTag;
        int ownerColor = getDefaultColor(province);
        if (newOwner != null) {
            newOwner.owns.add(province.id);
            final CountryInfo topOverlord = topOverlord(newOwner);
            if (newOwner.overlord != null
                    && replay.subjectsAsOverlords
                    && (!replay.focusing
                        || replay.focusTags.contains(topOverlord.tag)
                        || replay.focusTags.contains(newOwner.tag))) {
                ownerColor = topOverlord.color;
            } else if (!replay.focusing
                    || replay.focusTags.contains(newOwner.tag)) {
                ownerColor = newOwner.color;
            }
        }
        int controllerColor = getDefaultColor(province);
        if (newController != null) {
            newController.controls.add(province.id);
            final CountryInfo topOverlord = topOverlord(newController);
            if (newController.overlord != null
                    && replay.subjectsAsOverlords
                    && (!replay.focusing
                        || replay.focusTags.contains(topOverlord.tag)
                        || replay.focusTags.contains(newController.tag))) {
                controllerColor = topOverlord.color;
            } else if (!replay.focusing
                    || replay.focusTags.contains(newController.tag)) {
                controllerColor = newController.color;
            }
        }
        if (replay.focusing
                && !replay.focusTags.contains(newOwnerTag)
                && !replay.focusTags.contains(previousOwnerTag)
                && (newController == null || !replay.focusTags.contains(newController.overlord))
                && (previousOwner == null || !replay.focusTags.contains(topOverlord(previousOwner).tag))) {
            return false;
        }
        final int techCombinedColor = newOwner == null ? replay.landColor : getCombinedTechColor(newOwner.adm, newOwner.dip, newOwner.mil);
        final int techSeparateColor = newOwner == null ? replay.landColor : getSeparateTechColor(newOwner.adm, newOwner.dip, newOwner.mil);
        for(int p : province.points) {
            if (replay.notableEvents.contains("Controller")
                    && p / replay.bufferWidth % 2 == 0) {
                setColor(replay.politicalBuffer, p, controllerColor);
            } else {
                setColor(replay.politicalBuffer, p, ownerColor);
            }
            setColor(replay.technologyCombinedBuffer, p, techCombinedColor);
            setColor(replay.technologySeparateBuffer, p, techSeparateColor);
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
        final ProvinceInfo province = replay.provinces.get(provinceID);
        province.religion = newReligion;
        final Integer Color = replay.religions.get(newReligion);
        int color = Color == null ? getDefaultColor(province) : Color;
        for(int p : province.points) {
            setColor(replay.religiousBuffer, p, color);
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
        final CountryInfo from = replay.countries.get(oldTag);
        final CountryInfo to = replay.countries.get(newTag);
        to.controls.clear();
        to.controls.addAll(from.controls);
        to.owns.clear();
        to.owns.addAll(from.owns);
        to.subjects.clear();
        to.subjects.addAll(from.subjects);
        for (String subject : to.subjects) {
            changeOverlord(date, subject, newTag);
        }
        if (from.overlord != null && !from.overlord.equals(newTag)) { //japanese fix
            to.overlord = from.overlord;
        }
        from.owns.clear();
        from.controls.clear();
        to.adm = from.adm;
        to.dip = from.dip;
        to.mil = from.mil;
        if (replay.focusing) {
            if (replay.focusTags.contains(oldTag)) {
                replay.focusTags.remove(oldTag);
                replay.focusTags.add(newTag);
            } else if (!replay.focusTags.contains(newTag)) {
                return false;
            }
        }
        final int color = to.overlord == null || !replay.subjectsAsOverlords ? to.color : topOverlord(to).color;
        //set new controller for controlled provinces
        for (String id : to.controls) {
            final ProvinceInfo province = replay.provinces.get(id);
            province.controller = to.tag;
        }
        //set new owner for owned provinces
        for (String id : to.owns) {
            final ProvinceInfo province = replay.provinces.get(id);
            province.owner = to.tag;
        }
        final List<String> controlled = new ArrayList<>(to.controls);
        final List<String> owned = new ArrayList<>(to.owns);
        //iterate subjects and change overlord
        //if needed add their provinces to controlled and owned to change colors
        final LinkedList<String> subsubjects = new LinkedList<>(); //no overlord change
        for (String tag : to.subjects) {
            final CountryInfo subject = replay.countries.get(tag);
            subject.overlord = newTag;
            if (replay.subjectsAsOverlords) {
                subsubjects.addAll(subject.subjects);
                controlled.addAll(subject.controls);
                owned.addAll(subject.owns);
            }
        }
        while (!subsubjects.isEmpty()) {
            final String tag = subsubjects.pop();
            final CountryInfo subsubject = replay.countries.get(tag);
            subsubjects.addAll(subsubject.subjects);
            controlled.addAll(subsubject.controls);
            owned.addAll(subsubject.owns);
        }
        //change colors of provinces
        for (String id : controlled) {
            final ProvinceInfo province = replay.provinces.get(id);
            for(int p : province.points) {
                if ( p / replay.bufferWidth % 2 == 0) {
                    setColor(replay.politicalBuffer, p, color);
                }
            }
        }
        for (String id : owned) {
            final ProvinceInfo province = replay.provinces.get(id);
            for(int p : province.points) {
                if ( p / replay.bufferWidth % 2 == 1
                        || !replay.notableEvents.contains("Controller")) {
                    setColor(replay.politicalBuffer, p, color);
                }
            }
        }
        return true;
    }

    public boolean changeTech(final String tag,
            final int adm, final int dip, final int mil) {
        final CountryInfo country = replay.countries.get(tag);
        if (country == null) {
            return false;
        }
        country.adm = adm;
        country.dip = dip;
        country.mil = mil;
        final int separateColor = getSeparateTechColor(adm, dip, mil);
        final int combinedColor = getCombinedTechColor(adm, dip, mil);
        for (String id : country.owns) {
            final ProvinceInfo province = replay.provinces.get(id);
            for(int p : province.points) {
                setColor(replay.technologySeparateBuffer, p, separateColor);
                setColor(replay.technologyCombinedBuffer, p, combinedColor);
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
        final ProvinceInfo province = replay.provinces.get(controller.id);
        province.addEvent(date, controller);
        controller.previousValue = province.controller;
        return changeController(date, controller.id, controller.tag);
    }

    /**
     * Processes Core event. Handles core2owner fix.
     * @param date date of the event
     * @param core core change
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Core core) {
        if (replay.fixCore2Owner && replay.notableEvents.contains("Owner")
                && core.type == Core.ADDED) {
            final ProvinceInfo province = replay.provinces.get(core.id);
            if (!core.tag.equals(province.owner)) {
                if (core.owner == null) {
                    core.owner = new Owner(core.id, core.name, core.tag);
                }
                core.owner.beProcessed(date, this);
            }
        }
        return true;
    }

    /**
     * Processes Culture event.
     * @param date date of the event
     * @param culture culture change
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Culture culture) {
        final ProvinceInfo province = replay.provinces.get(culture.id);
        province.addEvent(date, culture);
        culture.previousValue = province.culture;
        return changeCulture(date, culture.id, culture.value);
    }

    /**
     * Processes province trade goods chage event. Handles goods2owner fix.
     * @param date date of the event
     * @param goods goods change event
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Goods goods) {
        if (replay.fixGoods2Owner && "unknown".equals(goods.value)
                && replay.notableEvents.contains("Owner")) {
            final ProvinceInfo province = replay.provinces.get(goods.id);
            if (province.owner != null) {
                if (goods.owner == null) {
                    goods.owner = new Owner(goods.id, goods.name, null);
                }
                goods.owner.beProcessed(date, this);
            }
        }
        return true;
    }

    /**
     * Processes province name changed event.
     * @param date date of the event
     * @param name province name changed event
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Name name) {
        final ProvinceInfo province = replay.provinces.get(name.id);
        name.previousValue = province.name;
        province.name = name.value;
        province.addEvent(date, name);
        return true;
    }

    /**
     * Processes Owner event.
     * @param date date of the event
     * @param owner owner change event
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Owner owner) {
        final ProvinceInfo province = replay.provinces.get(owner.id);
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
        final ProvinceInfo province = replay.provinces.get(event.id);
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
        final ProvinceInfo province = replay.provinces.get(religion.id);
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
        final CountryInfo subjectCountry = replay.countries.get(subject.tag);
        subject.oldOverlord = subjectCountry.overlord;
        final CountryInfo oldOverlord = replay.countries.get(subject.oldOverlord);
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
        return changeTag(date, tagChange.tag.val, tagChange.fromTag);
    }

    /**
     * Processes technology event.
     * @param date date of the event
     * @param technology tag change event
     * @return true if event should be logged, false otherwise
     */
    public boolean process(final Date date, final Technology technology) {
        final CountryInfo country = replay.countries.get(technology.tag);
        if (country == null) {
            return false;
        }
        technology.old_adm = country.adm;
        technology.old_dip = country.dip;
        technology.old_mil = country.mil;
        return changeTech(technology.tag, technology.adm, technology.dip, technology.mil);
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
            if (!replay.notableEvents.contains(event.getClass().getSimpleName()) && !(event.getClass().isAnnotationPresent(AlwaysNotable.class))) {
                continue;
            }
            final boolean appendToLog = event.beProcessed(date, this);
            if (appendToLog) {
                logChange = true;
                System.out.printf("[%1$s]: %2$s\n", date, event);
                listener.appendLog(String.format("[%1$s]: %2$#s<br>", date, event));
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
            cand = replay.countries.get(cand.overlord);
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
        final ProvinceInfo province = replay.provinces.get(controller.id);
        province.remove(controller);
        return changeController(date, controller.id, controller.previousValue);
    }

    /**
     * Unprocesses Core event. Handles core2owner fix.
     * @param date date of the event
     * @param core core change
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Core core) {
        if (core.owner != null) {
            core.owner.beUnprocessed(date, this);
        }
        return true;
    }

    /**
     * Unprocesses Culture event.
     * @param date date of the event
     * @param culture culture change
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Culture culture) {
        final ProvinceInfo province = replay.provinces.get(culture.id);
        province.remove(culture);
        return changeCulture(date, culture.id, culture.previousValue);
    }

    /**
     * Unprocesses province trade goods change event. Handles goods2owner fix.
     * @param date date of the event
     * @param goods goods change event
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Goods goods) {
        if (goods.owner != null) {
            goods.owner.beUnprocessed(date, this);
        }
        return true;
    }

    /**
     * Unprocesses province name changed event.
     * @param date date of the event
     * @param name province name changed event
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Name name) {
        final ProvinceInfo province = replay.provinces.get(name.id);
        province.name = name.previousValue;
        province.remove(name);
        return true;
    }

    /**
     * Unprocesses Owner event.
     * @param date date of the event
     * @param owner owner change event
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Owner owner) {
        final ProvinceInfo province = replay.provinces.get(owner.id);
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
        final ProvinceInfo province = replay.provinces.get(event.id);
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
        final ProvinceInfo province = replay.provinces.get(religion.id);
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
        final CountryInfo newOverlord = replay.countries.get(subject.newOverlord);
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
        return changeTag(date, tagChange.fromTag, tagChange.tag.val);
    }

    /**
     * Unprocesses technology event.
     * @param date date of the event
     * @param technology technology event
     * @return true if event should be logged, false otherwise
     */
    public boolean unprocess(final Date date, final Technology technology) {
        return changeTech(technology.tag, technology.old_adm, technology.old_dip, technology.old_mil);
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
            if (!replay.notableEvents.contains(event.getClass().getSimpleName()) && !(event.getClass().isAnnotationPresent(AlwaysNotable.class))) {
                continue;
            }
            list.push(event);
        }
        for (Event event : list) {
            final boolean appendToLog = event.beUnprocessed(date, this);
            if (appendToLog) {
                logChange = true;
                System.out.printf("![%1$s]: %2$s\n", date, event);
                listener.appendLog(String.format("<i>![%1$s]: %2$#s<i><br>", date, event));
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
        listener.setColor(buffer, pos, color);
    }

    /**
     * Updates the controller log.
     * Intended to be overridden by descendants if update is not desirable.
     */
    protected void updateLog() {
        listener.updateLog();
    }

    /**
     * Handles GUI related stuff.
     * Listens to log changes, updates of log and buffers.
     */
    public interface IEventListener {
        void appendLog(String text);
        void updateLog();
        void setColor(int[] buffer, final int pos, final int color);
    }
}
