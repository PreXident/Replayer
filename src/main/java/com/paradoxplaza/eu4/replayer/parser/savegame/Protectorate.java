package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Parses protectorate={...}.
 */
public class Protectorate extends Subject {

    /**
     * Only constructor
     * @param parent parent state
     */
    public Protectorate(State<SaveGame> parent) {
        super(parent);
    }

    @Override
    protected Event createSubjectEvent() {
        return new com.paradoxplaza.eu4.replayer.events.Protectorate(subject.val, overlord.val);
    }
}
