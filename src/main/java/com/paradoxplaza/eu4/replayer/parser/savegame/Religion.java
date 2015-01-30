package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Defender;
import com.paradoxplaza.eu4.replayer.events.EnableReligion;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.DateState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Parses religion={...}.
 */
class Religion extends CompoundState<SaveGame> {

    /** Identifier of currently processed religion. */
    String name;

    /** Tag of currently processed religion's defender. */
    final Ref<String> defender = new Ref<>();

    /** Date of currently processed religion's defender. */
    final Ref<Date> defenderDate = new Ref<>();

    /** Enable date of currently processed religion. */
    final Ref<Date> enable = new Ref<>();

    /** State to process defender keyword. */
    final StringState<SaveGame> defenderState = new StringState<>(this).withOutput(defender);

    /** State to process enable and defender_date keywords. */
    final DateState<SaveGame> dateState = new DateState<>(this);

    /** State ignoring everything till matching }. */
    final Ignore<SaveGame> ignore = new Ignore<>(this);

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Religion(final State<SaveGame> parent) {
        super(parent);
    }

    /**
     * Sets name of currenctly processed religion.
     * @param name name of currenly processed religion
     * @return this
     */
    public Religion withName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    protected final void compoundReset() {
        //values could be null because reset() is called in super constructor
        if (defender != null) {
            defender.val = null;
        }
        if (defenderDate != null) {
            defenderDate.val = null;
        }
        if (enable != null) {
            enable.val = null;
        }
    }

    @Override
    protected void endCompound(final SaveGame saveGame) {
        if (enable.val != null) {
            saveGame.addEvent(enable.val, new EnableReligion(name));
        }
        if (defender.val != null && defenderDate.val != null) {
            saveGame.addEvent(defenderDate.val, new Defender(name, defender.val));
        }
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "league":
            case "papacy":
            case "invest_in_cardinal":
            case "hre_religion":
            case "total_centers":
            case "reformation_center":
            case "hre_heretic_religion":
            case "original_hre_religion":
            case "original_hre_heretic_religion":
                return ignore;
            case "defender":
                return defenderState;
            case "defender_date":
                return dateState.withOutput(defenderDate);
            case "enable":
                return dateState.withOutput(enable);
            default:
                throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_KEYWORD), word, "defender|defender_date|enable"));
        }
    }
}
