package com.paradoxplaza.eu4.replayer.parser.country;

import com.paradoxplaza.eu4.replayer.Utils;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Parses color={ NUM NUM NUM }.
 */
public class Color extends CompoundState<Ref<Integer>> {

    /** Which color is expected. */
    int colorIndex;

    /** RGB. */
    int[] colors = new int[3];

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Color(final State<Ref<Integer>> parent) {
        super(parent);
    }

    @Override
    protected final void compoundReset() {
        colorIndex = 0;
    }

    @Override
    protected void endCompound(final Ref<Integer> color) {
        color.val = Utils.toColor(colors[0], colors[1], colors[2]);
    }

    @Override
    public State<Ref<Integer>> processWord(final Ref<Integer> color, final String word) {
        colors[colorIndex++] = Integer.parseInt(word);
        return this;
    }
}
