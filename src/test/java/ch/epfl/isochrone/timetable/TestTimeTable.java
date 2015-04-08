package ch.epfl.isochrone.timetable;

import static java.lang.Math.toRadians;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.timetable.Date.DayOfWeek;
import ch.epfl.isochrone.timetable.Date.Month;

public class TestTimeTable {
    
    @Test
    public void testServicesForDate() {
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
        stops.add(stop5);
        
        Set<Service> services = new HashSet<Service>();
        Date start = new Date(20, Month.MARCH, 2014);
        Date end = new Date(30, Month.MARCH, 2014);
        
        Service.Builder sb = new Service.Builder("s", start, end);
        sb.addOperatingDay(DayOfWeek.FRIDAY);
        services.add(sb.build());
        sb.addExcludedDate(new Date(21, Month.MARCH, 2014));
        services.add(sb.build());
        
        TimeTable t = new TimeTable(stops,services);
        
        Set<Service> testServices = t.servicesForDate(new Date(21, Month.MARCH, 2014));
        assertEquals(1, testServices.size());
        
        Set<Service> testServices2 = t.servicesForDate(new Date(28, Month.MARCH, 2014));
        assertEquals(2, testServices2.size());
    }

    
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        TimeTable t = new TimeTable(Collections.<Stop> emptySet(),
                Collections.<Service> emptySet());
        t.stops();
        t.servicesForDate(new Date(1, Month.JANUARY, 2000));

        TimeTable.Builder b = new TimeTable.Builder();
        b.addStop(new Stop("s", new PointWGS84(0, 0)));
        Date d = new Date(1, Month.APRIL, 2000);
        b.addService(new Service("s", d, d, Collections.<DayOfWeek> emptySet(),
                Collections.<Date> emptySet(), Collections.<Date> emptySet()));
        b.build();
    }

    // A compléter avec de véritables méthodes de test...
}
