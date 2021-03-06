package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.regex.Pattern;

/**
 * Processes provinces={...}.
 */
class Provinces extends CompoundState<SaveGame> {

    /** Province ID patter. */
    static final Pattern NUMBER = Pattern.compile("-[0-9]+");

    /** State processing individual provinces. */
    final Province province = new Province(this);

    /** Current date in save game. */
    Date currentDate;

    /**
     * Only constructor
     * @param parent parent state
     */
    public Provinces(final State<SaveGame> parent) {
        super(parent);
    }

    /**
     * Sets current date.
     * @param date new current date
     * @return this
     */
    public Provinces withDate(final Date date) {
        this.currentDate = date;
        return this;
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        if (!NUMBER.matcher(word).matches()) {
            throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_VALUE), word, "-NUMBER"));
        }
        return province.withID(word.substring(1)).withDate(currentDate);
    }
}
