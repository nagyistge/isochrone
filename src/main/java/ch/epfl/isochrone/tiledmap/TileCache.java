package ch.epfl.isochrone.tiledmap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides a map-like interface to cache tiles.
 *
 * @author Jakob Bauer (223590)
 */
public class TileCache {

    private static final int MAX_SIZE = 100;
    private static final int MAX_ZOOM = 20;
    private static final int MAX_COORDINATE = 1048576;

    @SuppressWarnings("serial")
    private LinkedHashMap<Long, Tile> cache =
        new LinkedHashMap<Long, Tile>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Tile> e) {
                return size() > MAX_SIZE;
            }
        };

    /**
     * Adds a tile (value) for the given coordinates and
     * zoom level (key) to the map.
     *
     * @param zoom  the zoom level.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param tile  the tile.
     */
    public void put(int zoom, int x, int y, Tile tile) {
        if ((zoom < 0) || (zoom > MAX_ZOOM)) {
            throw new IllegalArgumentException("Invalid zoom level");
        }
        if ((x < 0) || (x > MAX_COORDINATE)) {
            throw new IllegalArgumentException("Invalid x coordinate");
        }
        if ((y < 0) || (y > MAX_COORDINATE)) {
            throw new IllegalArgumentException("Invalid y coordinate");
        }
        Long triplet = packCoordinates(zoom, x, y);
        cache.put(triplet, tile);
    }

    /**
     * Returns the tile associated with the given coordinates
     * and zoom level or null if no such tile exists.
     *
     * @param zoom  the zoom level.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @return      the tile associated with the coordinates and
     *              zoom level or null if no such tile exists.
     */
    public Tile get(int zoom, int x, int y) {
        if ((zoom < 0) || (zoom > MAX_ZOOM)) {
            throw new IllegalArgumentException("Invalid zoom level");
        }
        if ((x < 0) || (x > MAX_COORDINATE)) {
            throw new IllegalArgumentException("Invalid x coordinate");
        }
        if ((y < 0) || (y > MAX_COORDINATE)) {
            throw new IllegalArgumentException("Invalid y coordinate");
        }
        Long triplet = packCoordinates(zoom, x, y);
        return cache.get(triplet);
    }

    /**
     * Returns true if the cache contains a tile for the given
     * coordinates and zoom level.
     *
     * @param zoom  the zoom level.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @return      true if the cache contains the tile.
     */
    public boolean containsKey(int zoom, int x, int y) {
        Long triplet = packCoordinates(zoom, x, y);
        return cache.containsKey(triplet);
    }

    private Long packCoordinates(int zoom, int x, int y) {
        Long packedCoordinates = new Long(((zoom << 40) | (x << 20) | y));
        return packedCoordinates;
    }
}
