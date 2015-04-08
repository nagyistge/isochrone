package ch.epfl.isochrone.timetable;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

/**
 * Represents a fastest paths three.
 *
 * @author Jakob Bauer (223590)
 */
public final class FastestPathTree {

    private final Stop startingStop;
    private final Map<Stop, Integer> arrivalTime;
    private final Map<Stop, Stop> predecessor;

    /**
     * Builder class for the FastestPathTree class.
     *
     * @author Jakob Bauer (223590)
     */
    public static final class Builder {

        private final Stop startingStop;
        private final int startingTime;
        private final Map<Stop, Integer> arrivalTime;
        private final Map<Stop, Stop> predecessor;

        /**
         * Class constructor.
         *
         * @param startingStop  the departure stop.
         * @param startingTime  the time of departure.
         * @throws IllegalArgumentException if the time of departure
         *                      is negative.
         */
        public Builder(Stop startingStop, int startingTime)
                throws IllegalArgumentException {
            if (startingTime < 0) {
                throw new IllegalArgumentException("Invalid starting time");
            }
            this.startingStop = startingStop;
            this.startingTime = startingTime;
            this.arrivalTime = new HashMap<Stop, Integer>();
            this.predecessor = new HashMap<Stop, Stop>();
            this.arrivalTime.put(startingStop, startingTime);
            this.predecessor.put(startingStop, null);
        }

        /**
         * Sets the arrival time and the predecessor for a stop.
         *
         * @param stop  the stop for which the arrival time should be set.
         * @param time  the arrival time.
         * @param predecessor the predecessor of the stop.
         * @return      the builder with the updated arrival time
         *              and predecessor.
         * @throws IllegalArgumentException if the time is earlier than the
         *              arrival time.
         */
        public Builder setArrivalTime(Stop stop, int time, Stop predecessor)
                throws IllegalArgumentException {
            if (time < startingTime) {
                throw new IllegalArgumentException("Invalid time of arrival");
            }
            this.arrivalTime.put(stop, time);
            this.predecessor.put(stop, predecessor);
            return this;
        }

        /**
         * Returns the earliest arrival time of a stop.
         *
         * @param stop  the stop for which to return the arrival time.
         * @return      the earliest arrival time for the stop or
         *              SecondsPastMidnight.INFINITE if on arrival time
         *              has not been specified for the stop.
         */
        public int arrivalTime(Stop stop) {
            return arrivalTime.containsKey(stop) ?
                arrivalTime.get(stop) : SecondsPastMidnight.INFINITE;
        }

        /**
         * Constructs the FastestPathTree from the values passed to the builder.
         *
         * @return  the FastestPathTree.
         */
        public FastestPathTree build() {
            return new FastestPathTree(startingStop, arrivalTime, predecessor);
        }
    }

    /**
     * Class constructor.
     *
     * @param startingStop  the starting stop of the tree.
     * @param arrivalTime   the arrival times for all the stop.
     * @param predecessor   the predecessors of all the stops.
     * @throws IllegalArgumentException if the stops in the arrival times
     *                      or in the predecessors are not the same.
     */
    public FastestPathTree(Stop startingStop, Map<Stop, Integer> arrivalTime,
            Map<Stop, Stop> predecessor) throws IllegalArgumentException {
        if (! arrivalTime.keySet().containsAll(predecessor.keySet())) {
            throw new IllegalArgumentException(
                    "Invalid keys: arrivalTime does not contain all predecessors");
        }
        if (! predecessor.keySet().containsAll(arrivalTime.keySet())) {
            System.out.println("predecessor.size = " + predecessor.keySet().size()
                    + ", arrivalTimes.size = " + arrivalTime.keySet().size());
            throw new IllegalArgumentException(
                    "Invalid keys: predecessor does not contain all arrivalTimes");
        }
        this.startingStop = startingStop;
        this.arrivalTime = new HashMap<Stop, Integer>(arrivalTime);
        this.predecessor = new HashMap<Stop, Stop>(predecessor);
    }

    /**
     * Returns the starting stop.
     *
     * @return  the starting stop.
     */
    public Stop startingStop() { return this.startingStop; }

    /**
     * Returns the starting time.
     *
     * @return  the starting time.
     */
    public int startingTime() { return arrivalTime(startingStop); }

    /**
     * Returns all the stops for wich an arrival time exists.
     *
     * @return all the stops for which an arrival time exists.
     */
    public Set<Stop> stops() { return new HashSet<Stop>(arrivalTime.keySet()); }

    /**
     * Returns the arrival time of a stop.
     *
     * @param stop  the stop for which to return the arrival time.
     * @return      the arrival time of the stop of SecondsPastMidnight.INFINITE
     *              if the stop is not contained in the arrival times stops.
     */
    public int arrivalTime(Stop stop) {
        return arrivalTime.containsKey(stop) ?
            arrivalTime.get(stop) : SecondsPastMidnight.INFINITE;
    }

    /**
     * Returns the path from the starting stop to a given destination.
     *
     * @param stop  the destination of the path.
     * @return      a list with all the stops on the path.
     * @throws IllegalArgumentException if the destination
     *              stop is not contained in the arrival times.
     */
    public List<Stop> pathTo(Stop stop) throws IllegalArgumentException {
        if (! arrivalTime.keySet().contains(stop)) {
            throw new IllegalArgumentException(
                    "Stop not contained in arrivalTime");
        }
        LinkedList<Stop> path = new LinkedList<Stop>();
        path.addFirst(stop);
        while (predecessor.containsKey(path.peek())
                && (predecessor.get(path.peek()) != null)) {
            path.addFirst(predecessor.get(path.peek()));
        }
        return path;
    }
}
