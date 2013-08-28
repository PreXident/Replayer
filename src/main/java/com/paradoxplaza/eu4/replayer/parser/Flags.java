package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Flag;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Processes global flags.
 */
class Flags extends CompoundState {

    /** Identifier of currently processed flag. */
    String flag;

    /** Date of currently processed flag. */
    final Ref<Date> date = new Ref<>();

    /** State to process individual flags. */
    final DateState inFlags = new DateState(this).withOutput(date);

    /**
     * Only constructor.
     * @param start parent state
     */
    public Flags(final State start) {
        super(start);
    }

    /**
     * If any flag is processed, it's inserted into saveGame.
     * @param saveGame SaveGame to modify
     */
    @Override
    protected void endCompound(final SaveGame saveGame) {
        if (flag != null && date.val != null) {
            saveGame.addEvent(date.val, new Flag(flag));
        }
    }

    @Override
    public State processWord(final SaveGame saveGame, final String word) {
        endCompound(saveGame);
        flag = word;
        return inFlags;
    }

    @Override
    protected final void reset() {
        super.reset();
        flag = null;
        //value could be null because reset() is called in super constructor
        if (date != null) {
            date.val = null;
        }
    }
}
