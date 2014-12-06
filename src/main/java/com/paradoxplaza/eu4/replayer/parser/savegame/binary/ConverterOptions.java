package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Options for standalone of {@link Converter}.
 */
public class ConverterOptions {

    /** Flag indicating whether help needs printing. */
    @Parameter(
            description = "prints help and exits",
            help = true,
            names = { "-h", "/H", "--help", "/?" })
    boolean help = false;

    /** Print the converted saves to standard output. */
    @Parameter(
            description = "should converted saves be printed to standard output?",
            names = { "-t", "/T", "--test-output" })
    boolean test = false;

    /** Print the converted saves formatted. */
    @Parameter(
            description = "should converted saves be formatted?",
            names = { "-p", "/P", "--pretty-print" })
    boolean prettyPrint = true;

    /** List of saves to convert. */
    @Parameter(description = "[list of saves to convert]")
    List<String> files = new ArrayList<>();
}
