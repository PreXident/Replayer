package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Controller;
import com.paradoxplaza.eu4.replayer.events.Culture;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.events.Religion;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Processed ID={...}.
 */
class Province extends CompoundState<SaveGame> {

    /** Province id. */
    String id;

    /** Current date in save game. */
    Date currentDate;

    /**
     * Province name.
     * It needs to be this crazy reference to reference to string because of
     * province possible province name changes that are parsed after some events
     * are already created.
     */
    Ref<Ref<String>> name = new Ref<>(new Ref<String>());

    /** Province owner. */
    Ref<String> owner = new Ref<>();

    /** Province controller. */
    Ref<String> controller = new Ref<>();

    /** Province religion. */
    Ref<String> religion = new Ref<>();

    /** Province culture. */
    Ref<String> culture = new Ref<>();

    /** State parsing string info. */
    StringState<SaveGame> stringState = new StringState<>(this);

    /** State processing province history. */
    ProvinceHistory history = new ProvinceHistory(this);

    /** State ignoring uninteresting data. */
    Ignore<SaveGame> ignore = new Ignore<>(this);

    /**
     * Only constructor
     * @param start parent state
     */
    public Province(final State<SaveGame> start) {
        super(start);
    }

    /**
     * Sets current date.
     * @param date new date
     * @return this
     */
    public Province withDate(final Date date) {
        this.currentDate = date;
        return this;
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
            name.val = new Ref<>();
        }
        if (owner != null) {
            owner.val = null;
        }
        if (controller != null) {
            controller.val = null;
        }
        if (religion != null) {
            religion.val = null;
        }
        if (culture != null) {
            culture.val = null;
        }
    }

    @Override
    protected void endCompound(final SaveGame saveGame) {
        if (SaveGameParser.synchronizeProvinces) {
            saveGame.addEvent(currentDate, new Owner(id, name.val, owner.val));
            saveGame.addEvent(currentDate, new Controller(id, name.val, owner.val, null));
            saveGame.addEvent(currentDate, new Religion(id, name.val, religion.val));
            saveGame.addEvent(currentDate, new Culture(id, name.val, culture.val));
        }
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "name":
                return stringState.withOutput(name.val);
            case "history":
                return history.withID(id).withName(name);
            case "owner":
                return stringState.withOutput(owner);
            case "controller":
                return stringState.withOutput(controller);
            case "culture":
                return stringState.withOutput(culture);
            case "religion":
                return stringState.withOutput(religion);
            default:
                return ignore;
        }
    }
}
