package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Parses vassal={...}.
 */
public class Vassal extends Subject {

    /**
     * Only constructor
     * @param parent parent state
     */
    public Vassal(State<SaveGame> parent) {
        super(parent);
    }

    @Override
    protected Event createSubjectEvent() {
        return new com.paradoxplaza.eu4.replayer.events.Vassal(subject.val, overlord.val);
    }
}
