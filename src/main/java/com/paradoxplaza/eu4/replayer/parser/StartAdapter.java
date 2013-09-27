package com.paradoxplaza.eu4.replayer.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * Default implementation of {@link Start} providing a {@link StreamTokenizer} applicable for EU4 save games
 */
public class StartAdapter<Context>
		extends Start<Context> {
	
	/**
	 * Creates default tokenizer for EU4 save games, used by TextParser.
	 * 
	 * @param input InputStream to parse
	 * @return new StreamTokenizer
	 */
	@Override
	protected StreamTokenizer createTokenizer(final InputStream input) {
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
		t.quoteChar('"');
		
		return t;
	}
}
