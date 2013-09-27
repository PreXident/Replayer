package com.paradoxplaza.eu4.replayer.parser;

import java.io.InputStream;
import java.io.StreamTokenizer;


/**
 * Represents a starting state of a {@link TextParser}
 *
 * @param <Context> The context of the start node
 */
public abstract class Start<Context>
		extends State<Context> {
	
	/**
	 * Start states have no parent states, so call them without a parameter
	 */
	public Start() {
		super(null);
	}
	
	/**
     * Creates tokenizer used by TextParser. Quote character is always '"'.
     * @param input InputStream to parse
     * @return new StreamTokenizer
     */
    abstract StreamTokenizer createTokenizer(final InputStream input);
}
