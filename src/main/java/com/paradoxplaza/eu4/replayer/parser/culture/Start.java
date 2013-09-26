package com.paradoxplaza.eu4.replayer.parser.culture;

import com.paradoxplaza.eu4.replayer.CountryInfo;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.utils.Pair;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Map;

/**
 * Start state for CulturesParser.
 */
class Start extends com.paradoxplaza.eu4.replayer.parser.Start<Pair<Map<String, CountryInfo>, Map<String, Integer>>> {

    /** State processing culture groups. */
    final CultureGroup cultureGroup = new CultureGroup(this);

    /**
     * Only contructor.
     */
    public Start() {
        super(null);
    }

    @Override
    public Start end(final Pair<Map<String, CountryInfo>, Map<String, Integer>> context) {
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
    public State<Pair<Map<String, CountryInfo>, Map<String, Integer>>> processChar(final Pair<Map<String, CountryInfo>, Map<String, Integer>> context, final char token) {
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_VALUE, token, "RELIGION_NAME"));
    }

    @Override
    public State<Pair<Map<String, CountryInfo>, Map<String, Integer>>> processWord(final Pair<Map<String, CountryInfo>, Map<String, Integer>> context, final String word) {
        return cultureGroup.withName(word);
    }
}
