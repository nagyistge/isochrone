package ch.epfl.isochrone.tiledmap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * ColorTable represents a table of colors which can be used
 * to draw an isochrone map.
 *
 * @author Jakob Bauer (223590)
 */
public final class ColorTable {

    private final int duration;
    private final List<Color> colors;

    /**
     * Class constructor.
     *
     * @param duration  the duration of the tranche in seconds.
     * @param colors    a list with the colors to be used for the tranches;
     *                  the first element is used for the first tranche, the
     *                  second element for the second tranche etc.
     *                  (the last element contains the color for all the
     *                  points that are not reachable in the given time.)
     * @throws IllegalArgumentException if the duration is not strictly
     *                  positive or if the colors list is empty.
     */
    public ColorTable(int duration, List<Color> colors) {
        if (duration <= 0) {
            throw new IllegalArgumentException("invalid duration");
        }
        if (colors.isEmpty()) {
            throw new IllegalArgumentException("color list is empty");
        }
        this.duration = duration;
        this.colors = new ArrayList<Color>(colors);
    }

    /**
     * Returns the duration of the tranches.
     *
     * @return  the duration of the tranches in seconds.
     */
    public int duration() { return duration; }

    /**
     * Returns the number of tranches.
     *
     * @return  the number of tranches.
     */
    public int numberOfTranches() { return colors.size(); }

    /**
     * Returns the color associated with a given tranche.
     *
     * @param tranche   the tranche whose color is being returned.
     * @return          the color associated with the given tranche.
     */
    public Color color(int tranche) { return colors.get(tranche); }
}
