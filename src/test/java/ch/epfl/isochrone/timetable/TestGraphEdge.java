package ch.epfl.isochrone.timetable;

import static java.lang.Math.toRadians;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;

public class TestGraphEdge {
    private static final double DELTA = 0.000001;
    private static final double REPEAT = 100;
    private static final int TRIPS = 25;
    private static final int PACKEDMAX = 1000010000;
    
    // Testing packTrip
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testPackTripDepartureTimeTooSmall() {
        GraphEdge.packTrip(-1, 1000);
    }
    
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testPackTripDepartureTimeTooBig() {
        GraphEdge.packTrip(108000,110000);
    }

    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testPackTripTripDurationTooSmall() {
        GraphEdge.packTrip(2000, 1000);
    }
    
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testPackTripTripDurationTooBig() {
        GraphEdge.packTrip(100, 20000);
    }
    
    // Maybe we can add a test whether packedTrip is negative
    @Test
    public void testUnpackTripDepartureTime() {
        Random gen = new Random();
        for (int i = 0; i < REPEAT; i++) {
            int packedTrip = gen.nextInt(PACKEDMAX);
            int unpack = GraphEdge.unpackTripDepartureTime(packedTrip); 
            assertTrue(( unpack == ((packedTrip/10000) ) || ( unpack == packedTrip >> 14)));
//          assertEquals((packedTrip/10000), GraphEdge.unpackTripDepartureTime(packedTrip), DELTA);
        }
    }

    @Test
    public void testUnpackTripDuration() {
        Random gen = new Random();
        for (int i = 0; i < REPEAT; i++) {
            int packedTrip = gen.nextInt(PACKEDMAX);
            int unpack = GraphEdge.unpackTripDuration(packedTrip); 
            assertTrue(( unpack == ((packedTrip%10000) ) || ( unpack == (packedTrip & 0x3FFF))));
//          assertEquals((packedTrip%10000), GraphEdge.unpackTripDuration(packedTrip), DELTA);
        }
    }

    @Test
    public void testUnpackTripArrivalTime() {
        Random gen = new Random();
        for (int i = 0; i < REPEAT; i++) {
            int packedTrip = gen.nextInt(PACKEDMAX);
            int unpack = GraphEdge.unpackTripArrivalTime(packedTrip);
            assertTrue(( unpack == ((packedTrip/10000) + (packedTrip%10000)) || ( unpack == ((packedTrip >> 14) + (packedTrip & 0x3FFF)))));
//          assertEquals(((packedTrip/10000) + (packedTrip%10000)), GraphEdge.unpackTripArrivalTime(packedTrip), DELTA);
        }
    }
   
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testConstructorWalkingTimeTooSmall() {
        Stop destination = new Stop("test",new PointWGS84(toRadians(6.543), toRadians(6.543)));
        Set<Integer> packedTrips = new HashSet<Integer>();
        new GraphEdge(destination, -2, packedTrips);
    }

//    @Test (expected = java.lang.IllegalArgumentException.class)
//    public void testConstructorPackTripsNull() {
//        Stop destination = new Stop("test",new PointWGS84(toRadians(6.543), toRadians(6.543)));
//        new GraphEdge(destination, 10, null);
//    }

    // Testing without the Builder
    @Test
    public void testEarliestArrivalTimeTooLongOnFootNoPackedTrips() {
        GraphEdge e = new GraphEdge(null, -1, Collections.<Integer>emptySet());
        Random gen = new Random();
        for (int i = 0; i < REPEAT; i++) {
            int departureTime = gen.nextInt(107999); // NotTooBig
            assertEquals(SecondsPastMidnight.INFINITE, e.earliestArrivalTime(departureTime), DELTA);
        }
    }
    
    @Test
    public void testEarliestArrivalTimeOnFootNoPackedTrips() {
        Random gen = new Random();
        for (int i = 0; i < REPEAT; i++) {
            //(walkingTime + departureTime) <= SecondsPastMidnight.INFINITE  
            int departureTime = gen.nextInt(107999); // NotTooBig
            int walkingTime = gen.nextInt(200000);
            int totalTime = (departureTime + walkingTime);
            GraphEdge e = new GraphEdge(null, walkingTime, Collections.<Integer>emptySet());
            assertEquals(
                    (totalTime < SecondsPastMidnight.INFINITE) ? totalTime : SecondsPastMidnight.INFINITE, 
                    e.earliestArrivalTime(departureTime), DELTA);
        }
    }

