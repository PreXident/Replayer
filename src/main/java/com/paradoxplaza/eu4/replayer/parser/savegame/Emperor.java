package com.paradoxplaza.eu4.replayer.parser.savegame;

import java.util.Date;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.NewEmperor;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.DateState;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Processes old_emperor = {...}.
 */
class Emperor extends CompoundState<SaveGame> {

    /** Monarch's id. */
    final Ref<String> id = new Ref<>();

    /** Country tag. */
    final Ref<String> tag = new Ref<>();

    /** Coronation date. */
    final Ref<Date> date = new Ref<>();

    /** State processing date. */
    final DateState<SaveGame> dateState = new DateState<>(this).withOutput(date);

    /** State processing both id and tag. */
    final StringState<SaveGame> stringState = new StringState<>(this);

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Emperor(final State<SaveGame> parent) {
        super(parent);
    }

    @Override
    protected void compoundReset() {
        //values could be null because reset() is called in super constructor
        if (id != null) {
            id.val = null;
        }
        if (tag != null) {
            tag.val = null;
        }
        if (date != null) {
            date.val = null;
        }
    }

    /**
     * If any flag is processed, it's inserted into saveGame.
     * @param saveGame SaveGame to modify
     */
    @Override
    protected void endCompound(final SaveGame saveGame) {
        if (id.val != null && tag.val != null && date.val != null) {
            saveGame.addEvent(date.val, new NewEmperor(id.val, tag.val));
        } else {
            throw new RuntimeException("Incomplete old_emperor!");
        }
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        if (expecting != Expecting.CLOSING) {
            throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, expecting));
        }
        switch (word) {
            case "id":
                return stringState.withOutput(id);
            case "country":
                return stringState.withOutput(tag);
            case "date":
                return dateState.withOutput(date);
            default:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, "id|country|date"));
        }
    }
}
