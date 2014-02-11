package com.paradoxplaza.eu4.replayer.parser.defines;

import com.paradoxplaza.eu4.replayer.DefinesInfo;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.NumberState;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Parses NCountry={...}.
 */
public class Country extends CompoundState<DefinesInfo> {

    /** Holds MAX_CROWN_COLONIES value. */
    final Ref<Double> MAX_CROWN_COLONIES = new Ref<>(Double.MAX_VALUE);

    /** Parses numbers. */
    final NumberState<DefinesInfo> numberState = new NumberState<>(this);

    /** State ignoring unintersting data. */
    final Ignore<DefinesInfo> ignore = new Ignore<>(this);

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Country(final State<DefinesInfo> parent) {
        super(parent);
    }

    @Override
    protected void endCompound(final DefinesInfo context) {
        context.MAX_CROWN_COLONIES = Math.round(MAX_CROWN_COLONIES.val);
    }

    @Override
    public State<DefinesInfo> processChar(final DefinesInfo context, final char token) {
        // ignore commas
        if (token == ',') {
            return this;
        }
        return super.processChar(context, token);
    }

    @Override
    public State<DefinesInfo> processWord(final DefinesInfo context, final String word) {
        if (word.equals("MAX_CROWN_COLONIES")) {
            return numberState.withOutput(MAX_CROWN_COLONIES);
        } else {
            return ignore;
        }
    }
}
