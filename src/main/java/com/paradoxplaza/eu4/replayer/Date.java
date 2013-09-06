package com.paradoxplaza.eu4.replayer;

/**
 * Represents one date.
 */
public class Date implements Comparable<Date> {

    /** Number of days in each month ignoring leap years. */
    static byte[] monthsDays = new byte[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    /** Number of days in a year. */
    static short yearDays = 365;

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
    final short year;

    /** Month of this date. */
    final byte month;

    /** Day of this date. */
    final byte day;

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
        //this.setDate(date);
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
    public int compareTo(Date o) {
        if (year != o.year) {
            return year < o.year ? -1 : 1;
        }
        if (month != o.month) {
            return month < o.month ? -1 : 1;
        }
        if (day != o.day) {
            return day < o.day ? -1 : 1;
        }
        return 0;
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

    /**
     * Returns next date.
     * @return next date
     */
    public Date next() {
        byte d = day;
        byte m = month;
        short y = year;
        ++d;
        if (d > monthsDays[m-1]) {
            d = 1;
            ++m;
            if (m / 12 > 0) {
                m = 1;
                ++y;
            }
        }
        return new Date(y, m, d);
    }

    /**
     * Returns previous date.
     * @return previous date
     */
    public Date prev() {
        byte d = day;
        byte m = month;
        short y = year;
        --d;
        if (d < 1) {
            --m;
            if (m < 1) {
                m = 12;
                --y;
            }
            d = monthsDays[m-1];
        }
        return new Date(y, m, d);
    }

    @Override
    public String toString() {
        return year + "." + month + "." + day;
    }
}
