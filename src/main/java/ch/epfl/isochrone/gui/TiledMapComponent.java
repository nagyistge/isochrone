package ch.epfl.isochrone.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import static java.lang.Math.pow;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import ch.epfl.isochrone.tiledmap.Tile;
import ch.epfl.isochrone.tiledmap.TileProvider;

/**
 * TiledMapComponent is a swing component capable of displaying
 * a tiled map whose tiles are provided by one or more tile providers.
 *
 * @author Jakob Bauer (223590)
 */
@SuppressWarnings("serial")
public final class TiledMapComponent extends JComponent {

    private final static int MAX_ZOOM = 19;
    private final static int MIN_ZOOM = 10;

    private int zoom;
    private final List<TileProvider> providers;

    /**
     * Class constructor.
     *
     * @param zoom  the initial zoom level (between 10 and 19).
     * @throws IllegalArgumentException if the zoom level is not
     *              between 10 and 19 (inclusive).
     */
    public TiledMapComponent(int zoom) {
        if ((zoom < MIN_ZOOM) || (zoom > MAX_ZOOM)) {
            throw new IllegalArgumentException("invalid zoom level");
        }
        this.zoom = zoom;
        this.providers = new ArrayList<TileProvider>();
    }

    /**
     * Returns the current zoom level.
     *
     * @return  the current zoom level.
     */
    public int zoom() { return zoom; }

    /**
     * Set zoom to new level. This will cause a redrawing
     * of the component.
     *
     * @param zoom  the new zoom level
     * @throws IllegalArgumentException if the zoom level is not
     *              between 10 and 19 (inclusive).
     */
    public void setZoom(int zoom) {
        if ((zoom < MIN_ZOOM) || (zoom > MAX_ZOOM)) {
            throw new IllegalArgumentException("invalid zoom level");
        }
        this.zoom = zoom;
        repaint();
    }

    /**
     * Add a new tile provider. This will cause a redrawing
     * of the component.
     *
     * @param tp    the tile provider to be added.
     * @return      the tiled map component with the new
     *              provider added to it.
     */
    public TiledMapComponent addTileProvider(TileProvider tp) {
        this.providers.add(tp);
        repaint();
        return this;
    }

    /**
     * Remove a tile provider from the tile provider list. This
     * will cause a redrawing of the component.
     *
     * @param tp    the tile provider to be removed.
     * @return      the tiled map component without the
     *              removed provider.
     */
    public TiledMapComponent removeTileProvider(TileProvider tp) {
        this.providers.remove(tp);
        repaint();
        return this;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        int dim = (int) pow(2, zoom + 8);
        return new Dimension(dim, dim);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(jawa.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;

        // First, the visible zone has to be determined
        Rectangle visible = getVisibleRect();

        // Second, the coordinates of the tiles to be drawn have to be determined
        int xTopLeft = visibleToTile(visible.x);
        int yTopLeft = visibleToTile(visible.y);
        int xBottomRight = visibleToTile(visible.x + visible.width);
        int yBottomRight = visibleToTile(visible.y + visible.height);

        // Third, the specified tiles have to be obtained from the providers
        // Fourth, the tiles have to be drawn using drawImage()
        for (TileProvider tp : providers) {
            for (int i = xTopLeft; i <= xBottomRight; i++) {
                for (int j = yTopLeft; j <= yBottomRight; j++) {
                    Tile tile = tp.tileAt(zoom, i, j);
                    g.drawImage(tile.image(), null, i * 256, j * 256);
                }
            }
        }
    }

    private int visibleToTile(int visibleCoordinate) {
        return visibleCoordinate / 256;
    }
}
