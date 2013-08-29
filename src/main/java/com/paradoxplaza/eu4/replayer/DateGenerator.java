package com.paradoxplaza.eu4.replayer;

import java.util.Iterator;

/**
 * Generates Dates.
 */
public class DateGenerator implements Iterable<Date>, Iterator<Date> {

    final Date min;
    final Date max;

    Date date;

    public DateGenerator(final Date min, final Date max) {
        this.min = min;
        date = min;
        this.max = max;
    }

    public boolean hasPrev() {
        return min.compareTo(date) < 0;
    }

    @Override
    public boolean hasNext() {
        return date.compareTo(max) <= 0;
    }

    @Override
    public Iterator<Date> iterator() {
        return this;
    }

    @Override
    public Date next() {
        date = date.next();
        return date;
    }

    public Date prev() {
        date = date.prev();
        return date;
    }

    @Override
    public void remove() {
        //nothing
    }
}
