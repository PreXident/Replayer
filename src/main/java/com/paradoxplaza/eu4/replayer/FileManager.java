package com.paradoxplaza.eu4.replayer;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.mod.ModParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * Class for files and directories with respect to mods.
 */
class FileManager {

    /** Application's controller. */
    final ReplayerController controller;

    /** List of active mods. */
    final List<ModInfo> mods = new ArrayList<>();

    /** Base mod directory. */
    String modDirPath;

    /**
     * Only constructor.
     * @param controller application's controller
     */
    public FileManager(final ReplayerController controller) {
        this.controller = controller;
    }

    public InputStream getInputStream(final String path) throws IOException {
        if (controller.rnw && path.equals("map/provinces.bmp")) {
            return new FileInputStream(controller.settings.getProperty("rnw.map"));
        }
        for (ModInfo mod : mods) {
            if (mod.dir != null) {
                final String filePath = modDirPath + "/" + mod.dir + "/" + path;
                final File file = new File(filePath);
                if (file.exists()) {
                    return new FileInputStream(file);
                }
            } else if (mod.archive != null) {
                final ZipArchiveEntry entry = mod.zip.getEntry(path);
                if (entry != null) {
                    final int size = (int) entry.getSize();
                    byte[] buffer = new byte[(int) entry.getSize()];
                    int read = 0;
                    try (InputStream is = mod.zip.getInputStream(entry)) {
                        while (read != size) {
                            read += is.read(buffer, read, size - read);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new ByteArrayInputStream(buffer);
                }
            } else {
                System.err.printf(l10n("mod.error"), mod.name);
            }
        }
        final String filePath = controller.eu4Directory + "/" + path;
        final File file = new File(filePath);
        return new FileInputStream(file);
    }

    /**
     * Adds input streams of files in directory. Ignores files listed in set.
     * @param directory folder to be listed
     * @param streams list of input streams to add to
     * @param files files to ignore
     */
    private void listDirectory(final File directory, final List<InputStream> streams, final Set<String> files) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        for (File file : directory.listFiles()) {
            final String fileName = file.getName();
            if (files.contains(fileName)) {
                continue;
            }
            try {
                streams.add(new FileInputStream(file));
                files.add(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns list of InputStreams from the directory.
     * @param directory folder to be listed
     * @return list of InputStreams from the directory
     */
    public List<InputStream> listFiles(final String directory) {
        boolean replaced = false;
        final Set<String> files = new HashSet<>();
        final List<InputStream> streams = new ArrayList<>();
        for (ModInfo mod : mods) {
            if (mod.replacePath.contains(directory)) {
                replaced = true;
            }
            if (mod.dir != null) {
                final String modPath = modDirPath + "/" + mod.dir + "/" + directory;
                final File dir = new File(modPath);
                listDirectory(dir, streams, files);
            } else if (mod.archive != null) {
                listZip(mod.zip, directory, streams, files);
            } else {
                System.err.printf(l10n("mod.error"), mod.name);
            }
        }
        if (!replaced) {
            final File dir = new File(controller.eu4Directory + "/" + directory);
            listDirectory(dir, streams, files);
        }
        return streams;
    }

    /**
     * Returns list of InputStreams from the directory in zip file.
     * @param zip zip file
     * @param directory folder to be listed
     * @param streams list of input streams to add to
     * @param files files to ignore
     */
    private void listZip(final ZipFile zip, final String directory, final List<InputStream> streams, final Set<String> files) {
        final Enumeration<ZipArchiveEntry> iter = zip.getEntries();
        while (iter.hasMoreElements()) {
            final ZipArchiveEntry entry = iter.nextElement();
            final String filePath = entry.getName();
            final String fileDir = filePath.replaceFirst("/[^/]*$", "");
            final String fileName = filePath.substring(filePath.lastIndexOf('/')+1);
            if (!fileDir.equals(directory) || files.contains(fileName)) {
                continue;
            }
            final int size = (int) entry.getSize();
            byte[] buffer = new byte[(int) entry.getSize()];
            int read = 0;
            try (InputStream is = zip.getInputStream(entry)) {
                while (read != size) {
                    read += is.read(buffer, read, size - read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            streams.add(new ByteArrayInputStream(buffer));
            files.add(fileName);
        }
    }

    /**
     * Loads mods info from mod directory.
     */
    public void loadMods() {
        for (ModInfo mod : mods) {
            if (mod.zip != null) {
                try {
                    mod.zip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        mods.clear();
        modDirPath = controller.settings.getProperty("mod.basedir", ReplayerController.DEFAULT_BASE_DIR);
        final String[] modDescriptors = controller.settings.getProperty("mod.list", "").split(";");
        for (String desc : modDescriptors) {
            if ("".equals(desc)) {
                continue;
            }
            final String modPath = modDirPath + "/" + desc;
            try (final InputStream is = new FileInputStream(modPath)) {
                final ModParser parser = new ModParser(mods, Long.MAX_VALUE, is);
                parser.run();
            } catch(Exception e) { e.printStackTrace(); }
        }
        for (ModInfo mod : mods) {
            if (mod.archive != null) {
                try {
                    mod.zip = new ZipFile(modDirPath + "/" + mod.archive);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
