package ch.epfl.isochrone.timetable;

import java.util.Set;
import java.util.HashSet;

/**
 * A service with a name, starting and ending date, operating days, as well as
 * included and excluded dates.
 *
 * @author Jakob Bauer (223590)
 */
public final class Service {

    private final String name;
    private final Date startingDate;
    private final Date endingDate;
    private final Set<Date.DayOfWeek> operatingDays;
    private final Set<Date> excludedDates;
    private final Set<Date> includedDates;

    /**
     * Builder class for the Service class.
     *
     * @author Jakob Bauer (223590)
     */
    public static class Builder {

        private final String name;
        private final Date startingDate;
        private final Date endingDate;
        private final Set<Date.DayOfWeek> operatingDays;
        private final Set<Date> excludedDates;
        private final Set<Date> includedDates;

        /**
         * Class constructor.
         *
         * @param name          the name of the service.
         * @param startingDate  the starting date of the service.
         * @param endingDate    the ending date of the service.
         * @throws IllegalArgumentException if the ending date is earlier
         *                      than the starting date.
         */
        public Builder(String name, Date startingDate, Date endingDate)
                throws IllegalArgumentException {
            if (endingDate.compareTo(startingDate) < 0) {
                throw new IllegalArgumentException(
                        "Ending date is earlier than starting date");
            }
            this.name = name;
            this.startingDate = startingDate;
            this.endingDate = endingDate;
            this.operatingDays = new HashSet<Date.DayOfWeek>();
            this.excludedDates = new HashSet<Date>();
            this.includedDates = new HashSet<Date>();
        }

        /**
         * Returns the name of the service.
         *
         * @return the name of the service.
         */
        public String name() { return name; }

        /**
         * Adds an operating day to the builder.
         *
         * @param day   the day that is to be added to the operating days.
         * @return      the builder with the day added to its operating days.
         */
        public Builder addOperatingDay(Date.DayOfWeek day) {
            operatingDays.add(day);
            return this;
        }

        /**
         * Adds an excluded date to the builder.
         *
         * @param date  the date to be added to the excluded dates.
         * @return      the builder with the date added to the excluded dates.
         * @throws IllegalArgumentException if the excluded date is not in the
         *              service period or if the excluded date is already
         *              in the included dates.
         */
        public Builder addExcludedDate(Date date) throws IllegalArgumentException {
            if (! inRange(date, startingDate, endingDate)) {
                throw new IllegalArgumentException("Excluded date not in range");
            }
            if (includedDates.contains(date)) {
                throw new IllegalArgumentException(
                        "Excluded date is already in included dates");
            }
            excludedDates.add(date);
            return this;
        }

        /**
         * Adds an included date to the builder.
         *
         * @param date  the date to be added to the included dates.
         * @return      the builder with the date added to the excluded dates.
         * @throws IllegalArgumentException if the included date is not in the
         *              service period or if the included date is already in
         *              the excluded dates.
         */
        public Builder addIncludedDate(Date date) throws IllegalArgumentException {
            if (! inRange(date, startingDate, endingDate)) {
                throw new IllegalArgumentException("Included date not in range");
            }
            if (excludedDates.contains(date)) {
                throw new IllegalArgumentException(
                        "Included date is already in excluded dates");
            }
            includedDates.add(date);
            return this;
        }

        /**
         * Constructs a service from the builder.
         *
         * @return  a service with the name, service period, operating days,
         *          and exceptions as specified in the builder.
         */
        public Service build() {
            return new Service(name, startingDate, endingDate, operatingDays,
                               excludedDates, includedDates);
        }
    }

    /**
     * Class constructor.
     *
     * @param name          the name of the service.
     * @param startingDate  the starting date of the service period (included).
     * @param endingDate    the ending date of the service period (included).
     * @param operatingDays the operating days (e.g. monday, tuesday, friday).
     * @param excludedDates the dates that are exceptionally excluded from
     *                      the service.
     * @param includedDates the dates that are exceptionally included in
     *                      the service.
     * @throws IllegalArgumentException if the ending date is earlier than the
     *                      starting date, if the excluded or included dates
     *                      are outside the service period or if the
     *                      intersection of included and excluded dates is
     *                      non-empty.
     */
    public Service(String name, Date startingDate, Date endingDate,
                   Set<Date.DayOfWeek> operatingDays, Set<Date> excludedDates,
                   Set<Date> includedDates) throws IllegalArgumentException {
        if (endingDate.compareTo(startingDate) < 0) {
            throw new IllegalArgumentException(
                    "Ending date is earlier than starting date");
        }
        for (Date i : excludedDates) {
            if (! inRange(i, startingDate, endingDate)) {
                throw new IllegalArgumentException(
                    "Excluded date not in range");
            }
        }
        for (Date i : includedDates) {
            if (! inRange(i, startingDate, endingDate)) {
                throw new IllegalArgumentException(
                    "Included date not in range");
            }
        }
        if (excludedDates.removeAll(includedDates)) {
            throw new IllegalArgumentException(
                    "Intersection of included and excluded dates is nonempty");
        }

        this.name = name;
        this.startingDate = startingDate;
        this.endingDate = endingDate;
        this.operatingDays = new HashSet<Date.DayOfWeek>(operatingDays);
        this.excludedDates = new HashSet<Date>(excludedDates);
        this.includedDates = new HashSet<Date>(includedDates);
    }

    /**
     * Returns the name of the service.
     *
     * @return  the name of the service.
     */
    public String name() { return name; }

    /**
     * Returns true if the service is operating on the given date, which
     * is the case if a) the date is included in the service period, falls on
     * an operating day, and is not an excluded date or b) if the date is
     * an included date.
     *
     * @param date  the date to be tested.
     * @return      true if the service is operating on the date and false
     *              otherwise.
     */
    public boolean isOperatingOn(Date date) {
        return ((inRange(date, startingDate, endingDate)
                    && operatingDays.contains(date.dayOfWeek())
                    && (! excludedDates.contains(date)))
                    || includedDates.contains(date));
    }

    /**
     * Returns the name of the service.
     *
     * @return the name of the service.
     */
    @Override
    public String toString() { return name; }

    private static boolean inRange(Date test, Date start, Date end) throws IllegalArgumentException {
        if (end.compareTo(start) < 0) {
            throw new IllegalArgumentException("End date before start date");
        }
        return ((test.compareTo(start) >= 0) && (test.compareTo(end) <= 0));
    }
}
