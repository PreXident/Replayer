package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

import java.util.EnumSet;

/**
 * Flags for tokens in binary save stream.
 * Intended mostly for preferred output.
 */
public enum Flag {
    /** Output should be date. */
    DATE,
    /** Output should be quoted date. */
    QUOTED_DATE,
    /** Output should be string. */
    STRING,
    /** Output should be quoted string. */
    QUOTED_STRING,
    /** Empty strings shoudl be replaced by "---". */
    EMPTY_STRING_DASH,
    /** Output should be integral. */
    INTEGER,
    /** Output should be real number with three decimal places. */
    FLOAT,
    /** Output should be real number with five decimal places. */
    FLOAT5,
    /** Output should be integral number equals real number with three decimals. */
    INTEQFLOAT,
    /** Output should be integral number equals date. */
    INTEQDATE,
    /** Empty list, do not print close braces. */
    EMPTY_LIST,
    /** Use pretty indenting even for usually not indented lists. */
    PRETTY_LIST,
    /** Unknown token, relaxed constraints. */
    UNKNOWN;

    /**
     * Parses string into enum set.
     * @param string comma separated abbreviations of flags
     * @return enum set created from string
     */
    public static EnumSet<Flag> parse(final String string) {
        final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        final String[] parts = string.split(",");
        for (String flag : parts) {
            switch (flag) {
                case "D":
                    flags.add(DATE);
                    break;
                case "QD":
                    flags.add(QUOTED_DATE);
                    break;
                case "S":
                    flags.add(STRING);
                    break;
                case "QS":
                    flags.add(QUOTED_STRING);
                    break;
                case "E":
                    flags.add(EMPTY_STRING_DASH);
                    break;
                case "I":
                    flags.add(INTEGER);
                    break;
                case "F":
                    flags.add(FLOAT);
                    break;
                case "F5":
                    flags.add(FLOAT5);
                    break;
                case "IF":
                    flags.add(INTEQFLOAT);
                    break;
                case "ID":
                    flags.add(INTEQFLOAT);
                    break;
                case "P":
                    flags.add(PRETTY_LIST);
                    break;
                case "":
                    break;
                default:
                    System.err.println("Unknown flag: " + flag);
            }
        }
        return flags;
    }
}
