package com.paradoxplaza.eu4.replayer.localization;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * Empty {@link ResourceBundle}. Always returns key back.
 */
class EmptyResourceBundle extends ResourceBundle {

    @Override
    protected Object handleGetObject(final String key) {
        return key;
    }

    @Override
    public Enumeration<String> getKeys() {
        return new EmptyEnumeration<>();
    }

    /**
     * Empty {@link Enumeration}.
     * @param <T> iterating over T
     */
    static private class EmptyEnumeration<T> implements Enumeration<T> {

        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public T nextElement() {
            throw new NoSuchElementException();
        }
    }
}
