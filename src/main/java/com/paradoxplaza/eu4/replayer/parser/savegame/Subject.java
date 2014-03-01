package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.DateState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Ancestor of subject related diplomacy.
 */
public abstract class Subject extends CompoundState<SaveGame> {

    /** Overlord tag. */
    final Ref<String> overlord = new Ref<>();

    /** Subject tag. */
    final Ref<String> subject = new Ref<>();

    /** Date of becoming a subject. */
    final Ref<Date> date = new Ref<>();

    /** State processing string values. */
    final StringState<SaveGame> stringState = new StringState<>(this);

    /** State processing date values. */
    final DateState<SaveGame> dateState = new DateState<>(this);

    /** State ignoring uninteresting info. */
    final Ignore<SaveGame> ignore = new Ignore<>(this);

    /**
     * Only constructor
     * @param parent parent state
     */
    public Subject(final State<SaveGame> parent) {
        super(parent);
    }

    @Override
    protected void compoundReset() {
        if (overlord != null) {
            overlord.val = null;
        }
        if (subject != null) {
            subject.val = null;
        }
        if (date != null) {
            date.val = null;
        }
    }

    /**
     * Returns new event parsed by this State.
     * Called in {@link #endCompound(SaveGame)}
     * when adding to {@link SaveGame#addEvent(Date, Event)};
     * @return new event parsed by this Stat
     */
    protected abstract Event createSubjectEvent();

    @Override
    protected void endCompound(final SaveGame saveGame) {
        saveGame.addEvent(date.val, createSubjectEvent());
        saveGame.addSubject(subject.val);
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "first":
                return stringState.withOutput(overlord);
            case "second":
                return stringState.withOutput(subject);
            case "start_date":
                return dateState.withOutput(date);
            default:
                return ignore;
        }
    }
}
