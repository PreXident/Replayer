package com.paradoxplaza.eu4.replayer.utils;

/**
 * Shameless copy of {@link javafx.beans.value.WritableValue}.
 * @param <T> type of value
 */
public interface WritableValue<T extends Object> {

    /**
     * Sets the value.
     * @param t value to be set
     */
    public void setValue(T t);
}