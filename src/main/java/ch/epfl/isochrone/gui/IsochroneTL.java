package ch.epfl.isochrone.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.epfl.isochrone.geo.PointOSM;
import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.tiledmap.CachedTileProvider;
import ch.epfl.isochrone.tiledmap.ColorTable;
import ch.epfl.isochrone.tiledmap.IsochroneTileProvider;
import ch.epfl.isochrone.tiledmap.OSMTileProvider;
import ch.epfl.isochrone.tiledmap.TileProvider;
import ch.epfl.isochrone.tiledmap.TransparentTileProvider;
import ch.epfl.isochrone.timetable.Date;
import ch.epfl.isochrone.timetable.Date.Month;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Graph;
import ch.epfl.isochrone.timetable.SecondsPastMidnight;
import ch.epfl.isochrone.timetable.Service;
import ch.epfl.isochrone.timetable.Stop;
import ch.epfl.isochrone.timetable.TimeTable;
import ch.epfl.isochrone.timetable.TimeTableReader;

/**
 * Main class.
 *
 * @author Jakob Bauer (223590)
 */
public final class IsochroneTL {
    private static final String OSM_TILE_URL = "http://b.tile.openstreetmap.org/";
    private static final int INITIAL_ZOOM = 11;
    private static final int MAX_ZOOM = 19;
    private static final int MIN_ZOOM = 10;
    private static final PointWGS84 INITIAL_POSITION = new PointWGS84(Math.toRadians(6.476), Math.toRadians(46.613));
    private static final String INITIAL_STARTING_STOP_NAME = "Lausanne-Flon";
    private static final int INITIAL_DEPARTURE_TIME = SecondsPastMidnight.fromHMS(6, 8, 0);
    private static final Date INITIAL_DATE = new Date(1, Month.OCTOBER, 2013);
    private static final int WALKING_TIME = 5 * 60;
    private static final double WALKING_SPEED = 1.25;
    private static final int SPM_THRESHOLD = SecondsPastMidnight.fromHMS(4, 0, 0);
    private static final double ALPHA = 0.5;

    private Date date;
    private Set<Service> services;
    private Set<Stop> stops;
    private TimeTable timeTable;
    private TimeTableReader timeTableReader;
    private Graph graph;
    private int departureTime;
    private Stop startingStop;
    private FastestPathTree fastestPathTree;
    private final TiledMapComponent tiledMapComponent;
    private TileProvider isoTileProvider;
    private final ColorTable colors;

    private Point oldMouseLocation;
    private Point oldViewPosition;

    /**
     * Class constructor.
     *
     * @throws IOException  if there is an IO problem.
     */
    public IsochroneTL() throws IOException {
        TileProvider bgTileProvider = new CachedTileProvider(new OSMTileProvider(new URL(OSM_TILE_URL)));
        this.tiledMapComponent = new TiledMapComponent(INITIAL_ZOOM);

        String baseResourceName = "/time-table/";
        this.timeTableReader = new TimeTableReader(baseResourceName);
        this.timeTable = timeTableReader.readTimeTable();
        this.stops = timeTable.stops();
        this.date = INITIAL_DATE;
        this.departureTime = INITIAL_DEPARTURE_TIME;
        this.services = timeTable.servicesForDate(INITIAL_DATE);
        this.graph = timeTableReader.readGraphForServices(stops, services, WALKING_TIME, WALKING_SPEED);
        for (Stop s : stops) {
            if (s.name().equals(INITIAL_STARTING_STOP_NAME)) {
                this.startingStop = s;
                break;
            }
        }
        this.fastestPathTree = graph.fastestPaths(startingStop, INITIAL_DEPARTURE_TIME);

        List<Color> colorlist = new ArrayList<Color>();
        colorlist.add(new Color((float) 1.0, (float) 0.0, (float) 0.0));
        colorlist.add(new Color((float) 1.0, (float) 0.5, (float) 0.0));
        colorlist.add(new Color((float) 1.0, (float) 1.0, (float) 0.0));
        colorlist.add(new Color((float) 0.5, (float) 1.0, (float) 0.0));
        colorlist.add(new Color((float) 0.0, (float) 1.0, (float) 0.0));
        colorlist.add(new Color((float) 0.0, (float) 0.5, (float) 0.5));
        colorlist.add(new Color((float) 0.0, (float) 0.0, (float) 1.0));
        colorlist.add(new Color((float) 0.0, (float) 0.0, (float) 0.5));
        colorlist.add(new Color((float) 0.0, (float) 0.0, (float) 0.0));
        this.colors = new ColorTable(WALKING_TIME, colorlist);

        this.isoTileProvider = new CachedTileProvider(new TransparentTileProvider(new IsochroneTileProvider(fastestPathTree, colors, WALKING_SPEED), ALPHA));
        this.tiledMapComponent.addTileProvider(bgTileProvider).addTileProvider(isoTileProvider);
    }

