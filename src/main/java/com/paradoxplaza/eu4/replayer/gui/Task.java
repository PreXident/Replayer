package com.paradoxplaza.eu4.replayer.gui;

import com.paradoxplaza.eu4.replayer.ITaskBridge;

/**
 * Simple extension to publish update* methods.
 */
abstract class Task<T> extends javafx.concurrent.Task<T> implements ITaskBridge<T> {

    @Override
    public void updateMessage(String message) {
        super.updateMessage(message);
    }

    @Override
    public void updateProgress(double workDone, double max) {
        super.updateProgress(workDone, max);
    }

    @Override
    public void updateProgress(long workDone, long max) {
        super.updateProgress(workDone, max);
    }

    @Override
    public void updateTitle(String title) {
        super.updateTitle(title);
    }

    @Override
    public void updateValue(T value) {
        //since 1.8
        //super.updateValue(value);
    }
}
