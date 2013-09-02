package com.paradoxplaza.eu4.replayer.defaultmap;

import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Map;
import java.util.Set;
import javafx.scene.paint.Color;

/**
 * Start state of default.map parser.
 */
public class Start extends com.paradoxplaza.eu4.replayer.parser.Start<Pair<Set<Color>, Map<String, Color>>> {

    /** State ignoring everything till matching }. */
    final Ignore<Pair<Set<Color>, Map<String, Color>>> ignore = new Ignore<>(this);

    /** Processes seas_start. */
    final Seas seas = new Seas(this);

    /**
     * Only contructor.
     */
    public Start() {
        super(null);
    }

    @Override
    public Start end(final Pair<Set<Color>, Map<String, Color>> context) {
        return this;
    }

    @Override
    protected StreamTokenizer _createTokenizer(InputStream input) {
        final StreamTokenizer t = new StreamTokenizer(new BufferedReader(new InputStreamReader(input)));
        t.resetSyntax();
        t.commentChar('#');
        t.eolIsSignificant(false);
        t.lowerCaseMode(false);
        t.wordChars('.', '.');
        t.wordChars('0', '9');
        t.wordChars('a', 'z');
        t.wordChars('A', 'Z');
        t.wordChars('_', '_');
        t.wordChars('-', '-');
        t.wordChars(128, Integer.MAX_VALUE);
        t.whitespaceChars('\t', '\t');
        t.whitespaceChars(' ', ' ');
        t.whitespaceChars('\n', '\n');
        t.whitespaceChars('\r', '\r');
        return t;
    }

    @Override
    public State<Pair<Set<Color>, Map<String, Color>>> processChar(final Pair<Set<Color>, Map<String, Color>> context, final char token) {
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "sea_starts"));
    }

    @Override
    public State<Pair<Set<Color>, Map<String, Color>>> processWord(final Pair<Set<Color>, Map<String, Color>> context, final String word) {
        switch (word) {
            case "sea_starts":
                return seas;
            default:
                return ignore;
        }
    }
}
