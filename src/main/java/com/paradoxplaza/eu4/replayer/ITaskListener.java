package com.paradoxplaza.eu4.replayer;

/**
 * Interface for listening to running tasks.
 * @param <T> generic parameter
 */
public interface ITaskListener<T> {
    void updateMessage(String message);
    void updateProgress(double workDone, double max);
    void updateProgress(long workDone, long max);
    void updateTitle(String title);
    void updateValue(T value);
}
