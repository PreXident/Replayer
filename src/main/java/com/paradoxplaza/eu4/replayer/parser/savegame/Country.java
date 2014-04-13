package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.TagChange;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import com.paradoxplaza.eu4.replayer.utils.Ref;
import java.util.ArrayList;
import java.util.List;

/**
 * Processed TAG={...}.
 */
class Country extends CompoundState<SaveGame> {

    /** Country tag. */
    String tag;

    /** Current date in save game. */
    Date currentDate;

    /** State processing country history. */
    CountryHistory history = new CountryHistory(this);

    /** State ignoring uninteresting data. */
    Ignore<SaveGame> ignore = new Ignore<>(this);

    /** State processing dynamic countries' colors. */
    CountryColor countryColor = new CountryColor(this);

    /** State processing technology levels. */
    Technology technology = new Technology(this);

    /**
     * Only constructor
     * @param start parent state
     */
    public Country(final State<SaveGame> start) {
        super(start);
    }

    /**
     * Sets current date.
     * @param date new date
     * @return this
     */
    public Country withDate(final Date date) {
        this.currentDate = date;
        return this;
    }

    /**
     * Sets country tag.
     * @param tag new tag
     * @return this
     */
    public Country withTag(final String tag) {
        this.tag = tag;
        return this;
    }

    @Override
    protected void compoundReset() {
        tag = null;
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "history":
                return history.withTag(new Ref<>(tag));
            case "map_color":
                return countryColor.withCountry(tag);
            case "technology":
                return technology.withCountry(tag).withDate(currentDate);
            default:
                return ignore;
        }
    }
}
