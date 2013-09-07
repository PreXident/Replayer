package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Building;
import com.paradoxplaza.eu4.replayer.events.Capital;
import com.paradoxplaza.eu4.replayer.events.City;
import com.paradoxplaza.eu4.replayer.events.Claim;
import com.paradoxplaza.eu4.replayer.events.ColonySize;
import com.paradoxplaza.eu4.replayer.events.Core;
import com.paradoxplaza.eu4.replayer.events.Culture;
import com.paradoxplaza.eu4.replayer.events.Event;
import com.paradoxplaza.eu4.replayer.events.Garrison;
import com.paradoxplaza.eu4.replayer.events.Goods;
import com.paradoxplaza.eu4.replayer.events.Hre;
import com.paradoxplaza.eu4.replayer.events.Manpower;
import com.paradoxplaza.eu4.replayer.events.Name;
import com.paradoxplaza.eu4.replayer.events.NativeFerocity;
import com.paradoxplaza.eu4.replayer.events.NativeHostileness;
import com.paradoxplaza.eu4.replayer.events.NativeSize;
import com.paradoxplaza.eu4.replayer.events.Owner;
import com.paradoxplaza.eu4.replayer.events.RevoltRisk;
import com.paradoxplaza.eu4.replayer.events.Tax;
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

    /** Adds addCore to savegame. */
    final SimpleWriteListener addCore = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Core(id, name, word, Core.ADDED);
        }
    };

    /** Adds new owner to savegame. */
    final SimpleWriteListener owner = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Owner(id, name, word);
        }
    };

    /** Adds buildings to savegame. */
    final SimpleWriteListener building = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Building(id, name, processing, word);
        }
    };

    /** Adds change of culture to savegame. */
    final SimpleWriteListener culture = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Culture(id, name, word);
        }
    };

    /** Adds change of religion to savegame. */
    final SimpleWriteListener religion = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new com.paradoxplaza.eu4.replayer.events.Religion(id, name, word);
        }
    };

    /** Adds change of hre to savegame. */
    final SimpleWriteListener hre = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Hre(id, name, word);
        }
    };

    /** Adds change of tax to savegame. */
    final SimpleWriteListener tax = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Tax(id, name, word);
        }
    };

    /** Adds change of goods to savegame. */
    final SimpleWriteListener goods = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Goods(id, name, word);
        }
    };

    /** Adds change of goods to savegame. */
    final SimpleWriteListener manpower = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Manpower(id, name, word);
        }
    };

    /** Adds change of goods to savegame. */
    final SimpleWriteListener capital = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Capital(id, name, word);
        }
    };

    /** Adds colony to city change to savegame. */
    final SimpleWriteListener city = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new City(id, name, word);
        }
    };

    /** Adds garrison change to savegame. */
    final SimpleWriteListener garrison = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Garrison(id, name, word);
        }
    };

    /** Adds native size change to savegame. */
    final SimpleWriteListener nativeSize = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new NativeSize(id, name, word);
        }
    };

    /** Adds native ferocity change to savegame. */
    final SimpleWriteListener nativeFerocity = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new NativeFerocity(id, name, word);
        }
    };

    /** Adds native hostileness change to savegame. */
    final SimpleWriteListener nativeHostileness = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new NativeHostileness(id, name, word);
        }
    };

    /** Adds native hostileness change to savegame. */
    final SimpleWriteListener colonySize = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new ColonySize(id, name, word);
        }
    };

    /** Adds revolt risk change to savegame. */
    final SimpleWriteListener revoltRisk = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new RevoltRisk(id, name, word);
        }
    };

    /** Adds core change to savegame. */
    final SimpleWriteListener removeCore = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Core(id, name, word, Core.REMOVED);
        }
    };

    /** Adds claim change to savegame. */
    final SimpleWriteListener addClaim = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Claim(id, name, word, Claim.ADDED);
        }
    };

    /** Adds revolt risk change to savegame. */
    final SimpleWriteListener removeClaim = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Claim(id, name, word, Claim.REMOVED);
        }
    };

    /** Adds name change to savegame. */
    final SimpleWriteListener provName = new SimpleWriteListener() {
        @Override
        protected Event createEvent(final String word) {
            return new Name(id, name, word);
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
                return stringState.withOutput(addCore);
            case "owner":
                return stringState.withOutput(owner);
            case "controller":
                return controller.withID(id).withName(name).withDate(date);
            case "culture":
                return stringState.withOutput(culture);
            case "religion":
                return stringState.withOutput(religion);
            case "hre":
                return stringState.withOutput(hre);
            case "base_tax":
                return stringState.withOutput(tax);
            case "trade_goods":
                return stringState.withOutput(goods);
            case "manpower":
                return stringState.withOutput(manpower);
            case "capital":
                return stringState.withOutput(capital);
            case "is_city":
                return stringState.withOutput(city);
            case "garrison":
                return stringState.withOutput(garrison);
            case "native_size":
                return stringState.withOutput(nativeSize);
            case "native_ferocity":
                return stringState.withOutput(nativeFerocity);
            case "native_hostileness":
                return stringState.withOutput(nativeHostileness);
            case "colonysize":
                return stringState.withOutput(colonySize);
            case "revolt_risk":
                return stringState.withOutput(revoltRisk);
            case "remove_core":
                return stringState.withOutput(removeCore);
            case "add_claim":
                return stringState.withOutput(addClaim);
            case "remove_claim":
                return stringState.withOutput(removeClaim);
            case "name":
                return stringState.withOutput(provName);
            case "advisor":
            case "revolt":
            case "discovered_by":
            case "citysize":
                return ignore;
            default:
                return stringState.withOutput(building);
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
