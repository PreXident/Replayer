package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

import com.beust.jcommander.JCommander;
import static com.paradoxplaza.eu4.replayer.parser.savegame.Utils.chooseStream;
import com.paradoxplaza.eu4.replayer.utils.UnclosableOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Converts binary save game to plain text.
 */
public class Converter {

    /** Standalone run will read blocks of this size. */
    static final int READ_BLOCK_SIZE = 2048;

    /**
     * Converts binary EU4 saves to plain text.
     * @param args see {@link ConverterOptions}
     */
    static public void main(String[] args) {
        //args = new String[] { "-d" };
        //parse command line options
        final ConverterOptions options = new ConverterOptions();
        final JCommander jCommander = new JCommander(options, args);
        jCommander.setProgramName("java -cp replayer.jar com.paradoxplaza.eu4.replayer.savegame.binary.Converter"); //set name in usage
        if (options.help) {
            jCommander.usage();
            return;
        }
        //show dialog if no file is specified
        if (options.files.isEmpty()) {
            //let user select the save file to convert
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select binary save to convert");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("EU4 bin saves (*.eu4)", "eu4"));
            if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION
                    || chooser.getSelectedFile() == null) {
                System.out.println("Bye!");
                return;
            }
            final File save = chooser.getSelectedFile();
            options.files.add(save.getAbsolutePath());
        }
        //convert files
        final String suffix = options.deironman ? "dei.eu4" : "txt.eu4";
        for (String path : options.files) {
            try (InputStream is = chooseStream(new FileInputStream(path), options.deironman).getFirst();
                    OutputStream os = !options.test ?
                            new FileOutputStream(path + suffix)
                            : new UnclosableOutputStream(System.out)) {
                if (is instanceof IronmanStream) {
                    ((IronmanStream)is).setPrettyPrint(options.prettyPrint);
                }
                final byte[] bytes = new byte[READ_BLOCK_SIZE];
                int length;
                while ((length = is.read(bytes)) != -1) {
                    os.write(bytes, 0, length);
                }
            } catch (Exception e) {
                System.err.println("Exception while processing file " + path);
                e.printStackTrace();
            }
        }
    }

    /**
     * Utility classes need no constructor.
     */
    private Converter() {
        //nothing
    }
}
