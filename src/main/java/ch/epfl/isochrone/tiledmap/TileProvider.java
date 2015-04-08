package ch.epfl.isochrone.tiledmap;

/**
 * Provides tiles given a zoom factor and x and y coordinates.
 *
 * @author Jakob Bauer (223590)
 */
public interface TileProvider {

    /**
     * Returns the tile for the given coordinates and zoom factor.
     *
     * @param zoom  the zoom factor.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @return      the tile at the given coordinates and zoom factor.
     */
    Tile tileAt(int zoom, int x, int y);
}
