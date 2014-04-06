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
            names = { "-s", "/S", "--settings" })
    String properties = "replayer.properties";

    /** Flag indicating whether political mapmode should be giffed. */
    @Parameter(
            description = "should political mapmode be giffed? (default one, if none specified)",
            names = { "-p", "/P", "--political" })
    boolean political = false;

    /** Flag indicating whether religious mapmode should be giffed. */
    @Parameter(
            description = "should cultural mapmode be giffed?",
            names = { "-r", "/R", "--religious" })
    boolean religious = false;

    /** Flag indicating whether religious mapmode should be giffed. */
    @Parameter(
            description = "should cultural mapmode be giffed?",
            names = { "-c", "/C", "--cultural" })
    boolean cultural = false;

    /** Flag indicating whether separate technological mapmode should be giffed. */
    @Parameter(
            description = "should separate technological mapmode be giffed?",
            names = { "-t1", "/T1", "--technological-separate" })
    boolean technologicalSeparate = false;

    /** Flag indicating whether combined technological mapmode should be giffed. */
    @Parameter(
            description = "should combined technological mapmode be giffed?",
            names = { "-t2", "/T2", "--technological-combined" })
    boolean technologicalCombined = false;

    /** Directory containing the saves to be loaded. */
    @Parameter(
            description = "directory with saves to load, ignored when list of saves is provided",
            names = { "-d", "/D", "--directory" })
    String directory = null;

    /** List of saves to load. */
    @Parameter(
            description = "[list of saves to load]",
            converter = StringToFileConverter.class)
    List<File> files = new ArrayList<>();
}
