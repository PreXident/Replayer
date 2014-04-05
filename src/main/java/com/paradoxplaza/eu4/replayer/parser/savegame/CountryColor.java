package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Utils;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Parses color={ NUM NUM NUM }.
 */
public class CountryColor extends CompoundState<SaveGame> {

    /** Country tag. */
    String country;

    /** Which color is expected. */
    int colorIndex;

    /** RGB. */
    int[] colors = new int[3];

    /**
     * Only constructor.
     * @param parent parent state
     */
    public CountryColor(final State<SaveGame> parent) {
        super(parent);
    }

    /**
     * Sets tag of the country.
     * @param country new tag
     * @return this
     */
    public CountryColor withCountry(final String country) {
        this.country = country;
        return this;
    }

    @Override
    protected final void compoundReset() {
        colorIndex = 0;
    }

    @Override
    protected void endCompound(final SaveGame saveGame) {
        saveGame.getDynamicCountriesColors().put(country, Utils.toColor(colors[0], colors[1], colors[2]));
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        colors[colorIndex++] = Integer.parseInt(word);
        return this;
    }
}
