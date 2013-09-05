package com.paradoxplaza.eu4.replayer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;

/**
 * Simple wrapper for information about provinces.
 */
public class ProvinceInfo {

    /** Pixel color of this province. */
    public final Color color;

    /** List of pixels of this province. */
    public final List<Point> points = new ArrayList<>();

    /** Center of the province. */
    public final Point center = new Point(-1, -1);

    /**
     * Only constructor.
     * @param color color of pixels associated to this province
     */
    public ProvinceInfo(final Color color) {
        this.color = color;
    }

    /**
     * Computes the center of the province as a mean of pixel coordinates.
     */
    public void calculateCenter() {
        if (points.isEmpty()) {
            return;
        }
        int x = 0;
        int y = 0;
        for (Point p : points) {
            x += p.x;
            y += p.y;
        }
        center.move(x / points.size(), y / points.size());
    }
}