    /**
     * Set a new starting stop.
     *
     * @param stop  the new starting stop.
     */
    public void setStartingStop(Stop stop) {
        this.startingStop = stop;
        updateFastestPathTree();
        updateMap();
    }


    /**
     * Set the date and departure time.
     *
     * @param date          the date.
     * @param departureTime the departure time in SPM.
     */
    public void setDateAndTime(Date date, int departureTime) {
        // Cette methode verifie que la date recu differe effectivement de
        // la date actuelle, et, dans ce cas seulement, appelle la methode de mise
        // a jour des services, nomme p.ex. updateServices calcule l'ensemble des
        // services actifs pour la date actuelle et, si et seulement si cet ensemble
        // est differen de l'ensemble actuel, appelle la methode de mise a jour du
        // graphe, nomme p.ex. updateGraph. Et ainsi de suite.
        //
        // Pour tenir compte de cela lors du dessin des cartes isochrones, il faut
        // adapter les dates et les heures donnees par l'utilisateur dans la periode
        // allant de minuit a 4 heures du matin. Un tel couple date/heure doit etre
        // transforme pour que la date soit celle du jour avant, et l'heure comprise
        // entre 24 et 28h.
        Date newDate = date;
        int newDepartureTime = departureTime;
        if (departureTime <= SPM_THRESHOLD) {
            newDepartureTime = departureTime + SecondsPastMidnight.fromHMS(24, 0, 0);
            newDate = date.relative(-1);
        }

        if (this.date.compareTo(newDate) != 0)  {
            this.date = newDate;
            this.departureTime = newDepartureTime;
            updateServicesAndGraph();
        } else {
            this.departureTime = newDepartureTime;
            updateFastestPathTree();
            updateMap();
        }
    }

