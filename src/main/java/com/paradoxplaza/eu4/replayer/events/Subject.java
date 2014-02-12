package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;

/**
 * Country becomes a subject. Ancestor for Vassal, Colonial, Protectorate etc.
 */
@AlwaysNotable
public abstract class Subject extends Event {

    /** Subject country tag. */
    public final String tag;

    /** New overlord tag. */
    public final String newOverlord;

    /** Old overlord tag. */
    public String oldOverlord;

    /**
     * Only construtor.
     * @param tag country tag
     * @param newOverlord new overloard tag
     */
    public Subject(final String tag, final String newOverlord) {
        this.tag = tag;
        this.newOverlord = newOverlord;
    }

    @Override
    public boolean beProcessed(final Date date, final EventProcessor processor) {
        return processor.process(date, this);
    }

    @Override
    public boolean beUnprocessed(final Date date, final EventProcessor processor) {
        return processor.unprocess(date, this);
    }

    @Override
    public abstract String toString();
}
