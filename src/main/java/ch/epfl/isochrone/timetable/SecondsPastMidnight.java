package ch.epfl.isochrone.timetable;

import static ch.epfl.isochrone.math.Math.divF;

/**
 * Contains methods to deal with hours past midnight that are represented
 * as integers.
 *
 * @author Jakob Bauer (223590)
 */
public final class SecondsPastMidnight {

    /**
     * Contains a number of seconds past midnight that is guaranteed to be
     * bigger than any valid number of seconds past midnight.
     */
    public static final int INFINITE = 200000;

    private static final int MAX_SECONDS = 107999;

    private SecondsPastMidnight() {};

    /**
     * Converts a triplet of (hours, minutes, seconds) into the number of
     * seconds past midnight.
     *
     * @param hours     the number of hours.
     * @param minutes   the number of minutes.
     * @param seconds   the number of seconds.
     * @return          the number of seconds past midnight.
     * @throws IllegalArgumentException if seconds or minutes are outside the
     *                  interval [0;60[ or if the number of hours is outside
     *                  the interval [0;30[.
     */
    public static int fromHMS(int hours, int minutes, int seconds)
            throws IllegalArgumentException {
        if ((seconds < 0) || (seconds >= 60)) {
            throw new IllegalArgumentException("seconds not in interval [0;60[");
        }
        if ((minutes < 0) || (minutes >= 60)) {
            throw new IllegalArgumentException("minutes not in interval [0;60[");
        }
        if ((hours < 0) || (hours >= 30)) {
            throw new IllegalArgumentException("hours not in interval [0;30[");
        }
        return (hours * 3600 + minutes * 60 + seconds);
    }

    /**
     * Converts the number of hours of a java.util.Date into the number of
     * seconds past midnight.
     *
     * @param date  the date in the java.util.Date format.
     * @return      the number of seconds past midnight.
     */
    @SuppressWarnings("deprecation")
    public static int fromJavaDate(java.util.Date date) {
        return (date.getHours() * 3600 + date.getMinutes() * 60
                + date.getSeconds());
    }

    /**
     * Returns the number of full hours of the number of seconds past midnight
     * that are passed as the argument.
     *
     * @param spm   the number of seconds past midnight.
     * @return      the number of full hours of the number of seconds past
     *              midnight.
     * @throws IllegalArgumentException if the number of seconds past midnight
     *              is not in the interval [0,MAX_SECONDS], where MAX_SECONDS
     *              is equivalent to 29 h 59 min 59 s.
     */
    public static int hours(int spm) throws IllegalArgumentException {
        if ((spm < 0) || (spm > MAX_SECONDS)) {
            throw new IllegalArgumentException(
                    "spm not in interval [0,MAX_SECONDS]");
        }
        return divF(spm, 3600);
    }

    /**
     * Returns the number of full minutes of the number of seconds past midnight
     * after all full hours have been subtracted.
     *
     * @param spm   the number of seconds past midnight.
     * @return      the number of full minutes of the numbers of seconds past
     *              midnight after all the full hours have been subtracted.
     * @throws IllegalArgumentException if the number of seconds past midnight
     *              is not in the interval [0,MAX_SECONDS], where MAX_SECONDS
     *              is equivalent to 29 h 59 min 59 s.
     */
    public static int minutes(int spm) throws IllegalArgumentException {
        if ((spm < 0) || (spm > MAX_SECONDS)) {
            throw new IllegalArgumentException(
                    "spm not in interval [0,MAX_SECONDS]");
        }
        return divF(spm - hours(spm) * 3600, 60);
    }

    /**
     * Returns the number of seconds of the number of seconds past midnight
     * after all full hours and full minutes have been subtracted.
     *
     * @param spm   the number of seconds past midnight.
     * @return      the number of full minutes of the numbers of seconds past
     *              midnight after all the full hours and full minutes have
     *              been subtracted.
     * @throws IllegalArgumentException if the number of seconds past midnight
     *              is not in the interval [0,MAX_SECONDS], where MAX_SECONDS
     *              is equivalent to 29 h 59 min 59 s.
     */
    public static int seconds(int spm) throws IllegalArgumentException {
        if ((spm < 0) || (spm > MAX_SECONDS)) {
            throw new IllegalArgumentException(
                    "spm not in interval [0,MAX_SECONDS]");
        }
        return (spm - hours(spm) * 3600 - minutes(spm) * 60);
    }

    /**
     * Returns a string of the seconds past midnight in the format hh:mm:ss.
     *
     * @param spm   the number of seconds past midnight.
     * @return      a string in the format hh:mm:ss.
     * @throws IllegalArgumentException if the number of seconds past midnight
     *              is not in the interval [0,MAX_SECONDS], where MAX_SECONDS
     *              is equivalent to 29 h 59 min 59 s.
     */
    public static String toString(int spm) throws IllegalArgumentException {
        if ((spm < 0) || (spm > MAX_SECONDS)) {
            throw new IllegalArgumentException(
                    "spm not in interval [0,MAX_SECONDS]");
        }
        return String.format("%02d:%02d:%02d",
                             hours(spm), minutes(spm), seconds(spm));
    }
}
