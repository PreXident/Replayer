package com.paradoxplaza.eu4.replayer;

import java.util.Collection;
import java.util.Iterator;

/**
 * Simple extension to add ProgressIterable to Tasks.
 */
abstract class Task<T> extends javafx.concurrent.Task<T> {
    class ProgressIterable<T> implements Iterable<T> {

        Collection<T> list;

        public ProgressIterable(final Collection<T> list) {
            this.list = list;
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
                    updateProgress(++count, list.size());
                    return it.next();
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }
    }
}
