package ch.epfl.isochrone.geo;

import static java.lang.Math.PI;
import static java.lang.Math.round;
import static java.lang.Math.pow;
import static java.lang.Math.atan;
import static java.lang.Math.sinh;

/**
 * A point in the OSM coordinate system.
 *
 * @author Jakob Bauer (223590)
 */
public final class PointOSM {

    private final int zoom;
    private final double x;
    private final double y;

    /**
     * Returns the maximum value for the x and y coordinates in the OSM
     * coordinate system for a given zoom level.
     *
     * @param zoom  the zoom level.
     * @return  the maximum value for the x and y coordinate.
     * @throws IllegalArgumentException if the zoom is negative.
     */
    public static int maxXY(int zoom) throws IllegalArgumentException {
        if (zoom < 0) {
            throw new IllegalArgumentException("zoom is negative");
        }
        return (int) pow(2, zoom + 8);
    }


    /**
     * Class constructor.
     *
     * @param zoom  the zoom level for the OSM coordinate system.
     * @param x     the x coordinate for the given zoom level.
     * @param y     the y coordinate for the given zoom level.
     * @throws IllegalArgumentException if the zoom is negative of if either
     *              the x or the y coordinate are not in the interval
     *              [0,maxXY], where maxXY is the maximum value for the x and y
     *              coordinate under the given zoom level.
     */
    public PointOSM(int zoom, double x, double y)
            throws IllegalArgumentException {
        if (zoom < 0) {
            throw new IllegalArgumentException("zoom is negative");
        }
        this.zoom = zoom;
        if ((x < 0) || (x > maxXY(zoom))) {
            throw new IllegalArgumentException(
                    "x is not in the interval [0,maxXY]: " + x);
        }
        if ((y < 0) || (y > maxXY(zoom))) {
            throw new IllegalArgumentException(
                    "y is not in the interval [0,maxXY]: " + y);
        }
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the point's x coordinate (not roundend).
     *
     * @return the unrounded x value.
     */
    public double x() { return x; }

    /**
     * Returns the point's y coordinate (not roundend).
     *
     * @return the unrounded y value.
     */
    public double y() { return y; }

    /**
     * Returns the point's x coordinate rounded to the nearest integer.
     *
     * @return the x value rounded to the nearest integer.
     */
    public int roundedX() { return (int) round(x); }

    /**
     * Returns the point's y coordinate rounded to the nearest integer.
     *
     * @return the y value rounded to the nearest integer.
     */
    public int roundedY() { return (int) round(y); }

    /**
     * Returns the point's zoom level.
     *
     * @return the zoom level.
     */
    public int zoom() { return zoom; }

    /**
     * Returns the same point but at a new zoom level.
     *
     * @param newZoom   the new zoom level.
     * @return          the point at the new zoom level.
     * @throws IllegalArgumentException if the new zoom level is negative.
     */
    public PointOSM atZoom(int newZoom) throws IllegalArgumentException {
        if (newZoom < 0) {
            throw new IllegalArgumentException("zoom is negative");
        }
        return new PointOSM(newZoom, x / pow(2, zoom - newZoom),
                            y / pow(2, zoom - newZoom));
    }

    /**
     * Returns the same point but in the WGS 84 coordinate system.
     *
     * @return the point in the WGS 84 coordinate system.
     * @throws IllegalArgumentException if the values for longitude and latitude
     *         that are passed to the constructor of the PointWGS84 class are
     *         outside the valid interval.
     */
    public PointWGS84 toWGS84() throws IllegalArgumentException {
        double s = pow(2, zoom + 8);
        double longitude = (2 * PI / s) * x - PI;
        double latitude = atan(sinh(PI - (2 * PI / s) * y));
        return new PointWGS84(longitude, latitude);
    }

    /**
     * Returns a string with the zoom, x and y coordinates in the format
     * (zoom,x,y)
     *
     * @return a string in the format (zoom,x,y)
     */
    @Override
    public String toString() {
        return "(" + zoom + "," + x + "," + y + ")";
    }
}
