package com.paradoxplaza.eu4.replayer.events;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;

/**
 * Represents country tag change.
 */
@AlwaysNotable
public class TagChange extends Event {

    /** Target tag. */
    public final String toTag;

    /** Source tag. */
    public final String fromTag;

    /**
     * Only construtor.
     * @param toTag target tag
     * @param fromTag what tag changes
     */
    public TagChange(final String toTag, final String fromTag) {
        this.toTag = toTag;
        this.fromTag = fromTag;
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
    public String toString() {
        return String.format(l10n("event.tagchange"), toTag, fromTag);
    }
}
