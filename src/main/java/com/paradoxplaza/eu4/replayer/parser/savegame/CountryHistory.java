package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.TagChange;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import java.util.regex.Pattern;
import javafx.beans.value.WritableValue;

/**
 * Processes country history.
 */
class CountryHistory extends CompoundState<SaveGame> {

    /** Pattern of dates. */
    static final Pattern DATE = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+");

    /** Country tag. */
    String tag;

    /** Date of this history. */
    Date date;

    /** State for processing inner history (eg 1492.1.1={..}). */
    CountryHistory innerHistory;

    /** SaveGame to modify. */
    SaveGame saveGame;

    /** Adds addCore to savegame. */
    final TagChangeWriteListener tagChange = new TagChangeWriteListener();

    /** State processsing simple events. */
    StringState<SaveGame> stringState = new StringState<>(this);

    /** State to ignore uninteresting events. */
    Ignore<SaveGame> ignore = new Ignore<>(this);

    /**
     * Only constructor.
     * @param state parent state
     */
    public CountryHistory(final State<SaveGame> start) {
        super(start);
    }

    /**
     * Sets history date.
     * @param date new history date
     * @return this
     */
    public CountryHistory withDate(final Date date) {
        this.date = date;
        return this;
    }

    /**
     * Sets country tag.
     * @param tag new country tag
     * @return this
     */
    public CountryHistory withTag(final String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Returns lazy initialized innerHistory.
     * @return
     */
    protected CountryHistory getInnerHistory() {
        return innerHistory == null ? new CountryHistory(this) : innerHistory;
    }

    @Override
    public void compoundReset() {
        tag = null;
        date = null;
    }

    @Override
    public State<SaveGame> processWord(final SaveGame saveGame, final String word) {
        this.saveGame = saveGame;
        if (DATE.matcher(word).matches()) {
            return getInnerHistory().withTag(tag).withDate(new Date(word));
        }
        switch (word) {
            case "changed_tag_from":
                return stringState.withOutput(tagChange);
            default:
                return ignore;
        }
    }

    /**
     * Mimicks WritableValue, but adds event to saveGame when value is written.
     */
    class TagChangeWriteListener implements WritableValue<String> {

        @Override
        public final String getValue() {
            return null; //we remember nothing
        }

        @Override
        public final void setValue(final String word) {
            saveGame.addEvent(date, new TagChange(tag, word));
            saveGame.tagChanges.put(tag, date);
        }
    }
}
