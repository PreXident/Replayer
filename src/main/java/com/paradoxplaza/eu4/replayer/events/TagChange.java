package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Represents country tag change.
 */
@AlwaysNotable
public class TagChange extends CountryEvent {

    /** Source tag. */
    public final String fromTag;

    /**
     * Only construtor.
     * @param toTag target tag
     * @param fromTag what tag changes
     */
    public TagChange(final Ref<String> toTag, final String fromTag) {
        super(toTag);
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
        return String.format(l10n("event.tagchange"), tag.val, fromTag);
    }
}
