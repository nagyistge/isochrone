package ch.epfl.isochrone.timetable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;
import static java.lang.Math.toRadians;

 

public class TestFastestPathTree {
    
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testStartingTime() {
        Set<Stop> stops = new HashSet<Stop>();
        Stop stop1 = new Stop("Stand", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
      //  Stop stop2 = new Stop("EPFL", new PointWGS84(toRadians(6.56591465573),toRadians(46.5221889086)));
        FastestPathTree.Builder fb = new FastestPathTree.Builder(stop1, -1);
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testSetArrivalTime() {
        Set<Stop> stops = new HashSet<Stop>();
        Stop stop1 = new Stop("Stand", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        Stop stop2 = new Stop("EPFL", new PointWGS84(toRadians(6.56591465573),toRadians(46.5221889086)));
        FastestPathTree.Builder fb = new FastestPathTree.Builder(stop1, 10);
        fb.setArrivalTime(stop1, 8, stop2);
        
    }
    

    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        Stop stop = null;
        Map<Stop, Integer> arrivalTimes = null;
        Map<Stop, Stop> predecessors = null;
        FastestPathTree f = new FastestPathTree(stop, arrivalTimes, predecessors);
        Stop s = f.startingStop();
        int i = f.startingTime();
        Set<Stop> ss = f.stops();
        i = f.arrivalTime(stop);
        List<Stop> p = f.pathTo(stop);
        System.out.println("" + s + i + ss + p);

        FastestPathTree.Builder fb = new FastestPathTree.Builder(stop, 0);
        fb.setArrivalTime(stop, 0, stop);
        i = fb.arrivalTime(stop);
        f = fb.build();
    }

    // A compléter avec de véritables méthodes de test...
}
