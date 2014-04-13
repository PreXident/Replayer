package com.paradoxplaza.eu4.replayer.parser.savegame;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.TagChange;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Empty;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import com.paradoxplaza.eu4.replayer.utils.Ref;
import com.paradoxplaza.eu4.replayer.utils.WritableValue;
import java.util.regex.Pattern;

/**
 * Processes country history.
 */
class CountryHistory extends CompoundState<SaveGame> {

    /** Pattern of dates. */
    static final Pattern DATE = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+");

    /** Country tag. */
    Ref<String> tag;

    /** Date of this history. */
    Date date;

    /** State for processing inner history (eg 1492.1.1={..}). */
    CountryHistory innerHistory;

    /** SaveGame to modify. */
    SaveGame saveGame;

    /** Adds addCore to savegame. */
    final TagChangeWriteListener tagChange = new TagChangeWriteListener();

    /** State processsing simple events. */
    final StringState<SaveGame> stringState = new StringState<>(this);

    /** State to ignore uninteresting events. */
    final Ignore<SaveGame> ignore = new Ignore<>(this);

    /** State to ignore empty { }. */
    final Empty<SaveGame> empty = new Empty<>(this);

    /**
     * Only constructor.
     * @param parent parent state
     */
    public CountryHistory(final CompoundState<SaveGame> parent) {
        super(parent);
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
    public CountryHistory withTag(final Ref<String> tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Returns lazy initialized innerHistory.
     * @return lazy initialized innerHistory
     */
    protected CountryHistory getInnerHistory() {
        return innerHistory == null ? new CountryHistory(this) : innerHistory;
    }

    @Override
    public void compoundReset() {
        tag = new Ref<>();
        date = null;
    }

    @Override
    public State<SaveGame> processChar(final SaveGame context, final char token) {
        if (token != expecting.toChar() && expecting == Expecting.CLOSING && token == '{') {
            return empty;
        }
        return super.processChar(saveGame, token);
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
        public final void setValue(final String word) {
            if (word.equals(tag.val)) {
                return;
            }
            final Ref<String> oldTag = tag;
            final Ref<String> newTag = new Ref<>(oldTag.val);
            oldTag.val = word;
            tag = newTag;
            if (parent instanceof CountryHistory) {
                ((CountryHistory) parent).tag = newTag;
            }
            final TagChange tagChange = new TagChange(newTag, word);
            saveGame.addEvent(date, tagChange);
            saveGame.tagChanges.add(new Pair<>(date, tagChange));
        }
    }
}
