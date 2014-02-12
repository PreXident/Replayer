package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Parses diplomacy={...}.
 */
public class Diplomacy extends CompoundState<SaveGame> {

    /** State processing vassals. */
    final Vassal vassal = new Vassal(this);

    /** State processing vassals. */
    final Protectorate protectorate = new Protectorate(this);

    /** State processing colonial nations. */
    final Colonial colonial = new Colonial(this);

    /** State ignoring uninteresting diplomacy. */
    final Ignore<SaveGame> ignore = new Ignore<>(this);

    /**
     * Only constructor
     * @param parent parent state
     */
    public Diplomacy(final State<SaveGame> parent) {
        super(parent);
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "vassal":
                return vassal;
            case "protectorate":
                return protectorate;
            case "is_colonial":
                return colonial;
            default:
                return ignore;
        }
    }
}
