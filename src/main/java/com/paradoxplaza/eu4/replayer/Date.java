package com.paradoxplaza.eu4.replayer;

import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;

/**
 * Represents one date.
 */
public class Date implements Comparable<Date>, java.io.Serializable {

    /** Enum representing different time periods. */
    public enum Period { DAY, MONTH, YEAR }

    /** Number of days in each month ignoring leap years. */
    static public byte[] monthsDays = new byte[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    /** Number of days in a year. */
    static short yearDays = 365;

    /** Period representing day. */
    static public final Period DAY = Period.DAY;

    /** Period representing month. */
    static public final Period MONTH = Period.MONTH;

    /** Period representing year. */
    static public final Period YEAR = Period.YEAR;

    /**
     * Throws InvalidArgumentException if given date is invalid.
     * @param year
     * @param month
     * @param day
     * @throws IllegalArgumentException if date is invalid
     */
    static void checkDate(short year, byte month, byte day) {
        if (month < 1 || month > 12 || day < 1 || day > monthsDays[month-1]) {
            throw new IllegalArgumentException(String.format(l10n("date.invalid.3"), year, month, day));
        }
    }

    /**
     * Returns number of days between lower and upper date.
     * @param low lower date
     * @param up upper date
     * @return number of days between lower and upper date
     */
    static public int calculateDistance(Date low, Date up) {
        if (low.compareTo(up) > 0) {
            final Date swap = low;
            low = up;
            up = swap;
        }
        final int years = up.year - low.year;
        int lowDays = low.day;
        for (int i = 0; i < low.month - 1; ++i) {
            lowDays += Date.monthsDays[i];
        }
        int upDays = up.day;
        for (int i = 0; i < up.month - 1; ++i) {
            upDays += Date.monthsDays[i];
        }
        return years * Date.yearDays + upDays - lowDays;
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
            throw new IllegalArgumentException(String.format(l10n("date.invalid.1"), date));
        }
        try {
            year = Short.parseShort(s[0]);
            month = Byte.parseByte(s[1]);
            day = Byte.parseByte(s[2]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException(String.format(l10n("date.invalid.1"), date), e);
        }
        checkDate(year, month, day);
    }

    @Override
    public int compareTo(final Date o) {
        if (o == null) {
            return 1;
        }
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
        return this.day == other.day;
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
     * Returns whether the date is between low and high dates inclusive.
     * @param low bottom boundary
     * @param high upper boundary
     * @return true if the date is between low and high
     */
    public boolean isBetween(final Date low, final Date high) {
        return compareTo(low) >= 0 && compareTo(high) <= 0;
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
            if (m > 12) {
                m = 1;
                ++y;
            }
        }
        return new Date(y, m, d);
    }

    /**
     * Returns Date with distance delta units.
     * @param period delta units
     * @param delta how many units to skip
     * @return Date with distance delta units
     */
    public Date skip(final Period period, final int delta) {
        if (delta > 0) {
            return skipForward(period, delta);
        } else {
            return skipBackward(period, -delta);
        }
    }

    /**
     * Returns Date in the past.
     * @param period delta units
     * @param delta how many units to skip
     * @return Date in the past
     */
    private Date skipBackward(final Period period, final int delta) {
        int d = day;
        int m = month;
        int y = year;

        switch (period) {
            case DAY:
                d -= delta;
                while (d < 1) {
                    d += monthsDays[m-1];
                    --m;
                    if (m < 1) {
                        m = 12;
                        --y;
                    }
                }
                break;
            case MONTH:
                m -= delta + 1;
                y += m / 12;
                m = m % 12;
                ++m;
                if (m < 1) {
                    m = 12 + m;
                    --y;
                }
                if (d > monthsDays[m-1]) {
                    d = monthsDays[m-1];
                }
                break;
            case YEAR:
                y -= delta;
                break;
        }

        return new Date((short) y, (byte) m, (byte) d);
    }

    /**
     * Returns future Date.
     * @param period delta units
     * @param delta how many units to skip
     * @return future Date
     */
    private Date skipForward(final Period period, final int delta) {
        int d = day;
        int m = month;
        int y = year;

        switch (period) {
            case DAY:
                d += delta;
                while (d > monthsDays[m-1]) {
                    d -= monthsDays[m-1];
                    ++m;
                    if (m > 12) {
                        m = 1;
                        ++y;
                    }
                }
                break;
            case MONTH:
                m += delta - 1;
                y += m / 12;
                m = m % 12;
                ++m;
                if (d > monthsDays[m-1]) {
                    d = monthsDays[m-1];
                }
                break;
            case YEAR:
                y += delta;
                break;
        }

        return new Date((short) y, (byte) m, (byte) d);
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
