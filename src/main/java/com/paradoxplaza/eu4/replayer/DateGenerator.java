package com.paradoxplaza.eu4.replayer;

import java.util.Iterator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Generates Dates.
 */
public class DateGenerator implements Iterable<Date>, Iterator<Date> {

    final Date min;
    final Date max;

    final ObjectProperty<Date> date;

    public DateGenerator(final Date min, final Date max) {
        this.min = min;
        date = new SimpleObjectProperty<>(min);
        this.max = max;
    }

    public ReadOnlyObjectProperty<Date> dateProperty() {
        return date;
    }

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

    public Date prev() {
        date.set(date.get().prev());
        return date.get();
    }

    @Override
    public void remove() {
        //nothing
    }
}
