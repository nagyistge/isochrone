package ch.epfl.isochrone.timetable;

import ch.epfl.isochrone.geo.PointWGS84;

/**
 * A stop with a name and a position.
 *
 * @author Jakob Bauer (223590)
 */
public final class Stop {

    private final String name;
    private final PointWGS84 position;

    /**
     * Class constructor.
     *
     * @param name      the name of the stop.
     * @param position  the position of the stop expressed
     *                  in the WGS 84 coordinate system.
     */
    public Stop(String name, PointWGS84 position) {
        this.name = name;
        this.position = position;
    }

    /**
     * Returns the name of the stop.
     *
     * @return  the name of the stop.
     */
    public String name() { return name; }

    /**
     * Returns the position of the stop expressed in the WGS 84
     * coordinate system.
     *
     * @return  the position of the stop as a PointWGS84.
     */
    public PointWGS84 position() { return position; }

    /**
     * Returns the name of the stop.
     *
     * @return  the name of the stop.
     */
    @Override
    public String toString() { return name; }
}
