package ch.epfl.isochrone.tiledmap;

import ch.epfl.isochrone.geo.PointOSM;
import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Stop;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * Provides tiles for isochrone maps.
 *
 * @author Jakob Bauer (223590)
 */
public final class IsochroneTileProvider implements TileProvider {

    private final static int TILE_SIZE = 256;
    private final FastestPathTree tree;
    private final ColorTable colors;
    private final double walkingSpeed;

    /**
     * Class constructor.
     *
     * @param tree          the fastest-path tree.
     * @param colors        the colors for the fastest-path tree.
     * @param walkingSpeed  the walking speed in meters per second.
     */
    public IsochroneTileProvider(FastestPathTree tree, ColorTable colors, double walkingSpeed) {
        if (walkingSpeed <= 0) {
            throw new IllegalArgumentException("invalid walking speed");
        }
        this.tree = tree;
        this.colors = colors;
        this.walkingSpeed = walkingSpeed;
    }

    /* (non-Javadoc)
     * @see ch.epfl.isochrone.tiledmap.TileProvider#tileAt(int, int, int)
     */
    @Override
    public Tile tileAt(int zoom, int x, int y) {
        BufferedImage i = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = i.createGraphics();

        // set background (i.e. last color in ColorTable)
        g.setColor(colors.color(colors.numberOfTranches() - 1));
        g.fillRect(0, 0, i.getWidth(), i.getHeight());

        int duration = colors.duration();
        Set<Stop> stops = tree.stops();
        int startingTime = tree.startingTime();

        PointWGS84 p1 = (new PointOSM(zoom, tileToOSM(x), tileToOSM(y))).toWGS84();
        PointWGS84 p2 = (new PointOSM(zoom, tileToOSM(x), tileToOSM(y + 1))).toWGS84();
        double distanceInMeters = p1.distanceTo(p2);

        // pour chaque arret atteignable A :
        //      T = M - (H(A) - Hd)
        //      si T > 0 :
        //          R = distance, sur la carte, faisable a pied en un temps T
        //          dessiner un disque centre en P(A), de couleur C et rayon R
        //
        // (T : temps de marche restant a disposition apres etre arrive a l'arret A)
        // (il faut repeter ce algorithme pour chaque tranche de temps, en allant
        // de la derniere a la premiere! (sinon on perd les couches deja dessiner))
        for (int j = colors.numberOfTranches() - 1; j > 0 ; j--) {
            for (Stop s : stops) {
                int T = j * duration - (tree.arrivalTime(s) - startingTime);
                if (T > 0) {
                    double radius = T * walkingSpeed / distanceInMeters * i.getHeight();
                    g.setColor(colors.color(j - 1));
                    PointOSM positionOSM = s.position().toOSM(zoom);
                    double xCenter = (positionOSM.x() - tileToOSM(x));
                    double yCenter = (positionOSM.y() - tileToOSM(y));
                    g.fill(new Ellipse2D.Double(xCenter - radius, yCenter - radius, 2 * radius, 2 * radius));
                }
            }
        }
        return new Tile(zoom, x, y, i);
    }

    private int tileToOSM(int coordinateTile) {
        return coordinateTile * 256;
    }
}
