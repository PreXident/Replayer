package com.paradoxplaza.eu4.replayer.gui;

import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.DateGenerator;
import com.paradoxplaza.eu4.replayer.DateGenerator.IDateListener;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import com.paradoxplaza.eu4.replayer.EventProcessor.IEventListener;
import com.paradoxplaza.eu4.replayer.localization.Localizator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.PixelFormat;
import javafx.scene.web.WebEngine;

/**
 * Class for jumping in the time period.
 */
class Jumper extends Task<Void> {

    /** Previous event listener to be restored after finish. */
    protected EventProcessor.IEventListener prevEventListener;

    /** Previous event listener to be restored after finish. */
    protected DateGenerator.IDateListener prevDateListener;

    /** Target date to skip to. */
    protected final Date target;

    /** Application controller. */
    private final ReplayerController controller;

    /**
     * Only constructor.
     * @param target target date to skip to
     * @param eventListener event listener to use
     */
    public Jumper(final ReplayerController controller,
            final Date target, final IEventListener eventListener) {
        this.controller = controller;
        this.target = target;
        prevEventListener = controller.replay.getEventListener();
        controller.replay.setEventListener(eventListener);
        prevDateListener = controller.replay.getDateListener();
        controller.replay.setDateListener(new CancellingDateListener());
        this.stateProperty().addListener(new EndStateListener());
    }

    @Override
    protected final Void call() throws Exception {
        updateTitle(String.format(Localizator.l10n("replay.jumping"), target));
        try {
            controller.replay.skipTo(target);
            updateTitle(String.format(Localizator.l10n("replay.jumped"), target));
        } catch (CancelException e) {
            updateTitle(Localizator.l10n("replay.cancel"));
        }
        return null;
    }

    /**
     * Simple indicator whether the jump was cancelled.
     */
    class CancelException extends RuntimeException {
    }

    /**
     * Checks whether the jump was cancelled during jumping.
     */
    class CancellingDateListener implements IDateListener {

        @Override
        public void update(Date date, double progress) {
            controller.updateGif(date);
            if (!isCancelled()) {
                updateProgress(progress, 1D);
            } else {
                throw new CancelException();
            }
        }
    }

    /**
     * Gets called when the state is change.
     * Intented to clean up after the jump.
     */
    class EndStateListener implements ChangeListener<State> {

        @Override
        public void changed(ObservableValue<? extends State> ov, State oldVal, State newVal) {
            if (newVal == State.SUCCEEDED || newVal == State.CANCELLED) {
                controller.replay.setEventListener(prevEventListener);
                controller.replay.setDateListener(prevDateListener);
                controller.output.getPixelWriter().setPixels(0, 0, controller.replay.bufferWidth, controller.replay.bufferHeight, PixelFormat.getIntArgbPreInstance(), controller.buffer, 0, controller.replay.bufferWidth);
                final WebEngine e = controller.log.getEngine();
                e.getLoadWorker().stateProperty().addListener(new LogFinishListener(e));
                e.loadContent(String.format(ReplayerController.LOG_INIT_FORMAT, controller.logContent.toString()));
            } else if (newVal == State.FAILED) {
                getException().printStackTrace();
            }
        }
    }

    /**
     * Listens to log worker to finish loading.
     */
    class LogFinishListener implements ChangeListener<State> {

        /** Log container. */
        final WebEngine webEngine;

        /**
         * Only constructor.
         * @param webEngine log container
         */
        public LogFinishListener(final WebEngine webEngine) {
            this.webEngine = webEngine;
        }

        @Override
        public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
            if (newState == State.SUCCEEDED) {
                controller.log.getEngine().executeScript(ReplayerController.SCROLL_DOWN);
                controller.logContent.setLength(ReplayerController.LOG_HEADER.length());
                webEngine.getLoadWorker().stateProperty().removeListener(this); //must remove itself
                controller.statusLabel.textProperty().unbind();
                controller.dateEdit.textProperty().set(controller.replay.getDate().toString());
                controller.jumpButton.setVisible(false);
                controller.finalizer = null;
                controller.lock.release();
            }
        }
    }

}
