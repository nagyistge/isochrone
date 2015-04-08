package ch.epfl.isochrone.timetable;

import static ch.epfl.isochrone.math.Math.divF;
import static ch.epfl.isochrone.math.Math.modF;

/**
 * Representation of and calculation with dates.
 * The Gregorian calendar is used.
 *
 * @author Jakob Bauer (223590)
 */
public final class Date implements Comparable<Date> {

    /**
    * Enumeration of the days of week from MONDAY to SUNDAY.
    *
    * @author Jakob Bauer (223590)
    */
    public enum DayOfWeek { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY,
                            SATURDAY, SUNDAY }

    /**
    * Enumeration of the months from JANUARY to DECEMBER.
    *
    * @author Jakob Bauer (223590)
    */
    public enum Month { JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY,
                        AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER }

    private final int day;
    private final Month month;
    private final int year;

    /**
     * Class constructor
     *
     * @param day   the day of month, from 1 (first day of month) to at most 31.
     * @param month the month, represented as a Month.
     * @param year  the year according to the Gregorian calendar.
     * @throws IllegalArgumentException if the day is negative or if it is
     *              bigger than the number of days of the given month.
     */
    public Date(int day, Month month, int year) throws IllegalArgumentException {
        if ((day < 1) || (day > daysInMonth(month, year))) {
            throw new IllegalArgumentException("Illegal value for day: " + day);
        }
        this.day = day;
        this.month = month;
        this.year = year;
    }

    /**
     * Class constructor
     *
     * @param day   the day of month, from 1 (first day of month) to at most 31.
     * @param month the month, represented as an int.
     * @param year  the year according to the Gregorian calendar.
     * @throws IllegalArgumentException if the day is negative of if it is
     *              bigger than the number of days of the given month.
     */
    public Date(int day, int month, int year) throws IllegalArgumentException {
        this(day, intToMonth(month), year);
    }

    /**
     * Class constructor
     *
     * @param date  the date in the java.util.Date format.
     */
    @SuppressWarnings("deprecation")
    public Date(java.util.Date date) {
        this(date.getDate(), intToMonth(date.getMonth() + 1),
             date.getYear() + 1900);
    }

    /**
     * Returns the day.
     *
     * @return  the day of month, from 1 (first day of month) to at most 31.
     */
    public int day() { return day; }

    /**
     * Returns the month.
     *
     * @return  the month in the Month format.
     */
    public Month month() { return month; }

    /**
     * Returns the month.
     *
     * @return  the month as an int ranging from 1 (JANUARY) to 12 (DECEMBER).
     */
    public int intMonth() { return monthToInt(month); }

    /**
     * Returns the year.
     *
     * @return  the year according to the Gregorian calendar.
     */
    public int year() { return year; }

    /**
     * Returns the day of week.
     *
     * @return  the day of week according to the Gregorian calendar.
     */
    public DayOfWeek dayOfWeek() {
        int d = modF(fixed(), 7);
        int dConv = (d == 0) ? 7 : d;
        return intToDay(dConv);
    }

    /**
     * Returns the date that has the indicated difference with this date.
     *
     * @param daysDiff  the difference of days between this date and the date
     *                  to be returned (e.g., -1 will return the day before
     *                  this day, 1 will return the next day).
     * @return          the new date.
     */
    public Date relative(int daysDiff) {
        return fixedToDate(fixed() + daysDiff);
    }

    /**
     * Converts this date to the java.util.Date format.
     *
     * @return  this date in the java.util.date format.
     */
    @SuppressWarnings("deprecation")
    public java.util.Date toJavaDate() {
        return new java.util.Date(year - 1900, monthToInt(month) - 1, day);
    }

    /**
     * Returns a string of the date in the format (YYYY-MM-DD)
     *
     * @return a string of the date in the format (YYYY-MM-DD)
     */
    @Override
    public String toString() {
        int m = intMonth();
        String mString =
            (m < 10) ? "0" + Integer.toString(m) : Integer.toString(m);
        String dString =
            (day < 10) ? "0" + Integer.toString(day) : Integer.toString(day);
        return Integer.toString(year) + "-" + mString + "-" + dString;
    }

