package com.paradoxplaza.eu4.replayer.gui;

import com.sun.javafx.scene.control.skin.ColorPickerSkin;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;

/**
 *
 */
public class MyColorPickerSkin extends ColorPickerSkin {

    public MyColorPickerSkin(ColorPicker cp) {
        super(cp);
    }

    public MyColorPickerSkin(MyColorPicker cp) {
        super(cp);
    }

    @Override
    protected Node getPopupContent() {
        final Node result = super.getPopupContent();
        if (result instanceof Pane) {
            final Pane p = (Pane) result;
            final List<Node> children = new ArrayList<>(p.getChildren());
            for (Node node : children) {
                if (node instanceof Separator || node instanceof Hyperlink) {
                    p.getChildren().remove(node);
                }
            }
        }
        return result;
    }
}
