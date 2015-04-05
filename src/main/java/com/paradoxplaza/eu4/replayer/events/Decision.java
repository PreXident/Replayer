package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.Ref;

/**
 * Represents country decision.
 */
@AlwaysNotable
public class Decision extends CountryEvent {

    /** Name. */
    public final String name;

    /**
     * Only construtor.
     * @param tag country tag
     * @param name decision name
     */
    public Decision(final Ref<String> tag, final String name) {
        super(tag);
        this.name = name;
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
        return String.format(l10n("event.decision"), tag.val, name);
    }
}
