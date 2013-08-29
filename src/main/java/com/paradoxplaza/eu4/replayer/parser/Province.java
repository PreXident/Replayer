package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Processed ID={...}.
 */
class Province extends CompoundState {

    /** Province id. */
    String id;

    /** Province name. */
    Ref<String> name = new Ref<>();

    /** State parsing province name. */
    StringState nameState = new StringState(this).withOutput(name);

    /** State processing province history. */
    ProvinceHistory history = new ProvinceHistory(this);

    /** State ignoring uninteresting data. */
    Ignore ignore = new Ignore(this);

    /**
     * Only constructor
     * @param start parent state
     */
    public Province(final State start) {
        super(start);
    }

    /**
     * Sets province id.
     * @param id new id
     * @return this
     */
    public Province withID(final String id) {
        this.id = id;
        return this;
    }

    @Override
    protected void compoundReset() {
        id = null;
        //value could be null because reset() is called in super constructor
        if (name != null) {
            name.val = null;
        }
    }

    @Override
    public State processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "name":
                return nameState;
            case "history":
                return history.withID(id).withName(name.val);
            default:
                return ignore;
        }
    }
}
