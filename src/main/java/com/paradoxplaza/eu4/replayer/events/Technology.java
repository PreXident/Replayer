package com.paradoxplaza.eu4.replayer.events;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;

/**
 * Country got new technology levels.
 */
public class Technology extends Event {

    /** Subject country tag. */
    public final String tag;

    /** New adm tech level. */
    public final int adm;

    /** New dip tech level. */
    public final int dip;

    /** New mil tech level. */
    public final int mil;

    /** Old adm tech level. */
    public int old_adm;

    /** Old dip tech level. */
    public int old_dip;

    /** Old mil tech level. */
    public int old_mil;

    public Technology(final String tag, final int adm, final int dip, final int mil) {
        this.tag = tag;
        this.adm = adm;
        this.dip = dip;
        this.mil = mil;
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
        return String.format(l10n("event.technology"), tag, adm, dip, mil);
    }
}
