package ch.epfl.isochrone.tiledmap;

/**
 * TransparentTileProvider is a tile transformer that changes
 * the transparency (alpha value) of a tile.
 *
 * @author Jakob Bauer (223590)
 */
public final class TransparentTileProvider extends FilteringTileProvider {

    private final double alpha;

    /**
     * Class constructor.
     *
     * @param alpha             the alpha value.
     * @param providerToFilter  the provider to be made transparent.
     * @throws IllegalArgumentException if the alpha value is not
     *                          between 0 and 1.
     */
    public TransparentTileProvider(TileProvider providerToFilter, double alpha) {
        super(providerToFilter);
        if ((alpha < 0) || (alpha > 1)) {
            throw new IllegalArgumentException("invalid alpha value");
        }
        this.alpha = alpha;
    }

    /* (non-Javadoc)
     * @see ch.epfl.isochrone.tiledmap.FilteringTileProvider#transformARGB(int)
     */
    @Override
    public int transformARGB(int argb) {
        int alphaInt = (int)(255 * alpha);
        return (argb & 0x00FFFFFF) | (alphaInt << 24);
    }
}
