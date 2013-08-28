package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Defender;
import com.paradoxplaza.eu4.replayer.events.EnableReligion;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Parses religion={...}.
 */
class Religion extends CompoundState {

    /** Identifier of currently processed religion. */
    String name;

    /** Tag of currently processed religion's defender. */
    final Ref<String> defender = new Ref<>();

    /** Date of currently processed religion's defender. */
    final Ref<Date> defenderDate = new Ref<>();

    /** Enable date of currently processed religion. */
    final Ref<Date> enable = new Ref<>();

    /** State to process defender keyword. */
    final StringState defenderState = new StringState(this).withOutput(defender);

    /** State to process enable and defender_date keywords. */
    final DateState dateState = new DateState(this);

    /**
     * Only constructor.
     * @param start parent state
     */
    public Religion(final State start) {
        super(start);
    }

    /**
     * Sets name of currenctly processed religion.
     * @param name name of currenly processed religion
     * @return this
     */
    public Religion withName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    protected void endCompound(final SaveGame saveGame) {
        if (enable.val != null) {
            saveGame.addEvent(enable.val, new EnableReligion(name));
        }
        if (defender.val != null && defenderDate.val != null) {
            saveGame.addEvent(defenderDate.val, new Defender(name, defender.val));
        }
    }

    @Override
    public State processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "defender":
                return defenderState;
            case "defender_date":
                return dateState.withOutput(defenderDate);
            case "enable":
                return dateState.withOutput(enable);
            default:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, "defender|defender_date|enable"));
        }
    }

    @Override
    protected final void reset() {
        super.reset();
        //values could be null because reset() is called in super constructor
        if (defender != null) {
            defender.val = null;
        }
        if (defenderDate != null) {
            defenderDate.val = null;
        }
        if (enable != null) {
            enable.val = null;
        }
    }
}
