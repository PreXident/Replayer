package com.paradoxplaza.eu4.replayer.parser.religion;

import com.paradoxplaza.eu4.replayer.ReplayerController;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.Map;

/**
 * Parses color={ NUM NUM NUM }.
 */
public class Color extends CompoundState<Map<String, Integer>> {

    /** Religion name. */
    String religion;

    /** Which color is expected. */
    int colorIndex;

    /** RGB. */
    int[] colors = new int[3];

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Color(final State<Map<String, Integer>> parent) {
        super(parent);
    }

    /**
     * Sets name of the religion.
     * @param religion new name
     * @return this
     */
    public Color withReligion(final String religion) {
        this.religion = religion;
        return this;
    }

    @Override
    protected final void compoundReset() {
        colorIndex = 0;
    }

    @Override
    protected void endCompound(final Map<String, Integer> religions) {
        religions.put(religion, ReplayerController.toColor(colors[0], colors[1], colors[2]));
    }

    @Override
    public State<Map<String, Integer>> processWord(final Map<String, Integer> saveGame, final String word) {
        colors[colorIndex++] = (int) (Double.parseDouble(word) * 255);
        return this;
    }
}
