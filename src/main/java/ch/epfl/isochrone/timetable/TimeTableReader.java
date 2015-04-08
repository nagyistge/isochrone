package ch.epfl.isochrone.timetable;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import ch.epfl.isochrone.geo.PointWGS84;

/**
 * TimeTableReader allows to parse the documents containing the
 * transporation schedule and to write this information to memory.
 *
 * @author Jakob Bauer (223590)
 */
public final class TimeTableReader {

    private final String baseResourceName;
    private final String stopsName;
    private final String stopTimesName;
    private final String calendarName;
    private final String calendarDatesName;

    /**
     * Class constructor.
     *
     * @param baseResourceName  the prefix of the location of the
     *                          resource files.
     */
    public TimeTableReader(String baseResourceName) {
        this.baseResourceName   = baseResourceName;
        this.stopsName          = "stops.csv";
        this.stopTimesName      = "stop_times.csv";
        this.calendarName       = "calendar.csv";
        this.calendarDatesName  = "calendar_dates.csv";
    }

    /**
     * Returns a TimeTable with the stops and services that are stored
     * in the corresponding resource files.
     *
     * @return              a TimeTable based on the information stored
     *                      in the resource files.
     * @throws IOException  if there is an IO or formatting problem.
     */
    public TimeTable readTimeTable() throws IOException {
        TimeTable.Builder timeTableBuilder = new TimeTable.Builder();
        readStops(timeTableBuilder);
        readCalendarDates(timeTableBuilder);
        return timeTableBuilder.build();
    }

    /**
     * Returns a graph for the stops and services passed as arguments.
     *
     * @param stops         the stops that are considered for the graph.
     * @param services      the services that are considered for the graph.
     * @param walkingTime   the maximum walking time to be taken into
     *                      consideration.
     * @param walkingSpeed  the walking speed in meters per second.
     * @return              the graph for the stops and services in question.
     * @throws IOException  if there is an IO or formatting problem.
     */
    public Graph readGraphForServices(Set<Stop> stops, Set<Service> services,
            int walkingTime, double walkingSpeed) throws IOException {
        Graph.Builder graphBuilder = new Graph.Builder(stops);
        readStopTimes(graphBuilder, stops, services);
        graphBuilder.addAllWalkEdges(walkingTime, walkingSpeed);
        return graphBuilder.build();
    }

    private Map<String, Service.Builder> readCalendar() throws IOException {
        String resourceName = baseResourceName + calendarName;
        BufferedReader reader = openReader(resourceName);
        Map<String, Service.Builder> services = new HashMap<String, Service.Builder>();
        String line;
        while ((line = reader.readLine()) != null) {
            /* 2013-SU-Semaine-50-0000100;0;0;0;0;1;0;0;20130923;20131213 */
            /* 2013-SU-Semaine-50-1111000;1;1;1;1;0;0;0;20130923;20131213 */
            /* 2013-SU-Semaine-50-0010000;0;0;1;0;0;0;0;20130923;20131213 */
            String[] fields = line.split(";");
            if (fields.length != 10) {
                throw new IOException("Error while reading "
                        + resourceName + "; wrong number of fields");
            }
            String name = fields[0];
            Date startingDate = extractDate(fields[8]);
            Date endingDate = extractDate(fields[9]);
            Service.Builder service =
                new Service.Builder(name, startingDate, endingDate);
            int[] operatingDays = new int[7];
            for (int k = 0; k < 7; k++) {
                operatingDays[k] = Integer.parseInt(fields[k+1]);
            }
            for (int k = 0; k < operatingDays.length; k++) {
                if (operatingDays[k] == 1) {
                    Date.DayOfWeek dayOfWeek = intToDay(k+1);
                    service.addOperatingDay(dayOfWeek);
                }
            }
            services.put(service.name(), service);
        }
        reader.close();
        return services;
    }

