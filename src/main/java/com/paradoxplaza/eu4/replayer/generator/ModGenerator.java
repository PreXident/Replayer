package com.paradoxplaza.eu4.replayer.generator;

import com.paradoxplaza.eu4.replayer.utils.ColorUtils;
import static com.paradoxplaza.eu4.replayer.utils.ColorUtils.SEA_COLOR;
import static com.paradoxplaza.eu4.replayer.utils.ColorUtils.WASTELAND_COLOR;
import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import com.paradoxplaza.eu4.replayer.gui.ReplayerController;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Generates mod for supporting Random New World.
 */
public class ModGenerator {

    /** Application's settings. */
    final Properties settings;

    /**
     * Only constructor.
     * @param settings application settings
     */
    public ModGenerator(final Properties settings) {
        this.settings = settings;
    }

    /**
     * Generates mod for supporting Random New World.
     * @param provinces list of provinces
     */
    public void generate(Iterable<ProvinceInfo> provinces) {
        unzipStaticModPart();
        final String baseDir = settings.getProperty("mod.basedir", ReplayerController.DEFAULT_BASE_DIR) + "/mod/RNW";
        try (BufferedWriter tagWriter = new BufferedWriter(new FileWriter(baseDir + "/common/country_tags/RNW.txt"))) {
            final TagGenerator tagGenerator = new TagGenerator();
            for(ProvinceInfo province : provinces) {
                if (province.isSea || province.isWasteland) {
                    continue;
                }
                if (province.color == SEA_COLOR  || province.color == WASTELAND_COLOR) {
                    System.err.printf(l10n("generator.conflict"), province.id);
                }
                final String tag = tagGenerator.next();
                final String color = ColorUtils.colorToStringFloat(province.color, " ");
                //generating itself
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(baseDir + "/history/provinces/" + province.id + " - dummy.txt"))) {
                    writer.append("owner = " + tag);
                    writer.newLine();
                    writer.append("controller = " + tag);
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(baseDir + "/common/countries/" + tag + ".txt"))) {
                    writer.append("graphical_culture = westerngfx");
                    writer.newLine();
                    writer.append("color = { " + color + " }");
                    writer.newLine();
                    writer.append("monarch_names = { \"dummy #0\" = 10}");
                    writer.newLine();
                    writer.append("leader_names = { dummy }");
                    writer.newLine();
                    writer.append("ship_names = { dummy }");
                }
                tagWriter.append(tag + " = \"countries/" + tag + ".txt\"");
                tagWriter.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unzipStaticModPart() {
        final String baseDir = settings.getProperty("mod.basedir", ReplayerController.DEFAULT_BASE_DIR) + "/mod";
        try (ZipInputStream zip = new ZipInputStream(getClass().getResourceAsStream("mod.zip"))) {
            ZipEntry z = zip.getNextEntry();
            final byte[] buf = new byte[2048];
            while (z != null) {
                if (z.isDirectory()) {
                    z = zip.getNextEntry();
                    continue;
                }
                final String name = z.getName();
                final File file = new File(baseDir + "/" + name);
                file.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    int len;
                    while ((len = zip.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                z = zip.getNextEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
