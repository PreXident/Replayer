package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.NewEmperor;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Processes old_emperor = {...}.
 */
class Emperor extends CompoundState {

    /** Monarch's id. */
    Ref<String> id = new Ref<>();

    /** Country tag. */
    Ref<String> tag = new Ref<>();

    /** Coronation date. */
    Ref<Date> date = new Ref<>();

    /** State processing date. */
    DateState dateState = new DateState(this).withOutput(date);

    /** State processing both id and tag. */
    StringState stringState = new StringState(this);

    /**
     * Only constructor.
     * @param start parent state
     */
    public Emperor(final Start start) {
        super(start);
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
    public State processWord(final SaveGame saveGame, final String word) {
        if (expecting != Expecting.CLOSING) {
            throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, expecting));
        }
        switch (word) {
            case "id":
                return stringState.withOutput(id);
            case "country":
                return stringState.withOutput(tag);
            case "date":
                return dateState;
            default:
                throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, word, "id|country|date"));
        }
    }

    @Override
    protected void reset() {
        super.reset();
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
}
