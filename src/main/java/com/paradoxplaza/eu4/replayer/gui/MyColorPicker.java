package com.paradoxplaza.eu4.replayer.gui;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;

/**
 *
 */
public class MyColorPicker extends ColorPicker {

    public MyColorPicker() {
        super();
        this.setStyle("-fx-skin: \"com.paradoxplaza.eu4.replayer.gui.MyColorPickerSkin\"");
    }

    public MyColorPicker(final Color color) {
        super(color);
    }

//    @Override
//    protected Skin<?> createDefaultSkin() {
//        return new MyColorPickerSkin(this);
//    }
}
