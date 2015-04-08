package ch.epfl.isochrone.timetable;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import static java.lang.Math.toRadians;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;

public class TestGraph {
    
    
    private TimeTableReader r ;
    private TimeTable t;
    
    @Before
    public void init() throws IOException{
        r = new TimeTableReader("/time-table/");
        t = r.readTimeTable();
    }
    
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testFromStop() {
        Set<Stop> stops = new HashSet<Stop>();
        Stop stop1 = new Stop("Stand", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        Stop stop2 = new Stop("EPFL", new PointWGS84(toRadians(6.56591465573),toRadians(46.5221889086)));
        Stop stop3 = new Stop("Lausanne-Gare", new PointWGS84(toRadians(6.629371849),toRadians(46.5174432543)));
        Stop stop4 = new Stop("Renens-Gare", new PointWGS84(toRadians(6.57848590863),toRadians(46.5373037657)));
        Stop stop5 = new Stop("Lausanne-Flon", new PointWGS84(toRadians(6.6303347592),toRadians(46.5206173997)));
        
        stops.add(stop1);
        stops.add(stop2);
        stops.add(stop3);
        stops.add(stop4);
        
        Graph.Builder gb = new Graph.Builder(stops);
        gb = gb.addTripEdge(stop5, stop4, 0, 0);
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testToStop() {
        Set<Stop> stops = new HashSet<Stop>();
        Stop stop1 = new Stop("Stand", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        Stop stop2 = new Stop("EPFL", new PointWGS84(toRadians(6.56591465573),toRadians(46.5221889086)));
        Stop stop3 = new Stop("Lausanne-Gare", new PointWGS84(toRadians(6.629371849),toRadians(46.5174432543)));
        Stop stop4 = new Stop("Renens-Gare", new PointWGS84(toRadians(6.57848590863),toRadians(46.5373037657)));
        Stop stop5 = new Stop("Lausanne-Flon", new PointWGS84(toRadians(6.6303347592),toRadians(46.5206173997)));
        
        stops.add(stop1);
        stops.add(stop2);
        stops.add(stop3);
        stops.add(stop4);
        Graph.Builder gb = new Graph.Builder(stops);
        gb = gb.addTripEdge(stop4, stop5, 0, 0);
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testDeparture() {
        Set<Stop> stops = new HashSet<Stop>();
        Stop stop1 = new Stop("Stand", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        Stop stop2 = new Stop("EPFL", new PointWGS84(toRadians(6.56591465573),toRadians(46.5221889086)));
        Stop stop3 = new Stop("Lausanne-Gare", new PointWGS84(toRadians(6.629371849),toRadians(46.5174432543)));
        Stop stop4 = new Stop("Renens-Gare", new PointWGS84(toRadians(6.57848590863),toRadians(46.5373037657)));
        
        stops.add(stop1);
        stops.add(stop2);
        stops.add(stop3);
        stops.add(stop4);
        
        Graph.Builder gb = new Graph.Builder(stops);
        gb = gb.addTripEdge(stop3, stop4, -1, 1);
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testArrival() {
        Set<Stop> stops = new HashSet<Stop>();
        Stop stop1 = new Stop("Stand", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        Stop stop2 = new Stop("EPFL", new PointWGS84(toRadians(6.56591465573),toRadians(46.5221889086)));
        Stop stop3 = new Stop("Lausanne-Gare", new PointWGS84(toRadians(6.629371849),toRadians(46.5174432543)));
        Stop stop4 = new Stop("Renens-Gare", new PointWGS84(toRadians(6.57848590863),toRadians(46.5373037657)));
        
        stops.add(stop1);
        stops.add(stop2);
        stops.add(stop3);
        stops.add(stop4);
        
        Graph.Builder gb = new Graph.Builder(stops);
        gb = gb.addTripEdge(stop3, stop4, 1, -1);
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testArrivalBeforeDeparture() {
        Set<Stop> stops = new HashSet<Stop>();
        Stop stop1 = new Stop("Stand", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        Stop stop2 = new Stop("EPFL", new PointWGS84(toRadians(6.56591465573),toRadians(46.5221889086)));
        Stop stop3 = new Stop("Lausanne-Gare", new PointWGS84(toRadians(6.629371849),toRadians(46.5174432543)));
        Stop stop4 = new Stop("Renens-Gare", new PointWGS84(toRadians(6.57848590863),toRadians(46.5373037657)));
        
        stops.add(stop1);
        stops.add(stop2);
        stops.add(stop3);
        stops.add(stop4);
        
        Graph.Builder gb = new Graph.Builder(stops);
        gb = gb.addTripEdge(stop3, stop4, 10, 9);
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testMaxWalkingTime() {
        Set<Stop> stops = new HashSet<Stop>();
        Stop stop1 = new Stop("Stand", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        Stop stop2 = new Stop("EPFL", new PointWGS84(toRadians(6.56591465573),toRadians(46.5221889086)));
       
        stops.add(stop1);
        stops.add(stop2);
        
        
        Graph.Builder gb = new Graph.Builder(stops);
        gb = gb.addAllWalkEdges(-1, 10);
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testMaxWalkingSpeed() {
        Set<Stop> stops = new HashSet<Stop>();
        Stop stop1 = new Stop("Stand", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        Stop stop2 = new Stop("EPFL", new PointWGS84(toRadians(6.56591465573),toRadians(46.5221889086)));
        stops.add(stop1);
        stops.add(stop2);
        Graph.Builder gb = new Graph.Builder(stops);
        gb = gb.addAllWalkEdges(10, 0);
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testFastestPathsStartStop() throws IOException {
        Date travelDate = new Date(1,10,2013);
        int startingTime = SecondsPastMidnight.fromHMS(6,8,0);
        String startingStopName = "Lausanne-Flon";
        
        int walkingTime = 5 * 60;
        double walkingSpeed = 1.25;
        
        
        Set<Service> services = t.servicesForDate(travelDate);
        
        Stop startingStop = null;
        for (Stop s: t.stops()) {
            if (s.name().equals(startingStopName)) {
                startingStop = s;
                break;
            }
        }
        if (startingStop == null)
            throw new IllegalArgumentException("unknown stop: " + startingStopName);
       
        Graph g = r.readGraphForServices(t.stops(), services, walkingTime, walkingSpeed);
        
        Stop stop1 = new Stop("StandEPFL", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        FastestPathTree f = g.fastestPaths(stop1, startingTime);
        
    }
    
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testFastestPathsStartTime() throws IOException {
        Date travelDate = new Date(1,10,2013);
        String startingStopName = "Lausanne-Flon";
        
        int walkingTime = 5 * 60;
        double walkingSpeed = 1.25;
        
        Set<Service> services = t.servicesForDate(travelDate);
        
        Stop startingStop = null;
        for (Stop s: t.stops()) {
            if (s.name().equals(startingStopName)) {
                startingStop = s;
                break;
            }
        }
        if (startingStop == null)
            throw new IllegalArgumentException("unknown stop: " + startingStopName);
       
        Graph g = r.readGraphForServices(t.stops(), services, walkingTime, walkingSpeed);
        
        FastestPathTree f = g.fastestPaths(startingStop, -1);
        
    }
    
    @Test
    public void testFastestPaths() throws IOException {
        Date travelDate = new Date(1,10,2013);
        int startingTime = SecondsPastMidnight.fromHMS(6,8,0);
        String startingStopName = "Lausanne-Flon";
        
        int walkingTime = 5 * 60;
        double walkingSpeed = 1.25;
        
        Set<Service> services = t.servicesForDate(travelDate);
        
        Stop startingStop = null;
        for (Stop s: t.stops()) {
            if (s.name().equals(startingStopName)) {
                startingStop = s;
                break;
            }
        }
        if (startingStop == null)
            throw new IllegalArgumentException("unknown stop: " + startingStopName);
       
        Graph g = r.readGraphForServices(t.stops(), services, walkingTime, walkingSpeed);
        
        Stop stop1 = new Stop("StandEPFL", new PointWGS84(toRadians(6.5624795866),toRadians(46.5327194855)));
        FastestPathTree f = g.fastestPaths(startingStop, startingTime);
        
        List<Stop> sortedStops = new ArrayList<>(f.stops());
        Collections.sort(sortedStops, new Comparator<Stop>() {
            @Override
            public int compare(Stop o1, Stop o2) {
                return o1.name().compareTo(o2.name());
        }});
        
        /**
         * random check five records 
         * if all five tests are passed, we assume that 
         * the program is right.
         */
        String str1 = "1er Mai06:26:00[Lausanne-Flon, Port-Franc, EPSIC, Ecole des Métiers, Couchirard, Prélaz-les-Roses, Galicien, Perrelet, Florissant, Broye, Bourg-Dessus, Follieu, Borjod, Saugiaz, 1er Mai]";
        String str67 = "CHUV06:15:16[Lausanne-Flon, Rôtillon, Bessières, Ours, CHUV]";
        String str132 = "Croix-de-Plan06:36:00[Lausanne-Flon, Port-Franc, EPSIC, Ecole des Métiers, Couchirard, Prélaz-les-Roses, Galicien, Perrelet, Renens-Village, Sous l'Eglise, Hôtel-de-Ville, Avenir, Renens-14 Avril, Jura, Zinguerie, Arc-en-Ciel, Croix-de-Plan]";
        String str248 = "Margerol06:23:00[Lausanne-Flon, Rôtillon, Bessières, Ours, Béthusy, Fauconnières, Pont de Chailly, Chailly-Village, Coudrette, Rosiaz, Margerol]";
        String str365 = "Rive06:26:00[Lausanne-Flon, Rôtillon, St-François, Georgette, Eglantine, Avenue du Léman, Bonne-Espérance, Perraudettaz, Montillier, Pully-Clergère, Reymondin, Moulins, Paudex, Marronnier, Grand-Pont, Voisinand, Rive]";
        String str444 = "Villard06:14:00[Lausanne-Flon, Lausanne-Gare, Villard]";
        
        assertEquals(456, sortedStops.size());
        
        Stop s1 = sortedStops.get(1);
        String str = s1.name()+SecondsPastMidnight.toString(f.arrivalTime(s1))+f.pathTo(s1);
        assertEquals(str1, str);
        
        s1 = sortedStops.get(67);
        str = s1.name()+SecondsPastMidnight.toString(f.arrivalTime(s1))+f.pathTo(s1);
        assertEquals(str67, str);
        
        s1 = sortedStops.get(132);
        str = s1.name()+SecondsPastMidnight.toString(f.arrivalTime(s1))+f.pathTo(s1);
        assertEquals(str132, str);
        
        s1 = sortedStops.get(248);
        str = s1.name()+SecondsPastMidnight.toString(f.arrivalTime(s1))+f.pathTo(s1);
        assertEquals(str248, str);
        
        s1 = sortedStops.get(365);
        str = s1.name()+SecondsPastMidnight.toString(f.arrivalTime(s1))+f.pathTo(s1);
        assertEquals(str365, str);
        
        s1 = sortedStops.get(444);
        str = s1.name()+SecondsPastMidnight.toString(f.arrivalTime(s1))+f.pathTo(s1);
        assertEquals(str444, str);
        
    }

    
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        // Graph n'a aucune méthode publique à ce stade...

        Set<Stop> stops = null;
        Stop stop = null;
        Graph.Builder gb = new Graph.Builder(stops);
        gb.addTripEdge(stop, stop, 0, 0);
        gb.addAllWalkEdges(0, 0);
        gb.build();
    }

    // A compléter avec de véritables méthodes de test...
}
