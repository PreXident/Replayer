package com.paradoxplaza.eu4.replayer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple wrapper for information about provinces.
 */
public class ProvinceInfo {

    /** Province id. */
    public final String id;

    /** Province id. */
    public final String name;

    /** Pixel color of this province. */
    public final int color;

    /** List of pixels of this province. */
    public final List<Integer> points = new ArrayList<>();

    /** Center of the province. */
    public final Point center = new Point(-1, -1);

    /** Controller of this province. */
    public String controller = null;

    /** Owner of this province. */
    public String owner = null;

    /**
     * Only constructor.
     * @param color color of pixels associated to this province
     */
    public ProvinceInfo(final String id, final String name, final int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    /**
     * Computes the center of the province as a mean of pixel coordinates.
     */
    public void calculateCenter(final int mapWidth) {
        if (points.isEmpty()) {
            return;
        }
        int x = 0;
        int y = 0;
        for (Integer p : points) {
            x += p % mapWidth;
            y += p / mapWidth;
        }
        center.move(x / points.size(), y / points.size());
    }

    @Override
    public String toString() {
        return String.format("id=%1$s\nname=%2$s\ncontroller=%3$s\nowner=%4$s", id, name, controller, owner);
    }
}
