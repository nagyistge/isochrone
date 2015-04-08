package ch.epfl.isochrone.timetable;

import java.util.Set;
import java.util.HashSet;
import static java.util.Arrays.sort;
import static java.util.Arrays.binarySearch;

import static ch.epfl.isochrone.math.Math.divF;
import static ch.epfl.isochrone.math.Math.modF;

/**
 * Represents an annotated edge in the transportation schedule graph.
 *
 * @author Jakob Bauer (223590)
 */
final class GraphEdge {

    private static final int MAX_SECONDS = 107999;
    private static final int MAX_JOURNEY = 9999;
    private static final int SHIFT = 10000;

    private final Stop destination;
    private final int walkingTime;
    private final int[] packedTrips;

    /**
     * Builder class for the Edge class.
     *
     * @author Jakob Bauer (223590)
     */
    public static class Builder {

        private final Stop destination;
        private int walkingTime;
        private final Set<Integer> packedTrips;

        /**
         * Class constructor.
         *
         * @param destination   destination station of the edge.
         */
        public Builder(Stop destination) {
            this.destination = destination;
            this.walkingTime = -1;
            this.packedTrips = new HashSet<Integer>();
        }

        /**
         * Sets the walking time.
         *
         * @param newWalkingTime    the new walking time.
         * @return                  the builder with the updated walking time.
         * @throws IllegalArgumentException if the new walking time is less
         *                          less than -1.
         */
        public GraphEdge.Builder setWalkingTime(int newWalkingTime)
                throws IllegalArgumentException {
            if (newWalkingTime < -1) {
                throw new IllegalArgumentException("Invalid new walking time");
            }
            this.walkingTime = newWalkingTime;
            return this;
        }

        /**
         * Adds a trip to the builder.
         *
         * @param departureTime     departure time of the new trip.
         * @param arrivalTime       arrival time of the new trip.
         * @return                  the builder with the added trip.
         * @throws IllegalArgumentException if the departure or the arrival
         *                          time are invalid.
         */
        public GraphEdge.Builder addTrip(int departureTime, int arrivalTime)
                throws IllegalArgumentException {
            packedTrips.add(packTrip(departureTime, arrivalTime));
            return this;
        }

        /**
         * Constructs and Edge from the builder.
         *
         * @return  an Edge with the destination, walking time and trips
         *          stored in the builder.
         */
        public GraphEdge build() {
            return new GraphEdge(destination, walkingTime, packedTrips);
        }
    }

    /**
     * Class constructor.
     *
     * @param destination   destination station of the edge.
     * @param walkingTime   the time it takes to walk to the destination.
     * @param packedTrips   the trips available to the destination.
     * @throws IllegalArgumentException if the walking time is less than -1
     *                      or if the departure time is not in the range
     *                      [0,107999] or if the duration of the journey
     *                      is not in the range [0,9999].
     */
    public GraphEdge(Stop destination, int walkingTime, Set<Integer> packedTrips)
            throws IllegalArgumentException {
        if (walkingTime < -1) {
            throw new IllegalArgumentException("Invalid walking time");
        }

        this.destination = destination;
        this.walkingTime = walkingTime;
        // immutability is guaranteed by the method convertIntegerToInt()
        this.packedTrips = convertIntegerToInt(packedTrips);
        sort(this.packedTrips);
    }

    private static int[] convertIntegerToInt(Set<Integer> integerSet) {
        int[] intSet = new int[integerSet.size()];
        int i = 0;
        for (Integer j : integerSet) {
            intSet[i] = j.intValue();
            i++;
        }
        return intSet;
    }

    /**
     * Returns the trip in packed form, i.e. as a single integer
     * representing both the departure time and the duration of the journey.
     *
     * @param departureTime the departure time.
     * @param arrivalTime   the arrival time.
     * @return              an integer representing both the departure time
     *                      and the duration of the journey.
     * @throws IllegalArgumentException if the departure time  is not in the
     *                      range [0,107999] or if the duration of the journey
     *                      is not in the range [0,9999].
     */
    public static int packTrip(int departureTime, int arrivalTime)
            throws IllegalArgumentException {
        if ((departureTime < 0) || (departureTime > MAX_SECONDS)) {
            throw new IllegalArgumentException(
                    "Invalid departure time: " + departureTime);
        }

        int journeyDuration = arrivalTime - departureTime;
        if ((journeyDuration < 0) || (journeyDuration > MAX_JOURNEY)) {
            throw new IllegalArgumentException(
                    "Invalid journey duration: " + journeyDuration);
        }

        return (departureTime * SHIFT + journeyDuration);
    }

    /**
     * Extracts the departure time from a packed trip.
     *
     * @param packedTrip    the packed trip from which to extract the
     *                      departure time.
     * @return              the departure time in SPM format.
     */
    public static int unpackTripDepartureTime(int packedTrip) {
        return divF(packedTrip, SHIFT);
    }

    /**
     * Extracts the trip duration from a packed trip.
     *
     * @param packedTrip    the packed trip from which to extract the
     *                      trip duration.
     * @return              the trip duration in seconds.
     */
    public static int unpackTripDuration(int packedTrip) {
        return modF(packedTrip, SHIFT);
    }

    /**
     * Extracts the arrival time from a packed trip.
     *
     * @param packedTrip    the packed trip from which to extract the
     *                      arrival time.
     * @return              the arrival time in SPM format.
     */
    public static int unpackTripArrivalTime(int packedTrip) {
        return unpackTripDepartureTime(packedTrip) +
            unpackTripDuration(packedTrip);
    }

    /**
     * Returns the destination of the edge.
     *
     * @return  the destination of the edge.
     */
    public Stop destination() { return destination; }

    /**
     * Returns the earliest arrival time at the destination for a given
     * departure time, taking into account all possible trips as well
     * as the walking time.
     *
     * @param departureTime the departure time.
     * @return              the earliest arrival time in SPM format.
     *                      If it is not possible to reach the destination
     *                      at the given departure time, then the value
     *                      SecondsPastMidnight.INFINITE is returned.
     */
    public int earliestArrivalTime(int departureTime) {
        int arrivalTime = SecondsPastMidnight.INFINITE;
        if (walkingTime > -1) {
            arrivalTime = departureTime + walkingTime;
        }

        if (packedTrips.length > 0) {
            int departureTimePacked = packTrip(departureTime, departureTime);
            int i = binarySearch(packedTrips, departureTimePacked);
            i = (i >= 0) ? i : (-i - 1);

            int j = packedTrips.length;
            int arrivalTimePacked;
            if (arrivalTime < MAX_SECONDS) {
                arrivalTimePacked = packTrip(arrivalTime, arrivalTime);
            } else {
                arrivalTimePacked = packTrip(MAX_SECONDS, MAX_SECONDS);
            }
            j = binarySearch(packedTrips, arrivalTimePacked);
            j = (j >= 0) ? j : (-j - 1);

            for (int k = i ; k < j ; k++) {
                int temp = unpackTripArrivalTime(packedTrips[k]);
                if (temp < arrivalTime) {
                    arrivalTime = temp;
                }
            }
        }

        return (arrivalTime < SecondsPastMidnight.INFINITE) ? 
                arrivalTime : SecondsPastMidnight.INFINITE;
    }
}
