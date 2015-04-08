package ch.epfl.isochrone.geo;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toDegrees;
import static java.lang.Math.pow;
import static ch.epfl.isochrone.math.Math.haversin;
import static ch.epfl.isochrone.math.Math.asinh;

/**
 * A point in the WGS 84 coordinate system.
 *
 * @author Jakob Bauer (223590)
 */
public final class PointWGS84 {

    private static final double RAYON_TERRE = 6378137;
    private final double latitude;
    private final double longitude;

    /**
     * Class constructor
     *
     * @param longitude longitude of the point expressed in radians.
     * @param latitude  latitude of the point expressed in radians.
     * @throws IllegalArgumentException if longitude is not in the interval
     *         [-pi,pi] or if the latitude is not in the interval [-pi/2,pi/2].
     */
    public PointWGS84(double longitude, double latitude)
            throws IllegalArgumentException {
        if ((longitude > PI) || (longitude < -PI)) {
            throw new IllegalArgumentException(
                    "longitude not in interval [-pi,pi]: " + longitude);
        }
        if ((latitude > PI / 2.0) || (latitude < -PI / 2.0)) {
            throw new IllegalArgumentException(
                    "latitude not in interval [-pi/2,pi/2]: " + latitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns the point's longitude in radians.
     *
     * @return  the longitude in radians.
     */
    public double longitude() { return longitude; }

    /**
     * Returns the point's latitude in radians.
     *
     * @return  the latitude in radians.
     */
    public double latitude() { return latitude; }

    /**
     * Returns the distance of of the point "that" to this point.
     *
     * @param that the point to which the distance shall be calculated.
     * @return  the distance of "that" point to this point.
     */
    public double distanceTo(PointWGS84 that) {
        return 2 * RAYON_TERRE * asin(sqrt(haversin(this.latitude - that.latitude)
               + cos(this.latitude) * cos(that.latitude)
               * haversin(this.longitude - that.longitude)));
    }

    /**
     * Returns the same point but in the OSM coordinate system.
     *
     * @param zoom  the zoom level in the OSM coordinate system.
     * @return the point in the OSM coordinate system with the given zoom.
     */
    public PointOSM toOSM(int zoom) {
        double s = pow(2, zoom + 8);
        double x = (s / (2 * PI)) * (longitude + PI);
        double y = (s / (2 * PI)) * (PI - asinh(tan(latitude)));
        return new PointOSM(zoom, x, y);
    }

    /**
     * Returns a string with the longitude and the latitude of the point in
     * degrees in the format (longitude,latitude)
     *
     * @return a string of longitude and latitude
     */
    @Override
    public String toString() {
        return "(" + toDegrees(longitude) + "," + toDegrees(latitude) + ")";
    }
}
