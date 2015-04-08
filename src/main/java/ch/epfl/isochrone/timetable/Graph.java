package ch.epfl.isochrone.timetable;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;

import static java.lang.Math.rint;

/**
 * Represents a transportation schedule graph.
 *
 * @author Jakob Bauer (223590)
 */
public final class Graph {

    private final Set<Stop> stops;
    private final Map<Stop, List<GraphEdge>> outgoingEdges;

    /**
     * Builder class for the Graph class.
     *
     * @author Jakob Bauer (223590)
     */
    public static final class Builder {

        private final Set<Stop> stops;
        private final Map<Stop, Map<Stop, GraphEdge.Builder>> origsAndDests;

        /**
         * Class constructor.
         *
         * @param stops the stops for the transportation schedule graph.
         */
        public Builder (Set<Stop> stops) {
            this.stops = new HashSet<Stop>(stops);
            this.origsAndDests = new HashMap<Stop, Map<Stop, GraphEdge.Builder>>();
        }

        /**
         * Adds a journey to the graph under construction.
         *
         * @param fromStop      departure stop.
         * @param toStop        destination stop.
         * @param departureTime time of departure in SPM.
         * @param arrivalTime   time of arrival in SPM.
         * @return              the builder with the journey added to it.
         * @throws IllegalArgumentException if one or both of the stops are not
         *                      contained in the stops set passed to the
         *                      constructor, if one of the times is negative or
         *                      if the time of arrival is earlier than the time
         *                      of departure.
         */
        public Builder addTripEdge(Stop fromStop, Stop toStop, int departureTime,
                int arrivalTime) throws IllegalArgumentException {
            if (stops.isEmpty()) {
                throw new IllegalArgumentException("Stops is empty");
            }
            if (!(stops.contains(fromStop) && stops.contains(toStop))) {
                throw new IllegalArgumentException(
                        "starting or destination stop not contained in stops");
            }
            if ((departureTime < 0) || (arrivalTime < 0)
                    || (arrivalTime < departureTime)) {
                throw new IllegalArgumentException(
                        "invalid departure or arrival time");
            }
            getEdgeBuilder(fromStop, toStop).addTrip(departureTime, arrivalTime);
            return this;
        }

        /**
         * Adds all walking edges to the graph under construction.
         *
         * @param maxWalkingTime    specifies the maximum walking time to be
         *                          taken into consideration when calculating
         *                          the individual walking times.
         * @param walkingSpeed      the walking speed in meters per second.
         * @return                  the builder with all walking times added
         *                          to it
         * @throws IllegalArgumentException if the maximum walking time is
         *                          negative or if the walking speed is
         *                          negative or zero.
         */
        public Builder addAllWalkEdges(int maxWalkingTime, double walkingSpeed)
                throws IllegalArgumentException {
            if (maxWalkingTime < 0) {
                throw new IllegalArgumentException("invalid max walking time");
            }
            if (walkingSpeed <= 0) {
                throw new IllegalArgumentException("invalid walking speed");
            }
            List<Stop> stopList = new ArrayList<Stop>(stops);
            for (Stop i : stopList) {
                for (Stop j : stopList.subList(stopList.indexOf(i)+1, stopList.size())) {
                    double distance = i.position().distanceTo(j.position());
                    int walkingTime = (int) rint(distance / walkingSpeed);
                    if (walkingTime < maxWalkingTime) {
                        getEdgeBuilder(i, j).setWalkingTime(walkingTime);
                        getEdgeBuilder(j, i).setWalkingTime(walkingTime);
                    }
                }
            }
            return this;
        }

        /**
         * Constructs a new Graph with the values passed to the Builder.
         *
         * @return a new Graph with the values passed to the Builder.
         */
        public Graph build() {
            Map<Stop, List<GraphEdge>> outgoingEdges = new HashMap<Stop, List<GraphEdge>>();
            for (Stop s : origsAndDests.keySet()) {
                Map<Stop, GraphEdge.Builder> innerMap = new HashMap<Stop, GraphEdge.Builder>(origsAndDests.get(s));
                List<GraphEdge> edges = new ArrayList<GraphEdge>();
                for (Stop t : innerMap.keySet()) {
                    GraphEdge edge = innerMap.get(t).build();
                    edges.add(edge);
                }
                outgoingEdges.put(s, edges);
            }
            return new Graph(stops, outgoingEdges);
        }