    private void readCalendarDates(TimeTable.Builder timeTableBuilder)
            throws IOException {
        String resourceName = baseResourceName + calendarDatesName;
        BufferedReader reader = openReader(resourceName);
        Map<String, Service.Builder> services = readCalendar();
        String line;
        while ((line = reader.readLine()) != null) {
            /* 2013-SU-Semaine-50-0000100;20131014;2 */
            /* 2013-SU-Semaine-50-1111000;20131014;2 */
            /* 2013-SU-Semaine-50-0010000;20131014;2 */
            String[] fields = line.split(";");
            if (fields.length != 3) {
                throw new IOException("Error while reading "
                        + resourceName + "; wrong number of fields");
            }
            String name = fields[0];
            Date date = extractDate(fields[1]);
            int exceptionType = Integer.parseInt(fields[2]);
            if (exceptionType == 1) {
                services.get(name).addIncludedDate(date);
            } else if (exceptionType == 2) {
                services.get(name).addExcludedDate(date);
            } else {
                throw new IOException("Invalid exception type: "
                        + exceptionType);
            }
        }
        reader.close();

        for (Service.Builder sb : services.values()) {
            timeTableBuilder.addService(sb.build());
        }
    }

    private void readStops(TimeTable.Builder timeTableBuilder)
            throws IOException {
        String resourceName = baseResourceName + stopsName;
        BufferedReader reader = openReader(resourceName);
        String line;
        while ((line = reader.readLine()) != null) {
            /* 1er Août;46.5367366879;6.58201906962 */
            /* 1er Mai;46.5407686803;6.58344370604 */
            /* Abeilles;46.5411232548;6.64799239616 */
            String[] fields = line.split(";");
            if (fields.length != 3) {
                throw new IOException("Error while reading "
                        + resourceName + "; wrong number of fields");
            }
            String name = fields[0];
            double latitude = Math.toRadians(Double.parseDouble(fields[1]));
            double longitude = Math.toRadians(Double.parseDouble(fields[2]));
            PointWGS84 position = new PointWGS84(longitude, latitude);
            Stop stop = new Stop(name, position);
            timeTableBuilder.addStop(stop);
        }
        reader.close();
    }

    private void readStopTimes(Graph.Builder graphBuilder, Set<Stop> stops,
            Set<Service> services) throws IOException {
        Map<String, Stop> stopMap = new HashMap<String, Stop>();
        for (Stop s : stops) {
            stopMap.put(s.name(), s);
        }
        Set<String> stopNames = new HashSet<String>(stopMap.keySet());
        Set<String> serviceNames = new HashSet<String>();
        for (Service s : services) {
            serviceNames.add(s.name());
        }

        String resourceName = baseResourceName + stopTimesName;
        BufferedReader reader = openReader(resourceName);
        String line;
        while ((line = reader.readLine()) != null) {
            /* 2013-SU-Semaine-50-0000100;Croisettes;87120;Vennes;87181 */
            /* 2013-SU-Semaine-50-0000100;Vennes;87209;Fourmi;87293 */
            /* 2013-SU-Semaine-50-0000100;Fourmi;87316;Sallaz;87417 */
            String[] fields = line.split(";");
            if (fields.length != 5) {
                throw new IOException("Error while reading " + resourceName
                        + "; wrong number of fields: " + fields.length);
            }
            String serviceName  = fields[0];
            String fromStopName = fields[1];
            String toStopName   = fields[3];
            if (serviceNames.contains(serviceName)
                    && stopNames.contains(fromStopName)
                    && stopNames.contains(toStopName)) {
                Stop fromStop   = stopMap.get(fromStopName);
                Stop toStop     = stopMap.get(toStopName);
                int departureTime   = Integer.parseInt(fields[2]);
                int arrivalTime     = Integer.parseInt(fields[4]);
                graphBuilder.addTripEdge(fromStop, toStop,
                        departureTime, arrivalTime);
            }
        }
        reader.close();
    }

    private BufferedReader openReader(String resourceName) {
        InputStream inStream = getClass().getResourceAsStream(resourceName);
        return new BufferedReader(new
                InputStreamReader(inStream, StandardCharsets.UTF_8));
    }

    private Date extractDate(String dateString) {
        /* 20130923;20131213 */
        int year    = Integer.parseInt(dateString.substring(0, 4));
        int month   = Integer.parseInt(dateString.substring(4, 6));
        int day     = Integer.parseInt(dateString.substring(6, 8));
        return new Date(day, month, year);
    }

    private Date.DayOfWeek intToDay(int d) throws IllegalArgumentException {
        if ((d < 1) || (d > 7)) {
            throw new IllegalArgumentException("d is not in the interval [1;7]");
        }
        return Date.DayOfWeek.values()[d-1];
    }
}