    private void updateServicesAndGraph() {
        Set<Service> newServices = timeTable.servicesForDate(date);
        if (!services.equals(newServices)) {
            this.services = newServices;
            try {
                this.graph = timeTableReader.readGraphForServices(stops, services, WALKING_TIME, WALKING_SPEED);
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateFastestPathTree();
            updateMap();
        }
    }

    private void updateFastestPathTree() {
        this.fastestPathTree = graph.fastestPaths(startingStop, departureTime);
    }

    private void updateMap() {
        TileProvider newIsoTileProvider = new CachedTileProvider(new TransparentTileProvider(new IsochroneTileProvider(fastestPathTree, colors, WALKING_SPEED), ALPHA));
        this.tiledMapComponent.removeTileProvider(isoTileProvider).addTileProvider(newIsoTileProvider);
        this.isoTileProvider = newIsoTileProvider;
    }

    private JComponent createCenterPanel() {
        final JViewport viewPort = new JViewport();
        viewPort.setView(tiledMapComponent);
        PointOSM startingPosOSM = INITIAL_POSITION.toOSM(tiledMapComponent.zoom());
        viewPort.setViewPosition(new Point(startingPosOSM.roundedX(), startingPosOSM.roundedY()));

        final JPanel copyrightPanel = createCopyrightPanel();

        final JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(400, 300));

        layeredPane.add(viewPort, new Integer(0));
        layeredPane.add(copyrightPanel, new Integer(1));

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                final Rectangle newBounds = layeredPane.getBounds();
                viewPort.setBounds(newBounds);
                copyrightPanel.setBounds(newBounds);

                viewPort.revalidate();
                copyrightPanel.revalidate();
            }
        });

        layeredPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Le premier auditeur detecte les pressions du bouton gauche
                // de la souris et memorise a ce moment deux informations:
                // 1) la position de la souris, obtenue p.ex. au moyen de la
                // methode getLocationOnScreen de l'evenement, et
                oldMouseLocation = e.getLocationOnScreen();
                // 2) la position de la carte dans l'aire d'affichage,
                // obtenue au moyen de la methode getViewPosition de JViewPort.
                oldViewPosition = viewPort.getViewPosition();
            }
        });

        layeredPane.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Le second auditeur detecte quant a lui les deplacements de la
                // souris avec le bouton gauche presse et, a chacun d'entre-eux,
                // il ajuste la position de la carte dans l'aire d'affichage, au
                // moyen de la methode setViewPosition de JViewPort, afin que la
                // carte suive la souris.
                Point newMouseLocation = e.getLocationOnScreen();
                int dx = oldMouseLocation.x - newMouseLocation.x;
                int dy = oldMouseLocation.y - newMouseLocation.y;
                oldViewPosition.translate(dx, dy);
                viewPort.setViewPosition(oldViewPosition);
            }
        });

        layeredPane.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // getPoint de MouseWheelEvent permet d'obtenir la position
                // du pointeur de la souris au moment ou la molette a ete
                // tournee, dans le systeme de coordonnes du composant
                // (ici layeredPane)
                Point oldPosition = e.getPoint();

                // A chaque cran doit correspondre un niveau de zoom, et un
                // nombre de crans positif doit provoquer une diminuation
                // du niveau de zoom, et inversement.
                int oldZoom = tiledMapComponent.zoom();
                int newZoom = oldZoom - e.getWheelRotation();
                if (newZoom < MIN_ZOOM) {
                    newZoom = MIN_ZOOM;
                } else if (newZoom > MAX_ZOOM) {
                    newZoom = MAX_ZOOM;
                }

                // Update tiledMapComponent
                tiledMapComponent.setZoom(newZoom);

                // convertPoint de SwingUtilities (methode statique), qui
                // permet de convertir un point entre les systemes de
                // coordonnes de deux composants.
                // (convertPoint gives us the coordinates of the point in OSM coordinates.)
                final Point oldPositionOSM = SwingUtilities.convertPoint(layeredPane, oldPosition, tiledMapComponent);
                viewPort.setViewSize(tiledMapComponent.getPreferredSize());

                // use shrinkFactor to find new distances
                double shrinkFactor = java.lang.Math.pow(2, newZoom - oldZoom);
                double newX = oldPositionOSM.getX() * shrinkFactor - e.getX();
                double newY = oldPositionOSM.getY() * shrinkFactor - e.getY();
                viewPort.setViewPosition(new Point((int) newX, (int) newY));
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(layeredPane, BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createCopyrightPanel() {
        Icon tlIcon = new ImageIcon(getClass().getResource("/images/tl-logo.png"));
        String copyrightText = "Données horaires 2013. Source : Transports publics de la région lausannoise / Carte : © contributeurs d'OpenStreetMap";
        JLabel copyrightLabel = new JLabel(copyrightText, tlIcon, SwingConstants.CENTER);
        copyrightLabel.setOpaque(true);
        copyrightLabel.setForeground(new Color(1f, 1f, 1f, 0.6f));
        copyrightLabel.setBackground(new Color(0f, 0f, 0f, 0.4f));
        copyrightLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 5, 0));

        JPanel copyrightPanel = new JPanel(new BorderLayout());
        copyrightPanel.add(copyrightLabel, BorderLayout.PAGE_END);
        copyrightPanel.setOpaque(false);
        return copyrightPanel;
    }

    // les elements graphiques permettant de selectionner les parametres de la carte
    // isochrone -- arret, date et heure de depart -- sont a placer dans un nouveau panneau
    // Swing (instance de JPanel).
    // Ce panneau, cree par une methode privee de la class IsochroneTL similaire a
    // createCenterPanel, doit avoir un gestionnaire de mise en page de type FlowLayout.
    // Une fois cree et initialise, le panneau doit etre ajoute dans la zone PAGE_START du
    // panneau de contenu de la fenetre. Pour memoire, ce dernier s'obtient au moyen
    // de la methode getContentPane de JFrame.
    @SuppressWarnings("deprecation")
    private JComponent createSelectionPanel() {
        // le panneau superieur contient un total de cinq fils qui sont, de gauche a droite:
        // 1) l'etiquette Depart
        final JLabel departureLabel = new JLabel("Départ");
        // 2) le menu des arrets de departs possibles
        final JComboBox<Stop> stopsMenu = new JComboBox<Stop>(new Vector<Stop>(stops));
        stopsMenu.setSelectedItem(startingStop);
        // 3) un mince separateur
        final JSeparator separator = new JSeparator();
        // 4) l'etiquette Date et heure
        final JLabel dateAndTimeLabel = new JLabel("Date et heure");
        // 5) le champ de selection de la date et de l'heure de depart
        final java.util.Date initialDate = INITIAL_DATE.toJavaDate();
        initialDate.setHours(SecondsPastMidnight.hours(INITIAL_DEPARTURE_TIME));
        initialDate.setMinutes(SecondsPastMidnight.minutes(INITIAL_DEPARTURE_TIME));
        initialDate.setSeconds(SecondsPastMidnight.seconds(INITIAL_DEPARTURE_TIME));
        final SpinnerDateModel spinnerDateModel = new SpinnerDateModel(initialDate, null, null, Calendar.DAY_OF_MONTH);
        final JSpinner dateAndTimeSelector = new JSpinner(spinnerDateModel);

        // l'element selectionne dans un composant de type JComboBox peut etre defini
        // au moyen de la methode setSelectedItem et obtenu au moyen de la methode
        // getSelectedItem. Pour etre informe d'un changement de cet element
        // selectionne, il faut attacher un auditeur de type ActionListener
        // au composant, via sa methode addActionListener.
        stopsMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStartingStop((Stop)stopsMenu.getSelectedItem());
            }
        });

        // pout etre averti d'un changement de la date et/ou l'heure, il faut attacher
        // un auditeur de type ChangeListener au modele (et pas au composant directement),
        // au moyen de sa methode addChangeListener. La date et l'heure du modele peuvent
        // etre obtenues au moyen de la methode getDate, qui les retourne sous la forme
        // d'une instance de java.util.Date.
        spinnerDateModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                java.util.Date newDate = spinnerDateModel.getDate();
                setDateAndTime(new Date(newDate), SecondsPastMidnight.fromJavaDate(newDate));
            }
        });

        JPanel selectionPanel = new JPanel(new FlowLayout());
        selectionPanel.add(dateAndTimeSelector, FlowLayout.LEFT);
        selectionPanel.add(dateAndTimeLabel, FlowLayout.LEFT);
        selectionPanel.add(separator, FlowLayout.LEFT);
        selectionPanel.add(stopsMenu, FlowLayout.LEFT);
        selectionPanel.add(departureLabel, FlowLayout.LEFT);
        return selectionPanel;
    }

    private void start() {
        JFrame frame = new JFrame("Isochrone TL");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(createCenterPanel(), BorderLayout.CENTER);

        // une fois cree et initialise, le panneau doit etre ajoute dans
        // la zone PAGE_START du panneau de contenu de la fenetre. Pour memoire,
        // ce dernier s'obtient au moyen de la methode getContentPane de JFrame.
        frame.getContentPane().add(createSelectionPanel(), BorderLayout.PAGE_START);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Main.
     *
     * @param args
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new IsochroneTL().start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
