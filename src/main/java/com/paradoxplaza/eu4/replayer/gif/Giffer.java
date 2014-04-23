package com.paradoxplaza.eu4.replayer.gif;

import com.beust.jcommander.JCommander;
import com.paradoxplaza.eu4.replayer.Date;
import com.paradoxplaza.eu4.replayer.DateGenerator;
import com.paradoxplaza.eu4.replayer.EmptyTaskBridge;
import com.paradoxplaza.eu4.replayer.EventProcessor;
import com.paradoxplaza.eu4.replayer.Replay;
import com.paradoxplaza.eu4.replayer.SaveGame;
import com.paradoxplaza.eu4.replayer.Utils;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.utils.IgnoreCaseFileNameComparator;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import com.paradoxplaza.eu4.replayer.utils.UnclosableInputStream;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Class responsible for creating gifs.
 */
public class Giffer {

    /**
     * Loads specified saves and creates gif.
     * @param args command line arguments
     * @throws InterruptedException should not happen
     */
    static public void main(final String[] args) throws InterruptedException {
        final Properties settings = new Properties(Utils.loadDefaultJarProperties());
        Utils.resetDefaultLocale(settings.getProperty("locale.language"));
        //
        //parse command line options
        final GifferOptions options = new GifferOptions();
        final JCommander jCommander = new JCommander(options, args);
        jCommander.setProgramName("java -cp replayer.jar com.paradoxplaza.eu4.replayer.gif.Giffer"); //set name in usage
        if (options.help
                || (options.files.isEmpty() && options.directory == null )) {
            jCommander.usage();
            return;
        }
        final List<File> files = new ArrayList<>();
        if (options.files.isEmpty()) {
            final File directory = new File(options.directory);
            if (!directory.isDirectory()) {
                System.out.printf(l10n("giffer.directory.error"), options.directory);
                return;
            }
            files.addAll(Arrays.asList(directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".eu4");
                }
            })));
        } else {
            files.addAll(options.files);
        }
        Collections.sort(files, new IgnoreCaseFileNameComparator());
        //
        //load properties
        System.out.printf(l10n("app.properties.loading"), options.properties);
        try (final InputStream is =
                options.properties.equals("-") ? new UnclosableInputStream(System.in)
                    : new FileInputStream(options.properties)) {
             settings.load(is);
        } catch(Exception e) {
            System.err.printf(l10n("app.properties.error"), options.properties);
            e.printStackTrace();
        }
        Utils.resetDefaultLocale(settings.getProperty("locale.language"));
        //
        //if eu4.dir property is not valid, look into environment variable
        final String eu4dir = settings.getProperty("eu4.dir");
        if (eu4dir == null || !new File(eu4dir).exists()) {
            File dir = null;
            if (System.getenv("EU4_HOME") != null) {
                dir = new File(System.getenv("EU4_HOME"));
            }
            if (dir == null || !dir.exists()) {
                System.out.printf(l10n("app.eu4dir.error"));
                System.exit(-1);
            } else {
                settings.put("eu4.dir", dir.getPath());
            }
        }
        //
        //prepare replay
        final Replay replay = new Replay(settings);
        final Semaphore lock = new Semaphore(1);
        lock.acquire();
        replay.loadData(new EmptyTaskBridge<Void>() {
            @Override
            public void run() {
                lock.release();
            }
        });
        lock.acquire(); //wait for completation of loading data
        //
        //prepare giffers
        final String name = files.get(files.size() - 1).getAbsolutePath();
        final List<Pair<int[], Giffer>> giffers = new ArrayList<>();
        if (options.cultural) {
            final int[] buffer = replay.culturalBuffer;
            final Giffer giffer = new Giffer(settings, replay.bufferWidth, replay.bufferHeight, name + ".cultural");
            final Pair<int[], Giffer> pair = new Pair<>(buffer, giffer);
            giffers.add(pair);
        }
        if (options.religious) {
            final int[] buffer = replay.religiousBuffer;
            final Giffer giffer = new Giffer(settings, replay.bufferWidth, replay.bufferHeight, name + ".religious");
            final Pair<int[], Giffer> pair = new Pair<>(buffer, giffer);
            giffers.add(pair);
        }
        if (options.technologicalCombined) {
            final int[] buffer = replay.technologyCombinedBuffer;
            final Giffer giffer = new Giffer(settings, replay.bufferWidth, replay.bufferHeight, name + ".techcombined");
            final Pair<int[], Giffer> pair = new Pair<>(buffer, giffer);
            giffers.add(pair);
        }
        if (options.technologicalSeparate) {
            final int[] buffer = replay.technologySeparateBuffer;
            final Giffer giffer = new Giffer(settings, replay.bufferWidth, replay.bufferHeight, name + ".techseparate");
            final Pair<int[], Giffer> pair = new Pair<>(buffer, giffer);
            giffers.add(pair);
        }
        if (options.political || giffers.isEmpty()) { //if no mapmode specified, use political
            final int[] buffer = replay.politicalBuffer;
            final Giffer giffer = new Giffer(settings, replay.bufferWidth, replay.bufferHeight, name + ".political");
            final Pair<int[], Giffer> pair = new Pair<>(buffer, giffer);
            giffers.add(pair);
        }
        //
        replay.loadSaves(new EmptyTaskBridge<SaveGame>() {
            @Override
            public void updateTitle(final String title) {
                System.out.println(title);
            }
        }, EventProcessor.defaultListener, files);
        replay.setDateListener(new DateGenerator.IDateListener() {
            @Override
            public void update(Date date, double progress) {
                for (Pair<int[], Giffer> p : giffers) {
                    p.getSecond().updateGif(p.getFirst(), date);
                }
            }
        });
        replay.skipTo(replay.saveGame.date);
        for (Pair<int[], Giffer> p : giffers) {
            p.getSecond().endGif();
        }
    }

    /**
     * Gets property and parses it as an integer.
     * @param settings properties to get the value from
     * @param name property name
     * @param defaultString default property value
     * @param defaultInt default integer value
     * @return parsed property or defaultInt
     */
    static protected int parsePropertyWithDefault(final Properties settings,
            final String name, final String defaultString, final int defaultInt) {
        try {
            return Integer.parseInt(settings.getProperty(name, defaultString));
        } catch (NumberFormatException e) {
            return defaultInt;
        }
    }

    /**
     * Gets property and parses it as a float.
     * @param settings properties to get the value from
     * @param name property name
     * @param defaultString default property value
     * @param defaultFloat default float value
     * @return parsed property or defaultFloat
     */
    static protected float parsePropertyWithDefault(final Properties settings,
            final String name, final String defaultString, final float defaultFloat) {
        try {
            return Float.parseFloat(settings.getProperty(name, defaultString));
        } catch (NumberFormatException e) {
            return defaultFloat;
        }
    }

    /**
     * Gets property and parses it as a color.
     * @param settings properties to get the value from
     * @param name property name
     * @param defaultString default property value
     * @param defaultColor default color value
     * @return parsed property or defaultColor
     */
    static protected Color parsePropertyWithDefault(final Properties settings,
            final String name, final String defaultString, final Color defaultColor) {
        try {
            return Color.decode(settings.getProperty(name, defaultString));
        } catch (NumberFormatException e) {
            return defaultColor;
        }
    }

    /**
     * Writes the image to the sequence writer.
     * @param writer writer to write to
     * @param image image to write
     */
    static protected void write(final GifSequenceWriter writer, final BufferedImage image) {
        try {
            writer.writeToSequence(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Application settings. Handle with care, they're shared. */
    protected final Properties settings;

    /** Format for output gif files. */
    protected final String nameFormat;

    /** After this number of frames new gif file is created. */
    protected final int gifBreak;

    /** Period in ms between two frames in gif. */
    protected final int gifStep;

    /** Frame counter if gifBreak > 0. */
    protected int frameCounter = 1;

    /** File counter if gifBreak > 0. */
    protected int fileCounter = 1;

    /** Flag if only part of map should be giffed. */
    protected boolean gifSubImage;

    /** X-coord of gif subimage. */
    protected int gifSubImageX;

    /** Y-coord of gif subimage. */
    protected int gifSubImageY;

    /** Width of gif subimage. */
    protected int gifSubImageWidth;

    /** Height of gif subimage. */
    protected int gifSubImageHeight;

    /** Flag indicating whether the date should be drawn to gif. */
    protected boolean gifDateDraw;

    /** Font color of the gif date. */
    protected Color gifDateColor;

    /** Font size of the gif date. */
    protected float gifDateSize;

    /** X-coord of the gif date. */
    protected int gifDateX;

    /** Y-coord of the gif date. */
    protected int gifDateY;

    /** Outputstream of gif picture. */
    protected ImageOutputStream gifOutput = null;

    /** Writer of gif output. */
    protected GifSequenceWriter gifWriter = null;

    /** Delta between gif output dates. */
    protected int delta = 1;

    /** Delta's period. */
    protected Date.Period period;

    /** Buffered image representation of buffer. */
    protected final BufferedImage gifBufferedImage;

    /** Sized representation of {@link #gifBufferedImage}. */
    protected final BufferedImage gifSizedImage;

    /** On this date the gif should be updated. */
    protected Date gifTarget = null;

    /** Flag indicating whether the gif should loop continuously. */
    protected final boolean loop;

    /**
     * Only constructor.
     * @param settings application settings
     * @param width map width
     * @param height map height
     * @param baseName base file name
     */
    public Giffer(final Properties settings, final int width, final int height, final String baseName) {
        this.settings = settings;
        loop = settings.getProperty("gif.loop", "true").equals("true");
        gifBreak = parsePropertyWithDefault(settings, "gif.new.file", "0", 0);
        gifStep = parsePropertyWithDefault(settings, "gif.step", "100", 100);
        final String extension = gifBreak == 0 ? "" : ".%s";
        nameFormat = baseName + extension + ".gif";
        //
        gifSubImage = settings.getProperty("gif.subimage", "false").equals("true");
        gifSubImageX = parsePropertyWithDefault(settings, "gif.subimage.x", "0", 0);
        gifSubImageY = parsePropertyWithDefault(settings, "gif.subimage.y", "0", 0);
        gifSubImageWidth = parsePropertyWithDefault(settings, "gif.subimage.width",
                settings.getProperty("gif.width", "0"), 0);
        gifSubImageHeight = parsePropertyWithDefault(settings, "gif.subimage.height",
                settings.getProperty("gif.height", "0"), 0);
        //
        gifDateDraw = settings.getProperty("gif.date.draw", "true").equals("true");
        gifDateColor = parsePropertyWithDefault(settings, "gif.date.color", "0x000000", Color.BLACK);
        gifDateSize = parsePropertyWithDefault(settings, "gif.date.size", "12", 12);
        gifDateX = parsePropertyWithDefault(settings, "gif.date.x", "60", 60);
        gifDateY = parsePropertyWithDefault(settings, "gif.date.y", "60", 60);
        //
        delta = parsePropertyWithDefault(settings, "delta.per.tick", "1", 1);
        if (delta <= 0) {
            delta = 1;
        }
        try {
            period = Date.Period.values()[parsePropertyWithDefault(settings, "period.per.tick", "0", 0)];
        } catch (Exception e) {
            period = Date.DAY;
        }
        //
        fileCounter = 1;
        frameCounter = 0;
        createOutput();
        //
        gifBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int gifWidth = width;
        int gifHeight = height;
        try {
            gifWidth = parsePropertyWithDefault(settings, "gif.width", "5632", width);
            gifHeight = parsePropertyWithDefault(settings, "gif.height", "2048", height);
        } catch (Exception e) { }
        gifSizedImage = new BufferedImage(gifWidth, gifHeight, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Sets {@link #delta}.
     * @param delta new delta
     */
    public void setDelta(int delta) {
        this.delta = delta;
    }

    /**
     * Sets {@link #gifDateColor}.
     * @param gifDateColor new gif date color
     */
    public void setGifDateColor(Color gifDateColor) {
        this.gifDateColor = gifDateColor;
    }

    /**
     * Sets {@link #gifDateDraw}.
     * @param gifDateDraw new gif date draw
     */
    public void setGifDateDraw(boolean gifDateDraw) {
        this.gifDateDraw = gifDateDraw;
    }

    /**
     * Sets {@link #gifDateSize}.
     * @param gifDateSize new gif date size
     */
    public void setGifDateSize(float gifDateSize) {
        this.gifDateSize = gifDateSize;
    }

    /**
     * Sets {@link #gifDateX}.
     * @param gifDateX new gif date x
     */
    public void setGifDateX(int gifDateX) {
        this.gifDateX = gifDateX;
    }

    /**
     * Sets {@link #gifDateY}.
     * @param gifDateY new gif date y
     */
    public void setGifDateY(int gifDateY) {
        this.gifDateY = gifDateY;
    }

    /**
     * Sets {@link #gifSubImage}.
     * @param gifSubImage new gif subimage
     */
    public void setGifSubImage(boolean gifSubImage) {
        this.gifSubImage = gifSubImage;
    }

    /**
     * Sets {@link #gifSubImageHeight}.
     * @param gifSubImageHeight new gif subimage height
     */
    public void setGifSubImageHeight(int gifSubImageHeight) {
        this.gifSubImageHeight = gifSubImageHeight;
    }

    /**
     * Sets {@link #gifSubImageWidth}.
     * @param gifSubImageWidth new gif subimage width
     */
    public void setGifSubImageWidth(int gifSubImageWidth) {
        this.gifSubImageWidth = gifSubImageWidth;
    }

    /**
     * Sets {@link #gifSubImageX}.
     * @param gifSubImageX new gif subimage x
     */
    public void setGifSubImageX(int gifSubImageX) {
        this.gifSubImageX = gifSubImageX;
    }

    /**
     * Sets {@link #gifSubImageY}.
     * @param gifSubImageY new gif subimage y
     */
    public void setGifSubImageY(int gifSubImageY) {
        this.gifSubImageY = gifSubImageY;
    }

    /**
     * Sets {@link #period}.
     * @param period new period
     */
    public void setPeriod(Date.Period period) {
        this.period = period;
    }

    /**
     * Creates output file and fills gifOutput and gifWriter.
     */
    private void createOutput() {
        final File gifOutputFile = new File(String.format(nameFormat, fileCounter));
        gifOutputFile.delete();
        try {
            gifOutput = new FileImageOutputStream(gifOutputFile);
            gifWriter = new GifSequenceWriter(gifOutput, BufferedImage.TYPE_INT_ARGB, gifStep, loop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the output file.
     */
    public void endGif() {
        try {
            gifWriter.close();
            gifOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        gifWriter = null;
        gifOutput = null;
    }

    /**
     * Potentionally updates the output gif if the time is right.
     * @param buffer buffer with picture
     * @param date current date
     */
    public void updateGif(final int[] buffer, final Date date) {
        if (gifTarget != null && !date.equals(gifTarget)) {
            return;
        }
        gifTarget = date.skip(period, delta);
        final int[] a = ( (DataBufferInt) gifBufferedImage.getRaster().getDataBuffer() ).getData();
        System.arraycopy(buffer, 0, a, 0, buffer.length);
        final Graphics g = gifSizedImage.getGraphics();
        final BufferedImage src = gifSubImage ? gifBufferedImage.getSubimage(gifSubImageX, gifSubImageY, gifSubImageWidth, gifSubImageHeight) : gifBufferedImage;
        g.drawImage(src, 0, 0, gifSizedImage.getWidth(), gifSizedImage.getHeight(), null);
        if (gifDateDraw) {
            g.setColor(gifDateColor);
            g.setFont(g.getFont().deriveFont(gifDateSize));
            g.drawString(date.toString(), gifDateX, gifDateY);
        }
        g.dispose();
        write(gifWriter, gifSizedImage);
        if (gifBreak != 0 && ++frameCounter >= gifBreak) {
            endGif();
            ++fileCounter;
            createOutput();
            write(gifWriter, gifSizedImage);
            frameCounter = 1;
        }
    }
}
