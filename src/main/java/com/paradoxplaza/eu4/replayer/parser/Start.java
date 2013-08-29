package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Represents new starting state of TextParser.
 */
class Start extends State {

    /** State processing dates. */
    final DateState date = new DateState(this);

    /** State processing flags. */
    final Flags flags = new Flags(this);

    /** State ignoring everything till matching }. */
    final Ignore ignore = new Ignore(this);

    /** State processing emperors. */
    final Emperor emperor = new Emperor(this);

    /** State processing religions. */
    final Religions religions = new Religions(this);

    /** State processing provinces. */
    final Provinces provinces = new Provinces(this);

    /** Current date in save game. */
    final Ref<Date> currentDate = new Ref<>();

    /** Save game's starting date. */
    final Ref<Date> startDate = new Ref<>();

    /**
     * Only constructor. Sets start to null.
     */
    public Start() {
        super(null);
    }

    @Override
    public State end(final SaveGame saveGame) {
        if (currentDate.val == null) {
            throw new RuntimeException("Current date was not set!");
        }
        if (startDate.val == null) {
            throw new RuntimeException("Start date was not set!");
        }
        saveGame.date = currentDate.val;
        saveGame.startDate = startDate.val;
        return this;
    }

    @Override
    public State processChar(final SaveGame saveGame, final char token) {
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "date|start_date|flags|old_emperor|religions|provinces"));
    }

    @Override
    public State processWord(final SaveGame saveGame, final String word) {
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
