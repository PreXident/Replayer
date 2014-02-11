package com.paradoxplaza.eu4.replayer.parser.defines;

import com.paradoxplaza.eu4.replayer.DefinesInfo;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;

/**
 * Parses NDefines={...}.
 */
class Defines extends CompoundState<DefinesInfo> {

    /** Parses NCountry. */
    final Country country = new Country(this);

    /** Ignores uninteresting info. */
    Ignore<DefinesInfo> ignore = new Ignore<>(this);

    /**
     * Only constructor.
     * @param parent parent state
     */
    public Defines(final State<DefinesInfo> parent) {
        super(parent);
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
        switch (word) {
            case "NCountry":
                return country;
            default:
                return ignore;
        }
    }
}