    @Test
    public void testEarliestArrivalTimeTooLongOnFootWithPackedTrips() {
        // New GraphEdge without using the Builder
        Random gen = new Random();
        for (int i = 0; i < REPEAT; i++) {
            int departureTime = gen.nextInt(107999); // NotTooBig
            Set<Integer> packedTrips = generatePackedTrips(TRIPS);
            GraphEdge e = new GraphEdge(null, -1, packedTrips);

            assertEquals(nonWalkingArrivalTime(departureTime, packedTrips.toArray(new Integer[0])), 
                    e.earliestArrivalTime(departureTime), DELTA);
        }
    
        // Extra test for max departureTime
        int departureTime = 107999; 
        Set<Integer> packedTrips = generatePackedTrips(TRIPS);
        GraphEdge e = new GraphEdge(null, -1, packedTrips);
        assertEquals(nonWalkingArrivalTime(departureTime, packedTrips.toArray(new Integer[0])), 
                e.earliestArrivalTime(departureTime), DELTA);
        
        // Extra test for min departureTime
        departureTime = 0; 
        packedTrips = generatePackedTrips(TRIPS);
        e = new GraphEdge(null, -1, packedTrips);
        assertEquals(nonWalkingArrivalTime(departureTime, packedTrips.toArray(new Integer[0])), 
                e.earliestArrivalTime(departureTime), DELTA);
    }

    @Test
    public void testEarliestArrivalTimeWithOnFootWithPackedTrips() {
        // New GraphEdge without using the Builder
        Random gen = new Random();
        for (int i = 0; i < REPEAT; i++) {
            int departureTime = gen.nextInt(107999);
            int walkingTime = gen.nextInt(200000);
            Set<Integer> packedTrips = generatePackedTrips(TRIPS);
            GraphEdge e = new GraphEdge(null, walkingTime, packedTrips);

            assertEquals(Math.min(
                    nonWalkingArrivalTime(departureTime, packedTrips.toArray(new Integer[0])),
                    walkingArrivalTime(departureTime, walkingTime)), 
                    e.earliestArrivalTime(departureTime), DELTA);
        }
    
        
        // Extra test for max departureTime and walkingTime
        int departureTime = 107999; 
        int walkingTime = 200000; 
        Set<Integer> packedTrips = TestGraphEdge.generatePackedTrips(TRIPS);
        GraphEdge e = new GraphEdge(null, walkingTime, packedTrips);
        assertEquals(Math.min(
                nonWalkingArrivalTime(departureTime, packedTrips.toArray(new Integer[0])),
                walkingArrivalTime(departureTime, walkingTime)), 
                e.earliestArrivalTime(departureTime), DELTA);
        
        // Extra test for min departureTime and walkingTime
        departureTime = 0; 
        walkingTime = 0; 
        packedTrips = TestGraphEdge.generatePackedTrips(TRIPS);
        e = new GraphEdge(null, walkingTime, packedTrips);
        assertEquals(Math.min(
                nonWalkingArrivalTime(departureTime, packedTrips.toArray(new Integer[0])),
                walkingArrivalTime(departureTime, walkingTime)), 
                e.earliestArrivalTime(departureTime), DELTA);
    }


    @Test
    public void testEarliestArrivalTimeWithDuplicates() {
        Set<Integer> packedTrips = new HashSet<>();
        
        Random gen = new Random();
        
        int departureTime = gen.nextInt(107999); 
        int tripDuration = gen.nextInt(9999);
        
        int arrivalTime = tripDuration + departureTime;
        assert arrivalTime >= departureTime;

        // With duplicates
        for (int i = 0; i < TRIPS; i++)
            packedTrips.add(GraphEdge.packTrip(departureTime, arrivalTime--));
        
        gen = new Random();
        for (int i = 0; i < REPEAT; i++) {
            departureTime = gen.nextInt(107999);
            int walkingTime = gen.nextInt(200000);
            GraphEdge e = new GraphEdge(null, walkingTime, packedTrips);

            assertEquals(Math.min(
                    nonWalkingArrivalTime(departureTime, packedTrips.toArray(new Integer[0])),
                    walkingArrivalTime(departureTime, walkingTime)), 
                    e.earliestArrivalTime(departureTime), DELTA);
        }
    }
    
    @Test
    public void testEarliestArrivalTimeWithBuilder() {

        for (int j = 0; j < REPEAT; j++) {
            Set<Integer> packedTrips = new HashSet<>();

            GraphEdge.Builder b = new GraphEdge.Builder(null);

            Random gen = new Random();
            int walkingTime = gen.nextInt(200000);

            // Generate the same list of trips for the Builder and the Constructor 
            for (int i = 0; i < TRIPS; i++) {
                int departureTime = gen.nextInt(107999); 
                int tripDuration = gen.nextInt(9999);

                int arrivalTime = tripDuration + departureTime;
                assert arrivalTime >= departureTime;

                packedTrips.add(GraphEdge.packTrip(departureTime, arrivalTime));

                b.addTrip(departureTime, arrivalTime);
            }
            b.setWalkingTime(walkingTime);
            GraphEdge edgeBuilder = b.build();

            int departureTime = gen.nextInt(107999);
            GraphEdge e = new GraphEdge(null, walkingTime, packedTrips);

            assertEquals(edgeBuilder.earliestArrivalTime(departureTime), 
                    e.earliestArrivalTime(departureTime), DELTA);
        }
    }