        private GraphEdge.Builder getEdgeBuilder(Stop fromStop, Stop toStop) {
            if (! origsAndDests.containsKey(fromStop)) {
                Map<Stop, GraphEdge.Builder> newDest =
                    new HashMap<Stop, GraphEdge.Builder>();
                newDest.put(toStop, new GraphEdge.Builder(toStop));
                origsAndDests.put(fromStop, newDest);
            } else if (! origsAndDests.get(fromStop).containsKey(toStop)) {
                GraphEdge.Builder newGEB = new GraphEdge.Builder(toStop);
                origsAndDests.get(fromStop).put(toStop, newGEB);
            }
            return origsAndDests.get(fromStop).get(toStop);
        }
    }

    private Graph(Set<Stop> stops, Map<Stop, List<GraphEdge>> outgoingEdges) {
        assert stops.containsAll(outgoingEdges.keySet()) :
                "Stops used in outgoingEdges not contained in stops";
        for (List<GraphEdge> l : outgoingEdges.values()) {
            for (GraphEdge g : l) {
                assert stops.contains(g.destination()) :
                        "Not all destinations contained in stops";
            }
        }
        // if the builder guarantees that the parameters passed to the
        // constructor are valid and are not modified later on, then
        // the constructor does not have to verify or copy them.
        this.stops = stops;
        this.outgoingEdges = outgoingEdges;
    }

    /**
     * Returns the fastest path tree for a given starting stop and a
     * given departure time.
     *
     * @param startingStop  the root of the fastest path tree.
     * @param departureTime the departure time in SPM.
     * @return              the fastest path tree with startingStop
     *                      as its root.
     * @throws IllegalArgumentException if the starting stop is not
     *                      contained in the stop set.
     */
    public FastestPathTree fastestPaths(Stop startingStop, int departureTime)
            throws IllegalArgumentException {
        if (! stops.contains(startingStop)) {
            throw new IllegalArgumentException(
                    "Starting stop is not contained in stops");
        }
        // Pseudocode of Dijkstra's algorithm taken from Cormen et al.,
        // Introduction to Algorithms, 3rd ed., Cambridge MA, 2009
        //
        // DIJKSTRA(G,w,s)
        // 1    INITIALIZE-SINGLE-SOURCE(G,s)
        // 2    S = emptylist;
        // 3    Q = G.V
        // 4    while Q not empty
        // 5        u = EXTRACT-MIN(Q)
        // 6        S = S union {u}
        // 7        for each vertex v in G.Adj[u]
        // 8            RELAX(u,v,w)
        //
        // INITIALIZE-SINGLE-SOURCE(G,s)
        // 1    for each vertex v in G.V
        // 2        v.d = infinity
        // 3        v.p = NIL
        // 4    s.d = 0
        //
        // RELAX(u,v,w)
        // 1    if v.d > u.d + w(u,v)
        // 2        v.d = u.d + w(u,v)
        // 3        v.p = u

        // INITIALIZE-SINGLE-SOURCE(G,s)
        // is implemented via FastestPathTree.Builder.arrivalTime(Stop stop)
        // method that returns the earliest arrival time of the stop or
        // INFINITE if no arrival time has been specified yet

        // Implements the set S of stops whose final shortest paths
        // have already been determined.
        // Has to be final because o/w it cannot be used inside the anonymous
        // Comparator<Stop> class further below.
        final FastestPathTree.Builder treeBuilder =
            new FastestPathTree.Builder(startingStop, departureTime);

        // Implements the priority queue Q = G.V which contains all the stops
        int numberOfStops = stops.size();
        Comparator<Stop> comparator = new Comparator<Stop>() {
            // Priority queue sorts by least element first
            @Override
            public int compare(Stop stop1, Stop stop2) {
                return treeBuilder.arrivalTime(stop1) - treeBuilder.arrivalTime(stop2);
            }
        };

        PriorityQueue<Stop> q = new PriorityQueue<Stop>(numberOfStops, comparator);
        for (Stop s : stops) {
            q.add(s);
        }

        //Implements the for loop in DIJKSTRA (lines 4-8)
        boolean finiteArrivalTime = true;
        do {
            // PriorityQueue.poll() implements EXTRACT-MIN(Q)
            Stop u = q.poll();
            if (treeBuilder.arrivalTime(u) < SecondsPastMidnight.INFINITE) {
                for (GraphEdge e : outgoingEdges.get(u)) {
                    Stop v = e.destination();
                    int oldArrivalTime = treeBuilder.arrivalTime(v);
                    int newArrivalTime =
                        e.earliestArrivalTime(treeBuilder.arrivalTime(u));
                    if (oldArrivalTime > newArrivalTime) {
                        // If the ordering changes, element has to be removed
                        // from queue and reinserted
                        q.remove(v);
                        treeBuilder.setArrivalTime(v, newArrivalTime, u);
                        q.add(v);
                    }
                }
            } else {
                finiteArrivalTime = false;
            }
        } while ((! q.isEmpty()) && finiteArrivalTime);
        return treeBuilder.build();
    }
}
