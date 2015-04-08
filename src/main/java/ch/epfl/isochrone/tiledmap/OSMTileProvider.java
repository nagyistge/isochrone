package ch.epfl.isochrone.tiledmap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Provides tiles obtained from an OpenStreetMap server.
 *
 * @author Jakob Bauer (223590)
 */
public final class OSMTileProvider implements TileProvider {

    private final URL baseURL;
    private final BufferedImage errorTile;

    /**
     * Class constructor.
     *
     * @param baseURL       the URL of the OpenStreetMap server.
     * @throws IOException  if there is an IO problem.
     */
    public OSMTileProvider(URL baseURL) throws IOException {
        this.baseURL = baseURL;
        this.errorTile = ImageIO.read(getClass().getResource("/images/error-tile.png"));
    }

    /**
     * Class constructor.
     *
     * @param baseURL       the URL of the OpenStreetMap server.
     * @throws IOException  if there is an IO problem.
     */
    public OSMTileProvider(String baseURL) throws IOException {
        this(new URL(baseURL));
    }

    /* (non-Javadoc)
     * Returns a tile with an error message in case the
     * server cannot be queried.
     * @see ch.epfl.isochrone.tiledmap.TileProvider#tileAt(int, int, int)
     */
    @Override
    public Tile tileAt(int zoom, int x, int y) {
        BufferedImage image = null;
        String extension = zoom + "/" + x + "/" + y + ".png";
        try {
            URL imageURL = new URL(baseURL, extension);
            image = ImageIO.read(imageURL);
        } catch(IOException e) {
            image = errorTile;
        }
        return new Tile(zoom, x, y, image);
    }
}
