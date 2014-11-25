package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Processes countries={...}.
 */
class Countries extends CompoundState<SaveGame> {

    /** State processing individual provinces. */
    final Country country = new Country(this);

    /** Current date in save game. */
    Date currentDate;

    /**
     * Only constructor
     * @param parent parent state
     */
    public Countries(final State<SaveGame> parent) {
        super(parent);
    }

    /**
     * Sets current date.
     * @param date new current date
     * @return this
     */
    public Countries withDate(final Date date) {
        this.currentDate = date;
        return this;
    }

    @Override
    public State<SaveGame> processChar(final SaveGame saveGame, final char token) {
        //since 1.8.1 the first country is not ---, but entirely empty tag
        if (token == '=' && expecting.toChar() == '}') {
            country.withTag("---").withDate(currentDate); //prepare the country
            country.processChar(saveGame, token); //process =
            return country; //and continue
        }
        return super.processChar(saveGame, token);
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        return country.withTag(word).withDate(currentDate);
    }
}
