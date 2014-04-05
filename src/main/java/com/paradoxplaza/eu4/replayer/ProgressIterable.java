package com.paradoxplaza.eu4.replayer;

import java.util.Collection;
import java.util.Iterator;

/**
 * Decorates a collection, iterator updates Task's progress.
 */
class ProgressIterable<T> implements Iterable<T> {

    /** Decorated collection. */
    final Collection<T> list;

    /** Listener to inform about progress. */
    final ITaskListener<?> listener;

    /**
     * Only constructor.
     * @param list collection to decorate
     */
    public ProgressIterable(final Collection<T> list, final ITaskListener<?> listener) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            final Iterator<T> it = list.iterator();
            int count = 0;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                listener.updateProgress(++count, list.size());
                return it.next();
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }
}
