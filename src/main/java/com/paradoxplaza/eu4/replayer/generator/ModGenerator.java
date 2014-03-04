package com.paradoxplaza.eu4.replayer.generator;

import com.paradoxplaza.eu4.replayer.ProvinceInfo;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Generates mod for supporting Random New World.
 */
public class ModGenerator {

    /**
     * Creates a string "red green blue" out of int color.
     * @param color input color
     * @param separator delimiter to use
     * @return string "REDseparatorGREENseparator"
     */
    private static String colorToString(final int color, final String separator) {
        //255 << 24 | red << 16 | green << 8 | blue;
        final int red = (color & 0x00FF0000) >> 16;
        final int green = (color & 0x0000FF00) >> 8;
        final int blue = color & 0x000000FF;
        return red + separator + green + separator + blue;
    }

    /**
     * Generates mod for supporting Random New World.
     * @param provinces list of provinces
     */
    public void generate(Iterable<ProvinceInfo> provinces) {
        try (BufferedWriter tagWriter = new BufferedWriter(new FileWriter("common/country_tags/RNW.txt"))) {
            final TagGenerator tagGenerator = new TagGenerator();
            for(ProvinceInfo province : provinces) {
                if (province.isSea || province.isWasteland) {
                    continue;
                }
                final String tag = tagGenerator.next();
                final String color = colorToString(province.color, " ");
                //try (BufferedWriter writer = new BufferedWriter(new FileWriter("history/provinces/" + parts[0] + " - " + parts[4] + ".txt"))) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("history/provinces/" + province.id + " - dummy.txt"))) {
                    writer.append("owner = " + tag);
                    writer.newLine();
                    writer.append("controller = " + tag);
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("common/countries/" + tag + ".txt"))) {
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
//                try (BufferedWriter writer = new BufferedWriter(new FileWriter("common/country_colors/" + tag + ".txt"))) {
//                    writer.append(tag + " = {");
//                    writer.newLine();
//                    writer.append("\t color1= { " + parts[1] + " " + parts[2] + " " + parts[3] + " }");
//                    writer.newLine();
//                    writer.append("\t color2= { " + parts[1] + " " + parts[2] + " " + parts[3] + " }");
//                    writer.newLine();
//                    writer.append("\t color3= { " + parts[1] + " " + parts[2] + " " + parts[3] + " }");
//                    writer.append("}");
//                }
//                try (BufferedWriter writer = new BufferedWriter(new FileWriter("common/country_tags/" + tag + ".txt"))) {
//                    writer.append(tag + " = \"countries/" + tag + ".txt\"");
//                }
                tagWriter.append(tag + " = \"countries/" + tag + ".txt\"");
                tagWriter.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
