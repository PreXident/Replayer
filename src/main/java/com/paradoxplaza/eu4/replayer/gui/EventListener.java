package com.paradoxplaza.eu4.replayer.gui;

import com.paradoxplaza.eu4.replayer.EventProcessor;

/**
 * Ancestor of other {@link EventProcessor event listeners}.
 * It handles everything, others only lack functionality.
 */
public class EventListener implements EventProcessor.IEventListener {

    /** Replayer controller with GUI. */
    final protected ReplayerController controller;

    /**
     * Only constructor.
     * @param controller owner of the listener
     */
    public EventListener(final ReplayerController controller) {
        this.controller = controller;
    }

    @Override
    public void appendLog(String text) {
        controller.logContent.append(text);
    }

    @Override
    public void updateLog() {
        controller.updateLog();
    }

    @Override
    public void setColor(final int[] buffer, final int pos, final int color) {
        if (controller.buffer == buffer) {
            controller.output.getPixelWriter().setArgb(
                    pos % controller.replay.bufferWidth,
                    pos / controller.replay.bufferWidth,
                    color);
        }
    }
}
