package com.paradoxplaza.eu4.replayer.parser;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.events.Flag;
import com.paradoxplaza.eu4.replayer.events.NewEmperor;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Represents state of the TextParser.
 */
abstract class State {

    /** Error message. */
    static protected final String INVALID_TOKEN_EXPECTED_VALUE = "Invalid token \"%1$s\" after date, expected %2$s";

    /** Error message. */
    static protected final String INVALID_TOKEN_EXPECTED_KEYWORD = "Invalid token \"%1$s\" after date, expected \"%2$s\"";

    /**
     * Returns new starting state.
     * @return new starting state
     */
    static public State newStart() {
        return new Start();
    }

    /** Intented for subclasses. Parent state to which the control should return. */
    final State start;

    /**
     * Only constructor. Sets start field.
     * @param start
     */
    protected State(final State start) {
        this.start = start;
        reset();
    }

    /**
     * Processes end of file.
     * @param saveGame SaveGame to apply changes
     * @return new state
     * @throws RuntimeException if end of file was unexpected or some info is missing
     */
    public State end(final SaveGame saveGame) {
        throw new RuntimeException("Unexpected end of file!");
    }

    /** Processes charancter and changes savegame.
     * @param saveGame SaveGame to apply changes
     * @param char token from input
     * @return new state
     */
    public State processChar(final SaveGame saveGame, final char token) {
        return this;
    }

    /** Processes word and changes savegame.
     * @param saveGame SaveGame to apply changes
     * @param word token from input
     * @return new state
     */
    public State processWord(final SaveGame saveGame, final String word) {
        return this;
    }

    /**
     * Resets inner state. Intented to be overridden.
     */
    protected void reset() {
        //nothing to reset
    }
}
