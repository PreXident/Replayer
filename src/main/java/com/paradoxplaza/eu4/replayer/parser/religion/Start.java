package com.paradoxplaza.eu4.replayer.parser.religion;

import com.paradoxplaza.eu4.replayer.parser.State;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Map;

/**
 * Start state for ReligionParser.
 */
class Start extends com.paradoxplaza.eu4.replayer.parser.Start<Map<String, Integer>> {

    /** State processing religion groups. */
    final ReligionGroup religionGroup = new ReligionGroup(this);

    /**
     * Only contructor.
     */
    public Start() {
        super(null);
    }

    @Override
    public Start end(final Map<String, Integer> context) {
        return this;
    }

    @Override
    protected StreamTokenizer _createTokenizer(final InputStream input) {
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
    public State<Map<String, Integer>> processChar(final Map<String, Integer> context, final char token) {
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, token, "RELIGION_NAME"));
    }

    @Override
    public State<Map<String, Integer>> processWord(final Map<String, Integer> context, final String word) {
        return religionGroup.withName(word);
    }
}
