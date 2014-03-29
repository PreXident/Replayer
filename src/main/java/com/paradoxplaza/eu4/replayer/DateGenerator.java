package com.paradoxplaza.eu4.replayer;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import java.util.Iterator;

/**
 * Generates Dates.
 */
public class DateGenerator implements Iterable<Date>, Iterator<Date> {

    /**
     * Default listener that does nothing.
     */
    static protected IDateListener defaultListener = new IDateListener() {
        @Override
        public void update(Date date, double progress) { }
    };

    /** Minimal date of the generator. */
    protected final Date min;

    /** Maximal date of the generator. */
    protected final Date max;

    /** Current date of the generator. */
    protected Date date;

    /**
     * Number of days between min and max.
     * Double only to enforce double division.
     */
    final double distance;

    /** Current date number. */
    protected int day = 0;

    /** Current progress of the generating. */
    protected double progress = 0;

    /** Change listener to state of generator. */
    IDateListener listener = defaultListener;

    /**
     * Only contructor.
     * @param min minimal date
     * @param max maximal date
     */
    public DateGenerator(final Date min, final Date max) {
        this.min = min;
        date = min;
        this.max = max;
        distance = Date.calculateDistance(min, max);
    }

    /**
     * Returns current date.
     * @return current date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns current {@link #listener}.
     * @return current listener
     */
    public IDateListener getListener() {
        return listener;
    }

    /**
     * Sets the {@link #listener}. Do not pass null!
     * @param listener
     */
    public void setListener(final IDateListener listener) {
        assert listener != null : "Listener cannot be null";
        this.listener = listener;
    }

    /**
     * Resets {@link #listener} to default listener that does nothing.
     */
    public void resetListener() {
        listener = defaultListener;
    }

    /**
     * Returns {@link #max}.
     * @return the max
     */
    public Date getMax() {
        return max;
    }

    /**
     * Returns {@link #min}.
     * @return the min
     */
    public Date getMin() {
        return min;
    }

    /**
     * Returns progress.
     * @return progress
     */
    public double getProgress() {
        return progress;
    }

    /**
     * Returns whether {@link #prev()} can be called.
     * @return true if prev() can be called, false otherwise
     */
    public boolean hasPrev() {
        return getMin().compareTo(date) < 0;
    }

    @Override
    public boolean hasNext() {
        return date.compareTo(getMax()) < 0;
    }

    @Override
    public Iterator<Date> iterator() {
        return this;
    }

    @Override
    public Date next() {
        assert hasNext(): l10n("generator.next.error");
        date = date.next();
        progress = ++day/distance;
        listener.update(date, progress);
        return date;
    }

    /**
     * Moves date one day back and returns this new date.
     * @return new date one day back
     */
    public Date prev() {
        assert hasPrev() : l10n("generator.prev.error");
        date = date.prev();
        progress = --day/distance;
        listener.update(date, progress);
        return date;
    }

    @Override
    public void remove() {
        //nothing
    }

    /**
     * Listener to date changes.
     */
    public static interface IDateListener {

        /**
         * Gets called when date changes.
         * @param date current date
         * @param progress current progress
         */
        void update(Date date, double progress);
    }
}
