package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Processes controller={...}.
 */
class Controller extends CompoundState<SaveGame> {

    /** Province id. */
    String id;

    /** Province name. */
    String name;

    /** Controller change date. */
    Date date;

    /** New controller tag. */
    Ref<String> tag = new Ref<>();

    /** New controller tag. */
    Ref<String> rebel = new Ref<>();

    /** Parses inner simple values. */
    StringState<SaveGame> inner = new StringState<>(this);

    public Controller(final State<SaveGame> start) {
        super(start);
    }

    /**
     * Sets province id.
     * @param id new province id
     * @return this
     */
    public Controller withID(final String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets province name
     * @param name new province name
     * @return this
     */
    public Controller withName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets new date for controller change.
     * @param date new date
     * @return this
     */
    public Controller withDate(final Date date) {
        this.date = date;
        return this;
    }

    @Override
    protected void compoundReset() {
        //values could be null because reset() is called in super constructor
        if (tag != null) {
            tag.val = null;
        }
        if (rebel != null) {
            rebel.val = null;
        }
        id = null;
        name = null;
        date = null;
    }

    @Override
    protected void endCompound(final SaveGame saveGame) {
        saveGame.addEvent(date, new com.paradoxplaza.eu4.replayer.events.Controller(id, name, tag.val, rebel.val));
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "controller":
                return inner.withOutput(tag);
            case "rebel":
                return inner.withOutput(rebel);
            default:
                throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_KEYWORD), word, "controller|rebel"));
        }
    }
}
