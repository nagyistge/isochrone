package ch.epfl.isochrone.tiledmap;

import java.awt.image.BufferedImage;

/**
 * A map tile represented by its coordinates, its zoom factor,
 * and its image.
 *
 * @author Jakob Bauer (223590)
 */
public final class Tile {

    private final int zoom;
    private final double x;
    private final double y;
    private final BufferedImage image;

    /**
     * Class constructor.
     *
     * @param zoom  the zoom factor.
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param image the map image.
     */
    public Tile(int zoom, double x, double y, BufferedImage image) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
        this.image = image;
    }

    /**
     * Returns the image of the tile.
     *
     * @return  the image of the tile.
     */
    public BufferedImage image() { return image; }
}