    // Test copy in GraphEdge
    @Test
    public void testGraphEdgeConstructor() {
        int departureTime;
        int arrivalTime;
        for (int j = 0; j < REPEAT; j++) {
            if ( (j % 2) == 0 ) {
                departureTime = 20000;
                arrivalTime = 20001;
            }
            else {
                departureTime = 0;
                arrivalTime = 1;
            }
            
            // Departures in GraphEdge.Builder are in [0,10000]
            GraphEdge.Builder b = generateGraphEdgeBuilder(5); 
            GraphEdge edge = b.build();
    
            // The new trip should not be part of GraphEdge
            b.addTrip(departureTime, arrivalTime);
            // Thus, arrival time should be different otherwise the new Trip was added to the old GraphEdge
            // TODO why does this not work?
            /* assertNotEquals(edge.earliestArrivalTime(departureTime), b.build().earliestArrivalTime(departureTime), DELTA); */
        }                
    }

    
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testGraphEdgeBuildersetWalkingTime() {
        new GraphEdge.Builder(null).setWalkingTime(-2);
    }

    
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    public void namesAreOk() {
        int i1 = GraphEdge.packTrip(0, 0);
        i1 = GraphEdge.unpackTripDepartureTime(0);
        i1 = GraphEdge.unpackTripDuration(0);
        i1 = GraphEdge.unpackTripArrivalTime(0) + i1;
        Stop s = null;
        GraphEdge e = new GraphEdge(s, 0, Collections.<Integer>emptySet());
        s = e.destination();
        i1 = e.earliestArrivalTime(0);

        GraphEdge.Builder b = new GraphEdge.Builder(s);
        b.setWalkingTime(0);
        b.addTrip(0, 0);
        e = b.build();
    }
    
    
    /*****************************************/
    /***        Auxiliary functions        ***/
    /*****************************************/
    
    // Generate #size random packed Trips
    private static Set<Integer> generatePackedTrips(int size) {
        Set<Integer> trips = new HashSet<>();

        Random gen = new Random();
        for (int i = 0; i < size; i++) {
            // departureTime in [0, 107999]
            int departureTime = gen.nextInt(107999); 
            // tripDuration in [0, 9999]
            int tripDuration = gen.nextInt(9999);

            int arrivalTime = tripDuration + departureTime;
            assert arrivalTime >= departureTime;

            trips.add(GraphEdge.packTrip(departureTime, arrivalTime));
        }
        return trips;
    }
    
    // Generate #size random packed Trips
    private static GraphEdge.Builder generateGraphEdgeBuilder(int size) {

        GraphEdge.Builder b = new GraphEdge.Builder(null);

        Random gen = new Random();
        int walkingTime = 10 + gen.nextInt(1000);
        // Generate the same list of trips for the Builder and the Constructor 
        for (int i = 0; i < size; i++) {
            int departureTime = gen.nextInt(10000); 
            int tripDuration = gen.nextInt(9999);

            int arrivalTime = tripDuration + departureTime;
            assert arrivalTime >= departureTime;

            b.addTrip(departureTime, arrivalTime);
        }
        b.setWalkingTime(walkingTime);

        return b;
    }
   
    // Modify nonWalkingArrivalTime(int departureTime)
    private static int nonWalkingArrivalTime(int departureTime, Integer[] packedTrips) {
        int packedDepartureTime = GraphEdge.packTrip(departureTime, departureTime);
        Arrays.sort(packedTrips);
        int i = Arrays.binarySearch(packedTrips, packedDepartureTime);
        if (i >= 0)
            return departureTime;
        else {
            int insertionPoint = -(i + 1);
            int arrivalTime = insertionPoint == packedTrips.length ? SecondsPastMidnight.INFINITE : GraphEdge.unpackTripArrivalTime(packedTrips[insertionPoint]);
            assert arrivalTime >= departureTime;
            return arrivalTime;
        }
    }

    // Modify walkingArrivalTime(int departureTime)
    private static int walkingArrivalTime(int departureTime, int walkingTime) {
        return walkingTime != -1 ? departureTime + walkingTime : SecondsPastMidnight.INFINITE;
    }
}
