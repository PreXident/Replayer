package com.paradoxplaza.eu4.replayer.parser;

/**
 * Processes empty { }, so it expects closing }.
 */
public class Empty<Context> extends Ignore<Context> {

    public Empty(final State<Context> parent) {
        super(parent);
    }

    @Override
    public void reset() {
        super.reset();
        expecting = Expecting.CLOSING;
    }
}
