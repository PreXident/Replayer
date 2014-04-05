package com.paradoxplaza.eu4.replayer.utils;

import java.io.File;
import java.util.Comparator;

/**
 * Compares files by name ignoring case.
 */
public class IgnoreCaseFileNameComparator implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
        return f1.getName().compareToIgnoreCase(f2.getName());
    }
}
