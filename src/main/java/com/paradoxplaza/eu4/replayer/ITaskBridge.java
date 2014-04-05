package com.paradoxplaza.eu4.replayer;

/**
 * Interface for listening to running tasks and informing about cancellation.
 * @param <T> generic parameter
 */
public interface ITaskBridge<T> extends ITaskListener<T> {

    /**
     * Returns whether the task has been cancelled.
     * @return true if the task has been cancelled, false otherwise
     */
    boolean isCancelled();
}
