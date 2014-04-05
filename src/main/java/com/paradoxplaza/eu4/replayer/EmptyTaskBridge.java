package com.paradoxplaza.eu4.replayer;

/**
 * ITaskBridge that does nothing.
 */
public class EmptyTaskBridge<T> implements ITaskBridge<T> {

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void run() {
        //nothing
    }

    @Override
    public void updateMessage(String message) {
        //nothing
    }

    @Override
    public void updateProgress(double workDone, double max) {
        //nothing
    }

    @Override
    public void updateProgress(long workDone, long max) {
        //nothing
    }

    @Override
    public void updateTitle(String title) {
        //nothing
    }

    @Override
    public void updateValue(T value) {
        //nothing
    }
}
