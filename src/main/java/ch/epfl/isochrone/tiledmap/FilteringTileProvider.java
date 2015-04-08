package ch.epfl.isochrone.tiledmap;

import java.awt.image.BufferedImage;

/**
 * Transformer of tile provider.
 *
 * @author Jakob Bauer (223590)
 */
public abstract class FilteringTileProvider implements TileProvider {

    private TileProvider providerToFilter;

    /**
     * Class constructor.
     *
     * @param providerToFilter  the provider that is to be filtered.
     */
    public FilteringTileProvider(TileProvider providerToFilter) {
        this.providerToFilter = providerToFilter;
    }

    /**
     * Transforms an individual pixel.
     *
     * @param argb  the color (in ARGB format) to be transformed.
     * @return      the transformed color (in ARGB).
     */
    abstract public int transformARGB(int argb);

    /* (non-Javadoc)
     * @see ch.epfl.isochrone.tiledmap.TileProvider#tileAt(int, int, int)
     */
    @Override
    public Tile tileAt(int zoom, int x, int y) {
        // FilteringTileProvider doit absolument creer une nouvelle image
        // plutot que de modifier celle du fournisseur qu'il transforme!
        BufferedImage image = providerToFilter.tileAt(zoom, x, y).image();
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                result.setRGB(i, j, transformARGB(image.getRGB(i, j)));
            }
        }
        return new Tile(zoom, x, y, result);
    }
}
