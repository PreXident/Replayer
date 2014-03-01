package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.DateState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Represents new starting state of TextParser.
 */
class Start extends StartAdapter<SaveGame> {

    /** State processing dates. */
    final DateState<SaveGame> date = new DateState<>(this);

    /** State processing flags. */
    final Flags flags = new Flags(this);

    /** State ignoring everything till matching }. */
    final Ignore<SaveGame> ignore = new Ignore<>(this);

    /** State processing emperors. */
    final Emperor emperor = new Emperor(this);

    /** State processing religions. */
    final Religions religions = new Religions(this);

    /** State processing provinces. */
    final Provinces provinces = new Provinces(this);

    /** State processing countries. */
    final Countries countries = new Countries(this);

    /** State processing diplomacy. */
    final Diplomacy diplomacy = new Diplomacy(this);

    /** Current date in save game. */
    final Ref<Date> currentDate = new Ref<>();

    /** Save game's starting date. */
    final Ref<Date> startDate = new Ref<>();

    @Override
    public State<SaveGame> end(final SaveGame saveGame) {
        if (currentDate.val == null) {
            throw new RuntimeException(l10n("parser.savegame.error.currdate"));
        }
        if (startDate.val == null) {
            throw new RuntimeException(l10n("parser.savegame.error.startdate"));
        }
        saveGame.date = currentDate.val;
        saveGame.startDate = startDate.val;
        return this;
    }

    @Override
    public State<SaveGame> processChar(final SaveGame saveGame, final char token) {
        throw new RuntimeException(String.format(l10n(INVALID_TOKEN_EXPECTED_KEYWORD), token, "date|start_date|flags|old_emperor|religions|provinces|countries|diplomacy"));
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        switch (word) {
            case "EU4txt":
                return this;
            case "date":
                return date.withOutput(currentDate);
            case "start_date":
                return date.withOutput(startDate);
            case "flags":
                return flags;
            case "old_emperor":
                return emperor;
            case "religions":
                return religions;
            case "provinces":
                return provinces;
            case "countries":
                return countries.withDate(currentDate.val);
            case "diplomacy":
                return diplomacy;
            default:
                return ignore;
        }
    }

    @Override
    protected void reset() {
        //values could be null because reset() is called in super constructor
        if (currentDate != null) {
            currentDate.setVal(null);
        }
        if (startDate != null) {
            startDate.setVal(null);
        }
    }
}
