package ch.epfl.isochrone.timetable;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.timetable.Date.Month;

public class TestTimeTableReader {

    private static final double DELTA = 0.000001;
    private String baseResourceName = "/time-table/";

    @Test
    public void testReadTimeTableStops() throws IOException {
        
        // Read stops using input code
        TimeTableReader reader = new TimeTableReader(baseResourceName);
        TimeTable timetable = reader.readTimeTable();

        // Read stops and services using testing code
        TimeTable testTimetable = new TestTimeTableReader().TestReadTimeTable();
        
        Set<Stop> stops = timetable.stops();
        Set<Stop> testStops = testTimetable.stops();
        
        // Test #1: Number of elements in stop //
        assertEquals(stops.size(), testStops.size(), DELTA);
        
        // Test #2: Test if we have stored the same stops // 
        Stop stopsArray[] = stops.toArray(new Stop[0]);
        Stop testStopsArray[] = testStops.toArray(new Stop[0]);

        // Sort them by name
        Arrays.sort(stopsArray, new StopComparator());
        Arrays.sort(testStopsArray, new StopComparator());
        
        // Test if they are the same
        for (int  i = 0; i < stopsArray.length; i++) {
            // Same name
            assertEquals(0, stopsArray[i].name().compareTo(testStopsArray[i].name()), DELTA);
            
            // Same PointWGS84.longitude
            assertEquals(stopsArray[i].position().longitude(), testStopsArray[i].position().longitude(), DELTA);

            // Same PointWGS84.latitude
            assertEquals(stopsArray[i].position().latitude(), testStopsArray[i].position().latitude(), DELTA);
        }
    }

    // Custom comparator for Stops (use name)
    private class StopComparator implements Comparator<Stop> {
        public int compare(Stop o1, Stop o2) {
            return o1.name().compareTo(o2.name());
        }
    }
    

    // Services cannot be directly accessed by TimeTable 
    @Test
    public void testReadTimeTableServices() throws IOException {
        // Read services using input code
        TimeTableReader reader = new TimeTableReader(baseResourceName);
        TimeTable timetable = reader.readTimeTable();

        // Read stops and services using testing code
        TimeTable testTimetable = new TestTimeTableReader().TestReadTimeTable();

        // Test for year [2011,2015] almost all days
        for (int z = 2011; z <= 2015; z++) {
            for (int j = 1; j <= 12; j++) {
                for (int i = 1; i <= 28; i++) { // Avoid corner cases
                    Date date = new Date(i, j, z);
                    Set<Service> services = timetable.servicesForDate(date);
                    Set<Service> testServices = testTimetable.servicesForDate(date);

                    // Test #1: Number of elements in service //
                    assertEquals(services.size(), testServices.size(), DELTA);

                    // Test #2: Same service name //
                    Service servicesArray[] = services.toArray(new Service[0]);
                    Service testServicesArray[] = testServices.toArray(new Service[0]);

                    // Sort them by name
                    Arrays.sort(servicesArray, new ServiceComparator());
                    Arrays.sort(testServicesArray, new ServiceComparator());

                    // Test if they are the same
                    for (int  k = 0; k < servicesArray.length; k++) 
                        assertEquals(0, servicesArray[k].name().compareTo(testServicesArray[k].name()), DELTA);
                }
            }
        }
    }

    // Custom comparator for Service (use name)
    private class ServiceComparator implements Comparator<Service> {
        public int compare(Service s1, Service s2) {
            return s1.name().compareTo(s2.name());
        }
    }

    // Just a dummy test...
    @Test
    public void testReadGraphForServices() throws IOException {
        int walkingTime = 5 * 60;
        double walkingSpeed = 1.25;

        TimeTableReader reader = new TimeTableReader(baseResourceName);
        TimeTable timetable = reader.readTimeTable();

        // Test #1:
        // Test the first 5 days of January 2013 (otherwise too slow)
        int year = 2013; 
        Month month = Month.JANUARY;
        for (int i = 1; i <= 5; i++) 
        { 
            Date date = new Date(i, month, year);
            Set<Service> services = timetable.servicesForDate(date);
            Graph g = reader.readGraphForServices(timetable.stops(), services, walkingTime, walkingSpeed);
        }
    }

    
    
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() throws IOException {
        TimeTableReader r = new TimeTableReader("");
        TimeTable t = r.readTimeTable();
        Graph g = r.readGraphForServices(t.stops(), Collections.<Service>emptySet(), 0, 0d);
        System.out.println(g); // Evite l'avertissement que g n'est pas utilisé
    }

    
    /*****************************************/
    /***        Auxiliary functions        ***/
    /*****************************************/

    private TimeTable TestReadTimeTable() throws IOException {
        TimeTable.Builder timeTableBuilder = new TimeTable.Builder();
        readStops(timeTableBuilder);
        readServices(timeTableBuilder);
        return timeTableBuilder.build();
    }

    private void readStops(TimeTable.Builder timeTableBuilder) throws IOException {
        BufferedReader stopsReader = streamForResource("stops.csv");
        String line;
        while ((line = stopsReader.readLine()) != null) {
            String[] fields = line.split(";");
            double lat = Double.parseDouble(fields[1]);
            double lon = Double.parseDouble(fields[2]);
            
            Stop newStop = new Stop(fields[0], new PointWGS84(Math.toRadians(lon), Math.toRadians(lat)));
            timeTableBuilder.addStop(newStop);
        }
        stopsReader.close();
    }

    private void readServices(TimeTable.Builder timeTableBuilder) throws IOException {
        Map<String, Service.Builder> serviceBuilders = new HashMap<>();

        readServiceGeneralRules(serviceBuilders);
        readServiceExceptions(serviceBuilders);

        for (Service.Builder b: serviceBuilders.values())
            timeTableBuilder.addService(b.build());
    }

    private void readServiceGeneralRules(Map<String, Service.Builder> serviceBuilders)
            throws IOException {
        BufferedReader calendarReader = streamForResource("calendar.csv");
        String line;
        while ((line = calendarReader.readLine()) != null) {
            String[] fields = line.split(";");
            Date startingDate = parseDate(fields[8]);
            Date endingDate = parseDate(fields[9]);

            Service.Builder b = new Service.Builder(fields[0], startingDate, endingDate);
            serviceBuilders.put(b.name(), b);
            for (Date.DayOfWeek dow: Date.DayOfWeek.values()) {
                if (fields[1 + dow.ordinal()].equals("1"))
                    b.addOperatingDay(dow);
            }
        }
        calendarReader.close();
    }

    private void readServiceExceptions(Map<String, Service.Builder> serviceBuilders)
            throws IOException {
        BufferedReader calendarDatesReader = streamForResource("calendar_dates.csv");
        String line;
        while ((line = calendarDatesReader.readLine()) != null) {
            String[] fields = line.split(";");
            Service.Builder b = serviceBuilders.get(fields[0]);
            switch (fields[2]) {
            case "1":
                b.addIncludedDate(parseDate(fields[1]));
                break;
            case "2":
                b.addExcludedDate(parseDate(fields[1]));
                break;
            }
        }
        calendarDatesReader.close();
    }

    private BufferedReader streamForResource(String fileName) {
        String resourceName = baseResourceName + fileName;
        return new BufferedReader(
                new InputStreamReader(
                        getClass().getResourceAsStream(resourceName),
                        StandardCharsets.UTF_8));
    }

    private static Date parseDate(String dateString) {
        return new Date(Integer.parseInt(dateString.substring(6, 8)),
                        Integer.parseInt(dateString.substring(4, 6)),
                        Integer.parseInt(dateString.substring(0, 4)));
    }
}

