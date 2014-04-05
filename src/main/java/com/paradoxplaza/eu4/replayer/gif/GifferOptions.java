package com.paradoxplaza.eu4.replayer.gif;

import com.beust.jcommander.Parameter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Options for command line mode of {@link Giffer}.
 */
public class GifferOptions {

    /** Flag indicating whether help needs printing. */
    @Parameter(
            description = "prints help and exits",
            help = true,
            names = { "-h", "/H", "--help", "/?" })
    boolean help = false;

    /** Path to replayer.properties. */
    @Parameter(
            arity = 1,
            description = "path to replayer.properties",
            names = { "-p", "/P", "--properties" })
    String properties = "replayer.properties";

    /** Which buffer should be giffed. */
    @Parameter(
            arity = 1,
            description = "which mapmode should be giffed?",
            names = { "-m", "/M", "--mapmode" })
    Buffer buffer = Buffer.POLITICAL;

    /** List of saves to load. */
    @Parameter(
            description = "[list of saves to load]",
            converter = StringToFileConverter.class)
    List<File> files = new ArrayList<>();

    /** Which buffer should be giffed. */
    public enum Buffer {
        POLITICAL,
        RELIGIOUS,
        CULTURAL,
        TECHNOLOGICAL_COMBINED,
        TECHNOLOGICAL_SEPARATE
    };
}
