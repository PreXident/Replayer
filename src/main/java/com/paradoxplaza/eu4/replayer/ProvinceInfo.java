package com.paradoxplaza.eu4.replayer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;

/**
 * Simple wrapper for information about provinces.
 */
public class ProvinceInfo {

    public final Color color;

    public final List<Point> points = new ArrayList<>();

    public final Point center = new Point(-1, -1);

    public ProvinceInfo(final Color color) {
        this.color = color;
    }

    public void calculateCenter() {
        if (points.size() == 0) {
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
