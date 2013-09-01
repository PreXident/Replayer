package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Building;
import com.paradoxplaza.eu4.replayer.events.Core;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import java.util.regex.Pattern;
import javafx.beans.value.WritableValue;

/**
 * Processes province history.
 */
class ProvinceHistory extends CompoundState<SaveGame> {

    /** Pattern of dates. */
    static final Pattern DATE = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+");

    /** Province id. */
    String id;

    /** Province name. */
    String name;

    /** Date of this history. */
    Date date;

    /** State for processing inner history (eg 1492.1.1={..}). */
    ProvinceHistory innerHistory;

    /** SaveGame to modify. */
    SaveGame saveGame;

    /** What part of history is currently being processed. */
    String processing;

    /** Adds add_core to savegame. */
    SimpleWriteListener add_core = new SimpleWriteListener() {
        @Override
        protected Event createEvent(String word) {
            return new Core(id, name, word, Core.ADDED);
        }
    };

    /** Adds new owner to savegame. */
    SimpleWriteListener owner = new SimpleWriteListener() {
        @Override
        protected Event createEvent(String word) {
            return new Owner(id, name, word);
        }
    };

    /** Adds buildings to savegame. */
    SimpleWriteListener building = new SimpleWriteListener() {
        @Override
        protected Event createEvent(String word) {
            return new Building(id, name, processing, word);
        }
    };

    /** State processsing simple events. */
    StringState<SaveGame> stringState = new StringState<>(this);

    /** State processing controller changes. */
    Controller controller = new Controller(this);

    /** State to ignore advisors. */
    Ignore<SaveGame> ignore = new Ignore<>(this);

    /**
     * Only constructor.
     * @param state parent state
     */
    public ProvinceHistory(final State<SaveGame> start) {
        super(start);
    }

    /**
     * Sets province id
     * @param id new province id
     * @return this
     */
    public ProvinceHistory withID(final String id) {
        this.id = id;
        return this;
    }

    /**
     * Returns lazy initialized innerHistory.
     * @return
     */
    protected ProvinceHistory getInnerHistory() {
        return innerHistory == null ? new ProvinceHistory(this) : innerHistory;
    }

    /**
     * Sets province name
     * @param name new province name
     * @return this
     */
    public ProvinceHistory withName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets history date.
     * @param date new history date
     * @return this
     */
    public ProvinceHistory withDate(final Date date) {
        this.date = date;
        return this;
    }

    @Override
    public void compoundReset() {
        id = null;
        name = null;
        date = null;
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        this.saveGame = saveGame;
        processing = word;
        if (DATE.matcher(word).matches()) {
            return getInnerHistory().withID(id).withName(name).withDate(new Date(word));
        }
        switch (word) {
            case "add_core":
                return stringState.withOutput(add_core);
            case "owner":
                return stringState.withOutput(owner);
            case "controller":
                return controller.withID(id).withName(name).withDate(date);
            case "advisor":
            case "revolt":
            default:
                return ignore;
//            default:
//                return stringState.withOutput(building);
        }
    }

    /**
     * Mimicks WritableValue, but adds event to saveGame when value is written.
     */
    abstract class SimpleWriteListener implements WritableValue<String> {

        @Override
        public final String getValue() {
            return null; //we remember nothing
        }

        @Override
        public final void setValue(final String word) {
            saveGame.addEvent(date, createEvent(word));
        }

        /**
         * Returns event to add to saveGame.
         * @param word input value
         * @return event to add to saveGame
         */
        protected abstract Event createEvent(final String word);
    }
}
