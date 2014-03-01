package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Processes technology={...}.
 */
public class Technology extends CompoundState<SaveGame> {

    /** Country tag. */
    String country;

    /** Current date in save game. */
    Date currentDate;

    /** Administration tech. */
    final Ref<String> adm = new Ref<>();

    /** Diplomation tech. */
    final Ref<String> dip = new Ref<>();

    /** Military tech. */
    final Ref<String> mil = new Ref<>();

    /** State ignoring uninteresting info. */
    final Ignore<SaveGame> ignore = new Ignore<>(this);

    /** State processing string values. */
    final StringState<SaveGame> stringState = new StringState<>(this);

    /**
     * Only constructor
     * @param parent parent state
     */
    public Technology(final State<SaveGame> parent) {
        super(parent);
    }

    /**
     * Sets tag of the country.
     * @param country new tag
     * @return this
     */
    public Technology withCountry(final String country) {
        this.country = country;
        return this;
    }

    /**
     * Sets current date of the save game.
     * @param date new current date
     * @return this
     */
    public Technology withDate(final Date date) {
        this.currentDate = date;
        return this;
    }

    @Override
    protected void compoundReset() {
        if (adm != null) {
            adm.val = null;
        }
        if (dip != null) {
            dip.val = null;
        }
        if (mil != null) {
            mil.val = null;
        }
    }

    @Override
    protected void endCompound(final SaveGame saveGame) {
        final int adm_val = Integer.parseInt(adm.val);
        final int dip_val = Integer.parseInt(dip.val);
        final int mil_val = Integer.parseInt(mil.val);
        saveGame.addEvent(currentDate,
                new com.paradoxplaza.eu4.replayer.events.Technology(
                    country, adm_val, dip_val, mil_val));
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "adm_tech":
                return stringState.withOutput(adm);
            case "dip_tech":
                return stringState.withOutput(dip);
            case "mil_tech":
                return stringState.withOutput(mil);
            default:
                return ignore;
        }
    }
}
