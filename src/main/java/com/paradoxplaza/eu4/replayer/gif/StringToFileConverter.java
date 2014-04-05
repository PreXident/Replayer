package com.paradoxplaza.eu4.replayer.gif;

import com.beust.jcommander.IStringConverter;
import java.io.File;

/**
 * Simple converter to parse options containing file names.
 */
public class StringToFileConverter implements IStringConverter<File> {
    @Override
    public File convert(String value) {
        return new File(value);
    }
}
