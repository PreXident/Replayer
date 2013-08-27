package com.paradoxplaza.eu4.replayer;

/**
 * Represents one date.
 */
public class Date {

    /** Number of days in each month ignoring leap years. */
    static int[] monthsDays = new int[] { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    /**
     * Throws InvalidArgumentException if given date is invalid.
     * @param year
     * @param month
     * @param day
     * @throws IllegalArgumentException if date is invalid
     */
    static void checkDate(short year, byte month, byte day) {
        if (year < 1 || month < 1 || month > 12 || day < 1 || day > monthsDays[month-1]) {
            throw new IllegalArgumentException(String.format("Invalid date: %1$s.%2$s.%3$s", year, month, day));
        }
    }

    /** Year of this date. */
    short year;

    /** Month of this date. */
    byte month;

    /** Day of this date. */
    byte day;

    /**
     * Contructs new Date 1.1.1.
     */
    public Date() {
        year = 1;
        month = 1;
        day = 1;
    }

    /**
     * Constructs Date from given year, month and day.
     * @param year
     * @param month
     * @param day
     * @throws IllegalArgumentException if date is invalid
     */
    public Date(final short year, final byte month, final byte day) {
        checkDate(year, month, day);
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * Construct Date from given string.
     * @param date date in format "Y.M.D"
     * @throws IllegalArgumentException if date is invalid
     */
    public Date(final String date) {
        this.setDate(date);
    }

    /**
     * Sets the date for this Date.
     * @param date date to set
     */
    public final void setDate(final String date) {
        final String[] s = date.split("\\.");
        if (s.length != 3) {
            throw new IllegalArgumentException(String.format("Invalid date: %1$s", date));
        }
        try {
            year = Short.parseShort(s[0]);
            month = Byte.parseByte(s[1]);
            day = Byte.parseByte(s[2]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid date: %1$s", date), e);
        }
        checkDate(year, month, day);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Date other = (Date) obj;
        if (this.year != other.year) {
            return false;
        }
        if (this.month != other.month) {
            return false;
        }
        if (this.day != other.day) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.year;
        hash = 47 * hash + this.month;
        hash = 47 * hash + this.day;
        return hash;
    }
}
