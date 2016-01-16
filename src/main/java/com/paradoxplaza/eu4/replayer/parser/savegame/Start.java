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
    final SaveGameDateWriter currentDate = new SaveGameDateWriter() {
        @Override
        protected void setSaveGameDate(final Date val) {
            saveGame.date = val;
        }
    };

    /** Save game's starting date. */
    final SaveGameDateWriter startDate = new SaveGameDateWriter() {
        @Override
        protected void setSaveGameDate(final Date val) {
            saveGame.startDate = val;
        }
    };

    @Override
    public State<SaveGame> end(final SaveGame saveGame) {
        if (currentDate.val == null) {
            throw new RuntimeException(l10n("parser.savegame.error.currdate"));
        }
        if (startDate.val == null) {
            throw new RuntimeException(l10n("parser.savegame.error.startdate"));
        }
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
                return date.withOutput(currentDate.withSaveGame(saveGame));
            case "start_date":
                return date.withOutput(startDate.withSaveGame(saveGame));
            case "flags":
                return flags;
            case "old_emperor":
                return emperor;
            case "religions":
                return religions;
            case "provinces":
                return provinces.withDate(currentDate.val);
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
    
    /**
     * Simply writes date also to propriate field of the save game.
     */
    protected abstract class SaveGameDateWriter extends Ref<Date> {
        
        /** Currently processed save game. */
        protected SaveGame saveGame = null;

        /**
         * Sets appropriate date field of the save game class.
         * @param val parsed date
         */
        protected abstract void setSaveGameDate(final Date val);
        
        @Override
        public void setVal(final Date val) {
            setSaveGameDate(val);
            this.val = val;
        }
        
        /**
         * Sets save game.
         * @param saveGame new save game
         * @return this
         */
        public SaveGameDateWriter withSaveGame(final SaveGame saveGame) {
            this.saveGame = saveGame;
            return this;
        }
    }
}