    /**
     * Compares this date to that Object; returns true if and only if
     * that object is a date representing the same date as this.
     *
     * @param that the object with which this is to be compared.
     * @return true if that object represents the same date as this,
     *         false otherwise.
     */
    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        } else if (that.getClass() != getClass()) {
            return false;
        } else {
            return (fixed() == ((Date) that).fixed());
        }
    }

    /**
     * Returns the integer corresponding to this date.
     *
     * @return the integer corresponding to this date.
     */
    @Override
    public int hashCode() { return fixed(); }

    /**
     * Compares this date with another date.
     *
     * @param that  the date to which this date will be compared to.
     * @return      -1 if this date is strictly smaller to that date,
     *              0 if this date is equal to that date, and
     *              1 if this date is strictly greater than that date.
     */
    @Override
    public int compareTo(Date that) {
        return Integer.signum(fixed() - that.fixed());
    }

    private static DayOfWeek intToDay(int d) throws IllegalArgumentException {
        if ((d < 1) || (d > 7)) {
            throw new IllegalArgumentException("d is not in the interval [1;7]");
        }
        return DayOfWeek.values()[d-1];
    }

    private static Month intToMonth(int m) throws IllegalArgumentException {
        if ((m < 1) || (m > 12)) {
            throw new IllegalArgumentException("m is not in the interval [1;12]");
        }
        return Month.values()[m-1];
    }

    private static int monthToInt(Month m) throws NullPointerException {
        if (m == null) {
            throw new NullPointerException();
        }
        return m.ordinal() + 1;
    }

    private static boolean isLeapYear(int y) {
        return ((modF(y, 4) == 0) && (modF(y, 100) != 0)) || (modF(y, 400) == 0);
    }

    private static int daysInMonth(Month m, int y) {
        if (m == null) {
            throw new IllegalArgumentException();
        }
        switch(m) {
            case JANUARY:
            case MARCH:
            case MAY:
            case JULY:
            case AUGUST:
            case OCTOBER:
            case DECEMBER:
                return 31;
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            case FEBRUARY:
                return isLeapYear(y) ? 29 : 28;
            default:
                throw new Error();
        }
    }

    private static int dateToFixed(int d, Month m, int y) {
        int y0 = y - 1;
        int c;
        boolean l = isLeapYear(y);
        int mm = monthToInt(m);

        if (mm <= 2) {
            c = 0;
        } else if ((mm > 2) && l) {
            c = -1;
        } else {
            c = -2;
        }

        return (365 * y0 + divF(y0, 4) - divF(y0, 100) + divF(y0, 400) +
                divF(367 * mm - 362, 12) + c + d);
    }

    private static Date fixedToDate(int n) {
        int d0 = n - 1;
        int n400 = divF(d0, 146097);
        int d1 = modF(d0, 146097);
        int n100 = divF(d1, 36524);
        int d2 = modF(d1, 36524);
        int n4 = divF(d2, 1461);
        int d3 = modF(d2, 1461);
        int n1 = divF(d3, 365);
        int y0 = 400 * n400 + 100 * n100 + 4 * n4 + n1;
        int y = ((n100 == 4) || (n1 == 4)) ? y0 : y0 + 1;

        int p = n - dateToFixed(1, intToMonth(1), y);
        boolean l = ((modF(y, 4) == 0) && (modF(y, 100) != 0))
                    || (modF(y, 400) == 0);
        int c;
        if (n < dateToFixed(1, intToMonth(3), y)) {
            c = 0;
        } else if ((n >= dateToFixed(1, intToMonth(3), y)) && l) {
            c = 1;
        } else {
            c = 2;
        }
        int m = divF(12 * (p + c) + 373, 367);

        int d = n - dateToFixed(1, intToMonth(m), y) + 1;

        return new Date(d, m, y);
    }

    private int fixed() { return dateToFixed(day, month, year); }
}
