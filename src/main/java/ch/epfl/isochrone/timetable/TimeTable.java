package ch.epfl.isochrone.timetable;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * A timetable with stops and services.
 *
 * @author Jakob Bauer (223590)
 */
public final class TimeTable {

    private final Set<Stop> stops;
    private final Collection<Service> services;

    /**
     * Builder class for the Timetable class.
     *
     * @author Jakob Bauer (223590)
     */
    public final static class Builder {

        private final Set<Stop> stops;
        private final Collection<Service> services;

        /**
         * Class constructor.
         *
         */
        public Builder() {
            this.stops = new HashSet<Stop>();
            this.services = new HashSet<Service>();
        }

        /**
         * Adds a new stop to the stops.
         *
         * @param newStop   the stop to be added.
         * @return          the builder with the new stop included.
         */
        public Builder addStop(Stop newStop) {
            stops.add(newStop);
            return this;
        }

        /**
         * Adds a new service to the services.
         *
         * @param newService    the service to be added.
         * @return              the builder with the new service included.
         */
        public Builder addService(Service newService) {
            services.add(newService);
            return this;
        }

        /**
         * Builds a timetable with the information stored in the builder.
         *
         * @return  a timetable.
         */
        public TimeTable build() {
            return new TimeTable(stops, services);
        }
    }

    /**
     * Class constructor.
     *
     * @param stops     the stops of the timetable.
     * @param services  the services of the timetable.
     */
    public TimeTable(Set<Stop> stops, Collection<Service> services) {
        this.stops = java.util.Collections.unmodifiableSet(new HashSet<Stop>(stops));
        this.services = java.util.Collections.unmodifiableSet(new HashSet<Service>(services));
    }

    /**
     * Returns the stops of the timetable.
     *
     * @return  the stops of the timetable.
     */
    public Set<Stop> stops() {
        return stops;
    }

    /**
     * Returns the services on the given date.
     *
     * @param date  the date for which the active services shall be returned.
     * @return      the services active on the given date.
     */
    public Set<Service> servicesForDate(Date date) {
        Set<Service> activeServices = new HashSet<Service>();
        for (Service i : services) {
            if (i.isOperatingOn(date)) {
                activeServices.add(i);
            }
        }
        return java.util.Collections.unmodifiableSet(activeServices);
    }
}
