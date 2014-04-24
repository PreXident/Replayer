package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

import com.paradoxplaza.eu4.replayer.Date;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

/**
 * Transforms binary eu4 save game stream to text eu4 save game stream.
 */
public class IronmanStream extends InputStream {

    /** Minimal size of PushbackInputStream. */
    static final int PUSH_BACK_BUFFER_SIZE = 4;

    /** Charset to use. Both input and output. */
    static final Charset charset = StandardCharsets.ISO_8859_1;

    /** Token occuring in EU4bin. */
    static final TokenInfo[] tokens = new TokenInfo[0xFFFF + 1];

    /** Initialization of tokens. */
    static {
        final Map<String, ITokenProcessor> processors = new HashMap<>();
        processors.put("ActionProcessor", new ActionProcessor());
        processors.put("BooleanProcessor", new BooleanProcessor());
        processors.put("CloseBraceProcessor", new CloseBraceProcessor());
        processors.put("DiplomacyConstructionProcessor", new DiplomacyConstructionProcessor());
        processors.put("DiscoveredByProcessor", new DiscoveredByProcessor());
        processors.put("EnvoyProcessor", new EnvoyProcessor());
        processors.put("FloatProcessor", new FloatProcessor());
        processors.put("NodeProcessor", new NodeProcessor());
        processors.put("NumberProcessor", new NumberProcessor());
        processors.put("PowerProcessor", new PowerProcessor());
        processors.put("RivalProcessor", new RivalProcessor());
        processors.put("StringProcessor", new StringProcessor());
        processors.put("TotalProcessor", new TotalProcessor());
        processors.put("ValueIntProcessor", new ValueIntProcessor());
        processors.put("ValueProcessor", new ValueProcessor());
        //
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(IronmanStream.class.getResourceAsStream("/tokens.csv")))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final String[] parts = line.split(";");
                final int index = Integer.parseInt(parts[0].substring(2), 16);
                final String text = parts[1].equals("null") ? null : parts[1].replace("\\n", "\n");
                final Output output = parts[2].equals("null") ? null : Output.valueOf(parts[2]);
                final boolean list = parts[3].equals("true");
                final ITokenProcessor processor = processors.get(parts[4]);
                if (processor == null && !parts[4].equals("null")) {
                    System.err.println("Unknown processor: " + parts[4]);
                }
                tokens[index] = new TokenInfo(text, output, list, processor);
            }
        } catch (Exception e) {
            System.err.println("Error while initializing the IronmanStream!");
            e.printStackTrace();
        }
    }

    /**
     * Reads count bytes from is.
     * @param is input stream to read from
     * @param count read this number of bytes
     * @return read bytes
     * @throws IOException when IOException occurs during reading
     * or not enough bytes are read
     */
    static private byte[] readBytes(final InputStream is, final int count)
            throws IOException {
        final byte[] bytes = new byte[count];
        readBytes(is, bytes);
        return bytes;
    }

    /**
     * Reads count bytes from is.
     * @param is input stream to read from
     * @param out array to store read bytes
     * @throws IOException when IOException occurs during reading
     * or not enough bytes are read
     */
    static private void readBytes(final InputStream is, final byte[] out)
            throws IOException {
        if (is.read(out) != out.length) {
            throw new IOException(l10n("parser.eof.unexpected"));
        }
    }

    /**
     * Indicator what is about to be read now.
     */
    enum State {
        /** Read the header "EU4bin". */
        START,
        /** Read next token. */
        TOKEN,
        /** Nothing to read, return -1. */
        END
    }

    /**
     * Context for ITokenProcessors.
     */
    enum Output {
        /** Output date. */
        DATE,
        /** Output quoted date. */
        QUOTED_DATE,
        /** Output string. */
        STRING,
        /** Output quoted string. */
        QUOTED_STRING,
        /** Output integer. */
        INT,
        /** Output decimal number with 3 decimal places. */
        DECIMAL,
        /** AMBIGUOUS can be followed by INT or QUOTED_STRING. */
        AMBIGUOUS_INT_QSTRING,
        /** AMBIGUOUS can be followed by DECIMAL or QUOTED_STRING. */
        AMBIGUOUS_DECIMAL_QSTRING,
        /** Indicator to clear the output to null. */
        NONE
    }

    /**
     * Context of the save game.
     */
    enum Context {
        /** Content of action token should be int. */
        ACTION_INT,
        /** Content of action token should be quoted string. */
        ACTION_STRING,
        /** Content of total token should be decimal. */
        TOTAL_DECIMAL,
        /** Content of value token should be int. */
        VALUE_INT
    }

    /** Underlaying input stream with binary save. */
    final PushbackInputStream in;

    /** Context of the save game. */
    final LinkedList<Context> context = new LinkedList<>();

    /** Accumulator of the output. */
    final StringBuilder builder = new StringBuilder();

    /** Output buffer.
     * Strings to be sent up are stored here.
     */
    byte[] buff = new byte[0];

    /** Next byte should be return from buff on this position. */
    int bufPos = 0;

    /** Indicator what is about to be read now. */
    State state = State.START;

    /** Context for ITokenProcessors. */
    Output output = null;

    /** Flag indicating whether list of values is being processed. */
    boolean inList = false;

    /**
     * Creates IronmanStream from underlaying PushbackInputStream.
     * It's buffer's size must be at least {@link #PUSH_BACK_BUFFER_SIZE}.
     * @param in EU4bin stream to be converted
     */
    public IronmanStream(final PushbackInputStream in) {
        this.in = in;
    }

    /**
     * Creates IronmanStream from ordinary stream.
     * @param in EU4bin stream to be converted
     */
    public IronmanStream(final InputStream in) {
        this.in = new PushbackInputStream(in, PUSH_BACK_BUFFER_SIZE);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public int read() throws IOException {
        if (bufPos < buff.length) {
            return buff[bufPos++];
        }
        switch (state) {
            case START:
                readStart();
                break;
            case TOKEN:
                readToken();
                break;
            case END:
                return -1;
        }
        return state == State.END ? -1 : buff[bufPos++];
    }

    /**
     * Reads header.
     * @throws IOException if something goes wrong
     */
    private void readStart() throws IOException {
        final byte[] header = new byte[6];
        final int count = in.read(header);
        if (count != 6 || !new String(header, charset).equals("EU4bin")) {
            throw new IOException(l10n("parser.binary.notEU4"));
        }
        buff = "EU4txt ".getBytes(charset);
        bufPos = 0;
        state = State.TOKEN;
    }

    /**
     * Reads token, consults tokens hashmap and behaves accordingly.
     * Updates buff, bufPos, output and state.
     * @throws IOException when something goes wrong
     */
    private void readToken() throws IOException {
        final int b1 = in.read();
        if (b1 == -1) {
            state = State.END;
            return;
        }
        final int b2 = in.read();
        if (b2 == -1) {
            state = State.END;
            return;
        }
        final int token = (b1 << 8) + b2;
        TokenInfo info = tokens[token];
        if (info == null) {
//            throw new IOException(String.format(
//                    l10n("parser.binary.token.unknown"), token & 0xFFFF));
            final String hexa = String.format("0x%04X", token & 0xFFFF);
            System.err.println("Encountered unknown token " + hexa + ", trying to recover...");
            info = new TokenInfo("UNKNOWN_" + hexa);
        }
        if (info.output != null) {
            output = info.output == Output.NONE ? null : info.output;
        }
        if (info.list) {
            inList = true;
        }
        builder.setLength(0);
        builder.append(info.text);
        if (info.processor != null) {
            info.processor.processToken(this, builder);
        }
        builder.append(' ');
        buff = builder.toString().getBytes(charset);
        bufPos = 0;
    }

    /**
     * Simple interface called when specific token should be processed.
     */
    interface ITokenProcessor {

        /**
         * Reads is.in and appends to builder.
         * @param is calling IronmanStream
         * @param builder EU4txt output
         * @throws IOException when something goes wrong
         */
        void processToken(IronmanStream is, StringBuilder builder) throws IOException;
    }

    /**
     * Processes action keyword.
     */
    static class ActionProcessor implements ITokenProcessor {
        @Override
        public final void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            if (is.context.contains(Context.ACTION_INT)) {
                is.output = Output.INT;
            } else if (is.context.contains(Context.ACTION_STRING)) {
                is.output = Output.QUOTED_STRING;
            }
        }
    }

    /**
     * Processes booleans.
     */
    static class BooleanProcessor extends SingleValueProcessor {

        @Override
        public void processValue(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            final int flag = is.in.read();
            if (flag == -1) {
                throw new IOException(l10n("parser.eof.unexpected"));
            }
            builder.append(flag > 0 ? "yes" : "no");
        }
    }

    /**
     * Processes closing brace }.
     */
    static class CloseBraceProcessor implements ITokenProcessor {
        @Override
        public void processToken(IronmanStream is, StringBuilder builder) throws IOException {
            is.inList = false;
            is.output = null;
            if (!is.context.isEmpty()) {
                is.context.pop();
            }
        }
    }

    /**
     * Processes diplomacy_construction token.
     */
    static class DiplomacyConstructionProcessor implements ITokenProcessor {
        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            is.context.push(Context.ACTION_STRING);
        }
    }

    /**
     * Processes discovered_by token.
     */
    static class DiscoveredByProcessor implements ITokenProcessor {

        /** Here the next two tokens will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final short token1 = (short) ((bytes[0] << 8) + bytes[1]);
            final short token2 = (short) ((bytes[2] << 8) + bytes[3]);
            //does the list follow?
            if (token1 == (short) 0x0100 /*=*/
                    && token2 == (short) 0x0300 /*{*/) {
                is.inList = true;
                is.output = Output.STRING;
            }
            is.in.unread(bytes);
        }
    }

    /**
     * Processes closing brace }.
     */
    static class EnvoyProcessor implements ITokenProcessor {

        /** Here the next two tokens will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processToken(IronmanStream is, StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final short token1 = (short) ((bytes[0] << 8) + bytes[1]);
            final short token2 = (short) ((bytes[2] << 8) + bytes[3]);
            //does the list follow?
            if (token1 == (short) 0x0100 /*=*/
                    && token2 == (short) 0x0300 /*{*/) {
                is.context.push(Context.ACTION_INT);
            }
            is.in.unread(bytes);
        }
    }

    /**
     * Processes floats.
     */
    static class FloatProcessor extends SingleValueProcessor {

        /** Here the float bytes will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processValue(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            for(int i = 0, o = bytes.length -1; i < bytes.length / 2; ++i, --o) {
                final byte swap = bytes[i];
                bytes[i] = bytes[o];
                bytes[o] = swap;
            }
            final ByteBuffer bb = ByteBuffer.wrap(bytes);
            final float f = bb.getFloat();
            builder.append(String.format(Locale.ENGLISH, "%.3f", f));
        }
    }

    /**
     * Processes node token.
     */
    static class NodeProcessor implements ITokenProcessor {
        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            is.context.push(Context.TOTAL_DECIMAL);
        }
    }

    /**
     * Processes numbers and numeric dates.
     */
    static class NumberProcessor extends SingleValueProcessor {

        /** Numeric dates are represented as number of hours since this date. */
        static final Date start = new Date((short) -5000, (byte) 1, (byte) 1);

        /** Here the number bytes will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processValue(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final int number = toNumber(bytes);
            if (is.output == null) {
                //throw new IllegalStateException(l10n("parser.binary.output.none"));
                builder.append(number);
                return;
            }
            switch (is.output) {
                case DATE:
                    final Date date = start.skip(Date.Period.DAY, number / 24);
                    builder.append(date);
                    break;
                case QUOTED_DATE:
                    final Date qdate = start.skip(Date.Period.DAY, number / 24);
                    builder.append('"');
                    builder.append(qdate);
                    builder.append('"');
                    break;
                case INT:
                case AMBIGUOUS_INT_QSTRING:
                    builder.append(number);
                    break;
                case DECIMAL:
                case AMBIGUOUS_DECIMAL_QSTRING:
                    float f = number / 1000f;
                    builder.append(String.format(Locale.ENGLISH, "%.3f", f));
                    break;
                default:
                    throw new IllegalStateException(String.format(
                            l10n("parser.binary.output.invalid"), is.output));
            }
        }
    }

    /**
     * Processes power token.
     */
    static class PowerProcessor implements ITokenProcessor {

        /** Here the next two tokens will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final short token1 = (short) ((bytes[0] << 8) + bytes[1]);
            final short token2 = (short) ((bytes[2] << 8) + bytes[3]);
            //does the list follow?
            if (token1 == (short) 0x0100 /*=*/
                    && token2 == (short) 0x0300 /*{*/) {
                is.context.push(Context.TOTAL_DECIMAL);
            }
            is.in.unread(bytes);
        }
    }


    /**
     * Processes discovered_by token.
     */
    static class RivalProcessor implements ITokenProcessor {

        /** Here the next two tokens will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final short token1 = (short) ((bytes[0] << 8) + bytes[1]);
            final short token2 = (short) ((bytes[2] << 8) + bytes[3]);
            //does the list follow?
            if (token1 == (short) 0x0100 /*=*/
                    && token2 == (short) 0x0300 /*{*/) {
                is.context.push(Context.VALUE_INT);
            }
            is.in.unread(bytes);
        }
    }

    /**
     * Common ancestor for Processors of values.
     * If not in a list, clears the is.output.
     */
    static abstract class SingleValueProcessor implements ITokenProcessor {

        /**
         * Converts byte array to integer it represents.
         * @param bytes convert these bytes
         * @return converted integer
         */
        static protected int toNumber(final byte[] bytes) {
            int number = 0;
            for(int i = bytes.length - 1; i >= 0; --i) {
                number <<= 8;
                number += bytes[i] & 0xff;
            }
            return number;
        }

        @Override
        public final void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            processValue(is, builder);
            if (!is.inList) {
                is.output = null;
            }
        }

        /**
         * Processes the value.
         * @param is calling IronmanStream
         * @param builder EU4txt output
         * @throws IOException when something goes wrong
         */
        protected abstract void processValue(IronmanStream is,
                StringBuilder builder) throws IOException;
    }

    /**
     * Processes strings.
     */
    static class StringProcessor extends SingleValueProcessor {

        /** Here the string size will be read. */
        final byte[] lengthBytes = new byte[2];

        @Override
        public void processValue(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, lengthBytes);
            final int number = toNumber(lengthBytes);
            final byte[] stringBytes = readBytes(is.in, number);
            final String string = new String(stringBytes, charset);
            if (is.output == null) {
                builder.append(string);
                return;
            }
            switch (is.output) {
                case QUOTED_STRING:
                case AMBIGUOUS_INT_QSTRING:
                case AMBIGUOUS_DECIMAL_QSTRING:
                    builder.append('"');
                    builder.append(string);
                    builder.append('"');
                    break;
                default:
                case STRING:
                    builder.append(string);
                    break;
                    //throw new IllegalStateException(String.format(l10n("parser.binary.output.invalid"), is.output));
            }
        }
    }

    /**
     * Processes total keyword.
     */
    static class TotalProcessor implements ITokenProcessor {
        @Override
        public final void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            if (is.context.contains(Context.TOTAL_DECIMAL)) {
                is.output = Output.DECIMAL;
            }
        }
    }

    /**
     * Processes tokens that switches context to VALUE_INT.
     */
    static class ValueIntProcessor implements ITokenProcessor {
        @Override
        public void processToken(IronmanStream is, StringBuilder builder) throws IOException {
            is.context.push(Context.VALUE_INT);
        }
    }

    /**
     * Processes value keyword.
     */
    static class ValueProcessor implements ITokenProcessor {
        @Override
        public final void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            if (is.context.contains(Context.VALUE_INT)) {
                is.output = Output.INT;
            }
        }
    }

    /**
     * Information how the token should be processed.
     */
    static class TokenInfo {

        /** Output text. Is not null. */
        public final String text;

        /** Output expected after the token. */
        public final Output output;

        /** If non-static token, processor handles further reading and converting. */
        public final ITokenProcessor processor;

        /** Flag indicating whether the token is followed by list of values.  */
        public final boolean list;

        public TokenInfo(final String text) {
            this(text, (Output) null);
        }

        public TokenInfo(final String text,
                final Output expectedOutput) {
            this(text, expectedOutput, false);
        }

        public TokenInfo(final String text,
                final Output expectedOutput, final boolean list) {
            this(text, expectedOutput, list, null);
        }

        public TokenInfo(final String text, final ITokenProcessor processor) {
            this(text, null, false, processor);
        }

        public TokenInfo(final ITokenProcessor processor) {
            this("", null, false, processor);
        }

        public TokenInfo(final String text,
                final Output expectedOutput, final boolean list,
                final ITokenProcessor processor) {
            assert text != null : "text cannot be null, use empty string instead!";
            this.text = text;
            this.output = expectedOutput;
            this.list = list;
            this.processor = processor;
        }
    }
}
