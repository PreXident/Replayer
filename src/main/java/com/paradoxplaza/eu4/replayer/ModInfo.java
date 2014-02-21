package com.paradoxplaza.eu4.replayer;

import java.util.Set;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * Simple container with information about mod.
 */
public class ModInfo {
    /** Mod name. */
    final String name;

    /** Path to mod directory. */
    final String dir;

    /** Zip archive containing the mod. */
    final String archive;

    /** Set of dirs that should not be loaded from the game's directory. */
    final Set<String> replacePath;

    ZipFile zip = null;

    /**
     * Only constructor.
     * @param name mod name
     * @param dir path to mod directory
     * @param archive  mod zip archive
     * @param replacePath replaced directories
     */
    public ModInfo(final String name, final String dir, final String archive,
            final Set<String> replacePath) {
        this.name = name;
        this.dir = dir;
        this.archive = archive;
        this.replacePath = replacePath;
    }
}
