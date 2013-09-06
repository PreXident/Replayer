package com.paradoxplaza.eu4.replayer;

import java.util.Iterator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Generates Dates.
 */
public class DateGenerator implements Iterable<Date>, Iterator<Date> {

    /**
     * Returns number of days between lower and upper date.
     * @param low lower date
     * @param up upper date
     * @return number of days between lower and upper date
     */
    static double calculateDistance(final Date low, final Date up) {
        final int years = up.year - low.year;
        int lowDays = low.day;
        for (int i = 0; i < low.month - 1; ++i) {
            lowDays += Date.monthsDays[i];
        }
        int upDays = up.day;
        for (int i = 0; i < up.month - 1; ++i) {
            upDays += Date.monthsDays[i];
        }
        return years * Date.yearDays + upDays - lowDays;
    }

    /** Minimal date of the generator. */
    final Date min;

    /** Maximal date of the generator. */
    final Date max;

    /** Current date of the generator. */
    final ObjectProperty<Date> date;

    /**
     * Number of days between min and max.
     * Double only to enforce double division.
     */
    final double distance;

    /** Current date number. */
    int day = 0;

    /** Progress in timeline. */
    final DoubleProperty progress = new SimpleDoubleProperty(0);

    /**
     * Only contructor.
     * @param min minimal date
     * @param max maximal date
     */
    public DateGenerator(final Date min, final Date max) {
        this.min = min;
        date = new SimpleObjectProperty<>(min);
        this.max = max;
        distance = calculateDistance(min, max);
    }

    /**
     * Returns current date.
     * @return current date
     */
    public ReadOnlyObjectProperty<Date> dateProperty() {
        return date;
    }

    /**
     * Returns progress.
     * @return progress
     */
    public ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }

    /**
     * Returns whether {@link prev} can be called.
     * @return true if prev() can be called, false otherwise
     */
    public boolean hasPrev() {
        return min.compareTo(date.get()) < 0;
    }

    @Override
    public boolean hasNext() {
        return date.get().compareTo(max) <= 0;
    }

    @Override
    public Iterator<Date> iterator() {
        return this;
    }

    @Override
    public Date next() {
        assert hasNext(): "No next date!";
        date.set(date.get().next());
        progress.set(++day/distance);
        return date.get();
    }

    /**
     * Moves date one day back and returns this new date.
     * @return new date one day back
     */
    public Date prev() {
        assert hasPrev() : "No previous date!";
        date.set(date.get().prev());
        progress.set(--day/distance);
        return date.get();
    }

    @Override
    public void remove() {
        //nothing
    }
}
