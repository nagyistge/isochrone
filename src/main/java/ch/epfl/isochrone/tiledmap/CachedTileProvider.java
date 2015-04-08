package ch.epfl.isochrone.tiledmap;

/**
 * Provides caching for a tile provider.
 *
 * @author Jakob Bauer (223590)
 */
public final class CachedTileProvider implements TileProvider {

    private final TileCache cache;
    private final TileProvider tileProvider;

    /**
     * Class constructor.
     *
     * @param tileProvider  the tile provider to be cached.
     */
    public CachedTileProvider(TileProvider tileProvider) {
        this.tileProvider = tileProvider;
        this.cache = new TileCache();
    }

    /* (non-Javadoc)
     * @see ch.epfl.isochrone.tiledmap.TileProvider#tileAt(int, int, int)
     */
    @Override
    public Tile tileAt(int zoom, int x, int y) {
        if (cache.containsKey(zoom, x, y)) {
            return cache.get(zoom, x, y);
        } else {
            Tile tile = tileProvider.tileAt(zoom, x, y);
            cache.put(zoom, x, y, tile);
            return tile;
        }
    }
}
