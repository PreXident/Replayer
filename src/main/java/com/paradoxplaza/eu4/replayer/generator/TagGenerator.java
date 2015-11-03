package com.paradoxplaza.eu4.replayer.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Generates valid country tags.
 */
public class TagGenerator {

    /** Three letter strings not suitable for country tags. */
    static final Set<String> forbiddenTags = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("HRE", "AND", "NOT", "AUX", "NAT", "PIR", "REB", "CON")));

    /** Lastly returned tag. */
    String lastTag = "AA@";

    /** Buffer for creating tags. */
    final char[] buffer = lastTag.toCharArray();

    /**
     * Returns next country tag.
     * @return next country tag
     */
    public String next() {
        if (lastTag.equals("ZZZ")) {
            throw new NoSuchElementException("No other tags available!");
        }
        do {
            int i = buffer.length - 1;
            boolean up;
            do {
                up = false;
                int n = buffer[i] + 1;
                if (n > 'Z') {
                    n = 'A';
                    up = true;
                }
                buffer[i] = (char) n;
                --i;
            } while (up);
            lastTag = new String(buffer);
        } while (forbiddenTags.contains(lastTag));
        return lastTag;
    }
}
