package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Parses protectorate={...}.
 */
public class Colonial extends Subject {

    /**
     * Only constructor
     * @param parent parent state
     */
    public Colonial(State<SaveGame> parent) {
        super(parent);
    }

    @Override
    protected Event createSubjectEvent() {
        return new com.paradoxplaza.eu4.replayer.events.Colonial(subject.val, overlord.val);
    }
}
