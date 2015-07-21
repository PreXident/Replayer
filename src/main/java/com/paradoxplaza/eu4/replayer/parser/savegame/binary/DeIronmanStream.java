package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import com.paradoxplaza.eu4.replayer.parser.savegame.Utils;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.State;
import static com.paradoxplaza.eu4.replayer.parser.savegame.binary.IronmanStream.charset;
import com.paradoxplaza.eu4.replayer.parser.savegame.binary.handlers.SingleValueHandler;
import java.io.IOException;
import java.io.InputStream;

/**
 * Deironmans the binary save stream while keeping the binary format.
 */
public class DeIronmanStream extends InputStream {

    /** Initial size of the buffer. */
    static protected final int INITIAL_BUFFER_SIZE = 64;

    /** Underlaying input stream with binary save. */
    protected final InputStream in;

    /** Output buffer. */
    protected final ByteArrayOutputStream buff = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);

    /** Input buffer. */
    protected final byte[] inBuff = new byte[64];

    /** Next byte should be return from buff on this position. */
    protected int bufPos = 0;

    /** Indicator what is about to be read now. */
    protected State state = State.START;

    /**
     * Only constructor.
     * @param in EU4bin stream to be converted
     * @throws java.io.IOException if any IO error occurs
     */
    public DeIronmanStream(final InputStream in) throws IOException {
        this.in = in;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public int read() throws IOException {
        if (bufPos < buff.size()) {
            return buff.getByte(bufPos++) & 0xFF;
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
        return state == State.END ? -1 : buff.getByte(bufPos++) & 0xFF;
    }

    /**
     * Reads header.
     * @throws IOException if something goes wrong
     */
    private void readStart() throws IOException {
        final byte[] header = new byte[6];
        final int count = Utils.ensureRead(in, header);
        if (count != 6 || !new String(header, charset).equals("EU4bin")) {
            throw new IOException(l10n("parser.binary.notEU4"));
        }
        state = State.TOKEN;
        buff.reset(); //throw away bytes from auxilliary context
        buff.write(header);
    }

    /**
     * Reads token, filters the ironman related stuff.
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
        int length;
        final byte[] bytes;
        switch (token) {
            case 0x0C00: //integer
            case 0x0D00: //float
            case 0x1400: //integer
                copyToken(b1, b2, 4);
                break;
            case 0x0E00: //boolean
                copyToken(b1, b2, 1);
                break;
            case 0x0F00: //string
                buff.write(b1);
                buff.write(b2);
                Utils.ensureRead(in, inBuff, 2);
                length = SingleValueHandler.toNumber(inBuff, 0, 2);
                buff.write(inBuff, 0, 2);
                bytes = new byte[length];
                Utils.ensureRead(in, bytes);
                buff.write(bytes);
                break;
            case 0x182E: //FILE_NAME
            case 0xD72F: //FILE_NAME
                Utils.ensureRead(in, inBuff, 6); //read "= STR LEN"
                length = SingleValueHandler.toNumber(inBuff, 4, 2);
                bytes = new byte[length];
                Utils.ensureRead(in, bytes);
                break;
            case 0x992C: //setgameplayoptions
                buff.write(b1);
                buff.write(b2);
                Utils.ensureRead(in, inBuff, 4 + 10 * 6); //read "= {" and 10 times number
                inBuff[63] = 0;
                inBuff[62] = 0;
                inBuff[61] = 0;
                inBuff[60] = 1;
                buff.write(inBuff);
                break;
            case 0x8D2A: //FINISH
                Utils.ensureRead(in, inBuff, 4); //read "= {"
                while (true) {
                    Utils.ensureRead(in, inBuff, 2);
                    if (inBuff[0] == 0x04 && inBuff[1] == 0x00) { // }
                        break;
                    }
                    Utils.ensureRead(in, inBuff, 4); //number;
                }
                break;
            default:
                buff.write(b1);
                buff.write(b2);
                break;
        }
    }

    private void copyToken(int b1, int b2, int len) throws IOException {
        buff.write(b1);
        buff.write(b2);
        Utils.ensureRead(in, inBuff, len);
        buff.write(inBuff, 0, len);
    }
}
