package com.paradoxplaza.eu4.replayer.gif;

import com.paradoxplaza.eu4.replayer.Date;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Class responsible for creating gifs.
 */
public class Giffer {

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
    protected final boolean gifSubImage;

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

    /**
     * Only constructor.
     * @param settings application settings
     * @param width map width
     * @param height map height
     * @param baseName base file name
     */
    public Giffer(final Properties settings, final int width, final int height, final String baseName) {
        this.settings = settings;
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
     * Creates output file and fills gifOutput and gifWriter.
     */
    private void createOutput() {
        final File gifOutputFile = new File(String.format(nameFormat, fileCounter));
        gifOutputFile.delete();
        try {
            gifOutput = new FileImageOutputStream(gifOutputFile);
            gifWriter = new GifSequenceWriter(gifOutput, BufferedImage.TYPE_INT_ARGB, gifStep, true);
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
            createOutput();
            write(gifWriter, gifSizedImage);
            frameCounter = 1;
        }
    }
}
