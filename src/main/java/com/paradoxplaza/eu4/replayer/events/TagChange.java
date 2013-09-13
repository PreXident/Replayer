package com.paradoxplaza.eu4.replayer.events;

/**
 * Represents country tag change.
 */
public class TagChange extends Event {

    /** Target tag. */
    public final String toTag;

    /** Source tag. */
    public final String fromTag;

    /**
     * Only construtor.
     * @param toTag target tag
     * @param fromTag what tag changes
     */
    public TagChange(final String toTag, final String fromTag) {
        this.toTag = toTag;
        this.fromTag = fromTag;
    }

    @Override
    public String toString() {
        return String.format("Country %2$s changed tag to %1$s enabled", toTag, fromTag);
    }
}
