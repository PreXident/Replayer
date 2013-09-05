package com.paradoxplaza.eu4.replayer;

import java.util.Iterator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Generates Dates.
 */
public class DateGenerator implements Iterable<Date>, Iterator<Date> {

    /** Minimal date of the generator. */
    final Date min;

    /** Maximal date of the generator. */
    final Date max;

    /** Current date of the generator. */
    final ObjectProperty<Date> date;

    /**
     * Only contructor.
     * @param min minimal date
     * @param max maximal date
     */
    public DateGenerator(final Date min, final Date max) {
        this.min = min;
        date = new SimpleObjectProperty<>(min);
        this.max = max;
    }

    /**
     * Returns current date.
     * @return current date
     */
    public ReadOnlyObjectProperty<Date> dateProperty() {
        return date;
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
        date.set(date.get().next());
        return date.get();
    }

    /**
     * Moves date one day back and returns this new date.
     * @return new date one day back
     */
    public Date prev() {
        date.set(date.get().prev());
        return date.get();
    }

    @Override
    public void remove() {
        //nothing
    }
}
