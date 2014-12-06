package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.DefaultHandler;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.IHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Transforms binary eu4 save game stream to text eu4 save game stream.
 */
public class IronmanStream extends InputStream implements IParserContext {

    /** Initial size of the buffer. */
    static protected final int INITIAL_BUFFER_SIZE = 64;

    /** Minimal size of PushbackInputStream. */
    static protected final int PUSH_BACK_BUFFER_SIZE = 4;

    /** Charset to use. Both input and output. */
    static public final Charset charset = StandardCharsets.ISO_8859_1;

    /** Bytes representing system's line separator. */
    static public final byte[] NEW_LINE = System.lineSeparator().getBytes(charset);

    /** Bytes representing token separator. */
    static public final byte[] SPACE = " ".getBytes(charset);

    /** Bytes representing first token in textual save. */
    static public final byte[] EU4txt = "EU4txt".getBytes(charset);

    /**
     * Indicator what is about to be read now.
     */
    protected enum State {
        /** Read the header "EU4bin". */
        START,
        /** Read next token. */
        TOKEN,
        /** Nothing to read, return -1. */
        END
    }

    /** Token occuring in EU4bin. */
    protected final Token[] tokens = new Token[0xFFFF + 1];

    /** Underlaying input stream with binary save. */
    protected final PushbackInputStream in;

    /** Context of the save game. */
    protected final Deque<Token> context = new ArrayDeque<>(16);

    /** Output buffer. */
    protected final ByteArrayOutputStream buff = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);

    /** Next byte should be return from buff on this position. */
    protected int bufPos = 0;

    /** Indicator what is about to be read now. */
    protected State state = State.START;

    /** Flag indicating whether output should be nice. */
    protected boolean prettyPrint = false;

    /** Current indendation level. */
    protected int indent = 0;

    /** Previous handled token. */
    protected Token lastToken = null;

    /** Currently handled token. */
    protected Token currentToken = null;

    /**
     * Creates IronmanStream from underlaying PushbackInputStream.
     * It's buffer's size must be at least {@link #PUSH_BACK_BUFFER_SIZE}.
     * @param in EU4bin stream to be converted
     * @throws java.io.IOException if any IO error occurs
     */
    public IronmanStream(final PushbackInputStream in) throws IOException {
        this.in = in;
        //initialize tokens
        final Map<String, IHandler> handlers = new HashMap<>();
        handlers.put("", DefaultHandler.INSTANCE);
        //
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(IronmanStream.class.getResourceAsStream("/tokens.csv")))) {
            reader.readLine(); //skip header
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final String[] parts = line.split(";", -1);
                final int index = Integer.parseInt(parts[0].substring(2), 16);
                final String text = parts[1];
                final EnumSet<Flag> flags = Flag.parse(parts[2]);
                IHandler handler = handlers.get(parts[3]);
                if (handler == null) {
                    try {
                        handler = (IHandler) Class.forName(parts[3]).newInstance();
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        System.err.println("Cannot instantiate handler: " + parts[3]);
                        e.printStackTrace(System.err);
                    }
                }
                if (tokens[index] != null) {
                    System.err.println("Token " + parts[0] + " redefined!");
                }
                tokens[index] = new Token(index, text, flags, handler);
            }
        }
    }

    /**
     * Creates IronmanStream from ordinary stream.
     * @param in EU4bin stream to be converted
     * @throws java.io.IOException if any IO error occurs
     */
    public IronmanStream(final InputStream in) throws IOException {
        this(new PushbackInputStream(in, PUSH_BACK_BUFFER_SIZE));
    }

    @Override
    public PushbackInputStream getInputStream() {
        return in;
    }

    @Override
    public OutputStream getOutputStream() {
        return buff;
    }

    @Override
    public Deque<Token> getContext() {
        return context;
    }

    @Override
    public Token getLastToken() {
        return lastToken;
    }

    @Override
    public Token getCurrentToken() {
        return currentToken;
    }

    @Override
    public boolean getPrettyPrint() {
        return prettyPrint;
    }

    @Override
    public void setPrettyPrint(final boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    @Override
    public int getIndent() {
        return indent;
    }

    @Override
    public int increaseIndent() {
        return ++indent;
    }

    @Override
    public int decreaseIndent() {
        return --indent;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public int read() throws IOException {
        if (bufPos < buff.size()) {
            return buff.getByte(bufPos++);
        }
        switch (state) {
            case START:
                readStart();
                break;
            case TOKEN:
                buff.reset();
                bufPos = 0;
                //read until something is in buffer or eof
                while (buff.size() == 0 && state != State.END) {
                    readToken();
                }
                break;
            case END:
                return -1;
        }
        return state == State.END ? -1 : buff.getByte(bufPos++);
    }

    /**
     * Reads header, prepares context for reading tokens.
     * @throws IOException if something goes wrong
     */
    private void readStart() throws IOException {
        final byte[] header = new byte[6];
        final int count = in.read(header);
        if (count != 6 || !new String(header, charset).equals("EU4bin")) {
            throw new IOException(l10n("parser.binary.notEU4"));
        }
        state = State.TOKEN;
        lastToken = tokens[0x0000];
        currentToken = tokens[0x0100];
        currentToken.handler.handleToken(this);
        lastToken = currentToken;
        currentToken = tokens[0x0300];
        currentToken.handler.handleToken(this);
        lastToken = currentToken;
        buff.reset(); //throw away bytes from auxilliary context
        indent = 0;
        buff.write(EU4txt);
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
            bufPos = Integer.MAX_VALUE;
            return;
        }
        final int b2 = in.read();
        if (b2 == -1) {
            state = State.END;
            bufPos = Integer.MAX_VALUE;
            return;
        }
        final int token = (b1 << 8) + b2;
        currentToken = tokens[token];
        if (currentToken == null) {
            final String hexa = String.format("0x%04X", token & 0xFFFF);
            System.err.println("Encountered unknown token " + hexa + ", trying to recover...");
            currentToken = new Token(token & 0xFFFF, "UNKNOWN_" + hexa);
            tokens[token] = currentToken;
        }
        currentToken.handler.handleToken(this);
        lastToken = currentToken;
    }
}
