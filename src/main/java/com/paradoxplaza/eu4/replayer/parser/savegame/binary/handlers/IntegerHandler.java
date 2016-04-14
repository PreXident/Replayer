package com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers;

import static com.paradoxplaza.eu4.replayer.Date.monthsDays;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Flag;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IParserContext;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.SPACE;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.charset;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.ReverseByteArrayOutputStream;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.Token;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Locale;

/**
 * Handles integer value in binary save context.
 */
public class IntegerHandler extends SingleValueHandler {

    /** Bytes representing dot. */
    static final byte[] DOT = ".".getBytes(charset);

    /** Bytes representing numbers. */
    static final byte[][] NUMBERS = {
        "0".getBytes(charset),
        "1".getBytes(charset),
        "2".getBytes(charset),
        "3".getBytes(charset),
        "4".getBytes(charset),
        "5".getBytes(charset),
        "6".getBytes(charset),
        "7".getBytes(charset),
        "8".getBytes(charset),
        "9".getBytes(charset),
    };

    /** Bytes representing dash. */
    static final byte[] DASH = "-".getBytes(charset);

    /** Bytes representing dot. */
    static final byte[] ZERO = "0".getBytes(charset);

    /** Store flags here to restore flags for nested ints. */
    protected Deque<EnumSet<Flag>> flagStack = new ArrayDeque<>(16);

    /** Store brace tokens here to restore flags for nested ints. */
    protected Deque<Token> intStack = new ArrayDeque<>(16); //probably not necessary, just to be sure

    /** Here the number bytes will be read. */
    final byte[] bytes = new byte[4];

    /** Buffer for reversing output for numbers. */
    final ReverseByteArrayOutputStream buf = new ReverseByteArrayOutputStream(32);

    @Override
    public void handled() {
        final Token token = intStack.pop();
        token.flags.clear();
        token.flags.addAll(flagStack.pop());
    }

    /**
     * Outputs number with five decimal places.
     * @param context binary save context
     * @param number float to output
     * @throws java.io.IOException if any IO error occurs
     */
    protected void outputFloat5(final IParserContext context, final int number)
            throws IOException {
        final double d = (number * 2) / 256.0 / 256.0;
        final String string = String.format(Locale.ENGLISH, "%.5f", d);
        final byte[] out  = string.getBytes(charset);
        context.getOutputStream().write(out);
    }

    /**
     * Outputs number with three decimal places.
     * @param number float to output
     */
    protected void outputFloat(int number) {
        if (number == 0) {
            buf.write(NUMBERS[0]);
            return;
        }
        final int dec = outputInt(Math.abs(number % 1000));
        for (int i = dec; i < 3; ++i) {
            buf.write(NUMBERS[0]);
        }
        buf.write(DOT);
        final int floor = number / 1000;
        if (floor == 0) {
            buf.write(NUMBERS[0]);
            if (number < 0) {
                buf.write(DASH);
            }
        } else {
            outputInt(floor);
        }
    }

    /**
     * Outputs integral number.
     * @param number integral number
     * @return number of digits printed
     */
    protected int outputInt(int number) {
        if (number == 0) {
            buf.write(NUMBERS[0]);
            return 1;
        }
        boolean minus;
        if (number < 0) {
            minus = true;
            number = -number;
        } else {
            minus = false;
        }
        int count = 0;
        while (number > 0) {
            ++count;
            buf.write(NUMBERS[number % 10]);
            number /= 10;
        }
        if (minus) {
            buf.write(DASH);
        }
        return count;
    }

    /**
     * Outputs date.
     * @param flags context flags
     * @param number date
     */
    protected void outputDate(final EnumSet<Flag> flags, final int number) {
        int days = number / 24;
        final int year = -5000 + days / 365;
        int month = 1;
        days = 1 + days % 365;
        for (int i = 0; i < monthsDays.length; ++i) {
            if (days > monthsDays[i]) {
                days -= monthsDays[i];
                ++month;
            } else {
                break;
            }
        }
        if (flags.contains(Flag.QUOTED_DATE)) {
            buf.write(QUOTE);
        }
        outputInt(days);
        buf.write(DOT);
        outputInt(month);
        buf.write(DOT);
        outputInt(year);
        if (flags.contains(Flag.QUOTED_DATE)) {
            buf.write(QUOTE);
        }
    }

    @Override
    protected void handleValue(final IParserContext context) throws IOException {
        readBytes(context.getInputStream(), bytes);
        final int number = toNumber(bytes);
        final EnumSet<Flag> flags = context.getContext().peek().flags;
        final OutputStream output = context.getOutputStream();
        buf.reset();
        if (flags.contains(Flag.INTEQFLOAT)) {
            if (context.getContext().peek().index == 0x0C00) { //int before equals
                outputFloat(number);
                buf.writeTo(output);
            } else {
                outputInt(number);
                buf.writeTo(output);
            }
        }else if (flags.contains(Flag.DATE) || flags.contains(Flag.QUOTED_DATE)) {
            outputDate(flags, number);
            buf.writeTo(output);
        } else if (flags.contains(Flag.INTEGER)) {
            outputInt(number);
            buf.writeTo(output);
        } else if (flags.contains(Flag.FLOAT)) {
            outputFloat(number);
            buf.writeTo(output);
        } else if (flags.contains(Flag.FLOAT5)) {
            outputFloat5(context, number);
        } else {
            Token token = context.getContext().peek();
            if (token.index == 0x0300) { // {
                token = getToken(context, 1);
            }
            System.err.printf("No number flag specified for %s (0x%04X)\n", token.text, token.index);
            outputInt(number);
            buf.writeTo(output);
        }

        //copy flags, needed from string=value
        //store flags into flagStack, store token into flagStack
        final Token token = context.getCurrentToken();
        flagStack.push(EnumSet.copyOf(token.flags));
        token.flags.clear();
        intStack.push(token);
        //copy flags from determining token
        token.flags.addAll(flags);
    }

    @Override
    public void printIndent(final IParserContext context) throws IOException {
        if (context.getPrettyPrint()
                && context.getContext().peek().index == 0x0300 // {
                && !context.getContext().peek().flags.contains(Flag.PRETTY_LIST)) {
            if (context.getContext().peek().flags.contains(Flag.QUOTED_DATE)) {
                context.getOutputStream().write(IronmanStream.NEW_LINE); //quoted dates do not indent
            } else {
                if (context.getLastToken().index == 0x0300) { // { -> first in list
                    super.printIndent(context);
                } else {
                    context.getOutputStream().write(SPACE);
                }
            }
        } else {
            super.printIndent(context);
        }
    }
}
