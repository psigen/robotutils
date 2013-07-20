/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package robotutils.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

/**
 * A display widget that can display a map background and icon overlays.  It
 * support mouse-based panning and zooming.
 * 
 * @author pkv
 */
public class MapPanel extends JPanel {
    public static final double SCALE_FACTOR = 1.2;
    public static final Stroke DEFAULT_STROKE = new BasicStroke(0.5f);
    public static final Color DEFAULT_COLOR = Color.BLACK;

    private final LinkedHashMap<String, MapIcon> _icons = new LinkedHashMap();
    private final LinkedHashMap<String, MapShape> _shapes = new LinkedHashMap();

    private final AffineTransform _mapTransform = new AffineTransform();
    private final MapMouseListener _mt = new MapMouseListener();

    private Point currPoint;
    private Point downPoint;
    
    {
        this.addMouseListener(_mt);
        this.addMouseMotionListener(_mt);
        this.addMouseWheelListener(_mt);
    }

    private class MapIcon {
        Image icon;
        final AffineTransform xform = new AffineTransform();
    }

    private class MapShape {
        Color color;
        Stroke stroke;
        Shape shape;
        final AffineTransform xform = new AffineTransform();
    }

    /**
     * Sets the boundary points for the window in terms of the map coordinates.
     * These values can be reversed to flip the map view (e.g. left &gt right).
     * This function will only have an effect after the panel initialized to a
     * non-zero size.
     *
     * @param left the map coordinate of the left side of the window
     * @param right the map coordinate of the right side of the window
     * @param top the map coordinate of the top of the window
     * @param bottom the map coordination of the bottom of the window
     */
    public void setView(double left, double right, double top, double bottom) {
        setView(new Rectangle2D.Double(left, top, right - left, bottom - top));
    }

    /**
     * @see MapPanel#setView(double, double, double, double) 
     *
     * @param bounds A rectangle representing the boundary of the window
     */
    public void setView(Rectangle2D bounds) {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) return;

        _mapTransform.setToIdentity();
        _mapTransform.scale(this.getWidth()/bounds.getWidth(), this.getHeight()/bounds.getHeight());
        _mapTransform.translate(-bounds.getMinX(), -bounds.getMinY());
    }

    /**
     * @see MapPanel#setShape(java.lang.String, java.awt.Shape, java.awt.geom.AffineTransform, java.awt.Color, java.awt.Stroke)
     *
     * @param name a name for the shape
     * @param shape an object represeting the shape to be drawn
     * @param xform the transformation of the shape to map coordinates
     */
    public void setShape(String name, Shape shape, AffineTransform xform) {
        setShape(name, shape, xform, DEFAULT_COLOR);
    }

    /**
     * @see MapPanel#setShape(java.lang.String, java.awt.Shape, java.awt.geom.AffineTransform, java.awt.Color, java.awt.Stroke)
     *
     * @param name a name for the shape
     * @param shape an object represeting the shape to be drawn
     * @param xform the transformation of the shape to map coordinates
     * @param color the color with which to draw the shape
     */
    public void setShape(String name, Shape shape, AffineTransform xform, Color color) {
        setShape(name, shape, xform, color, DEFAULT_STROKE);
    }

    /**
     * @see MapPanel#setShape(java.lang.String, java.awt.Shape, java.awt.geom.AffineTransform, java.awt.Color, java.awt.Stroke)
     *
     * @param name a name for the shape
     * @param shape an object represeting the shape to be drawn
     * @param xform the transformation of the shape to map coordinates
     * @param stroke the stroke width to use when drawing the shape
     */
    public void setShape(String name, Shape shape, AffineTransform xform, Stroke stroke) {
        setShape(name, shape, xform, DEFAULT_COLOR, stroke);
    }

    /**
     * Sets up a named shape to be drawn using the provided transformation.  If
     * the shape is already transformed correctly, the identity transform can be
     * passed in, or null can be passed in.
     *
     * @param name a name for the shape
     * @param shape an object represeting the shape to be drawn
     * @param xform the transformation of the shape to map coordinates
     * @param color the color with which to draw the shape
     * @param stroke the stroke width to use when drawing the shape
     */
    public void setShape(String name, Shape shape, AffineTransform xform, Color color, Stroke stroke) {
        MapShape ms = getShape(name);

        ms.color = color;
        ms.stroke = stroke;
        
        if (xform != null) {
            ms.shape = xform.createTransformedShape(shape);
            ms.xform.setTransform(xform);
        } else {
            ms.shape = shape;
        }

        this.repaint();
    }

    /**
     * Sets up a named icon to be drawn using the provided boundaries.  The icon
     * will be drawn such that its boundaries match the rectangle exactly.  If
     * the rectangle has negative width or height, the icon will be flipped
     * accordingly.
     *
     * @param name a name for the icon
     * @param img an image for the icon
     * @param bounds a box specifying the map coordinates of the icon's edges
     */
    public void setIcon(String name, Image img, Rectangle2D bounds) {
        MapIcon mi = getIcon(name);
        mi.icon = img;
        
        mi.xform.setToIdentity();
        mi.xform.translate(bounds.getX(), bounds.getY());
        mi.xform.scale(bounds.getWidth(), bounds.getHeight());
        mi.xform.scale(1.0/img.getWidth(null), 1.0/img.getHeight(null));
        this.repaint();
    }

    /**
     * Sets up a named icon to be drawn using the provided transformation.  The
     * specified transform should be from the map frame to the image frame (in
     * pixels).
     *
     * @param name a name for the icon
     * @param img an image for the icon
     * @param xform a transform from map coordinates to image coordinates
     */
    public void setIcon(String name, Image img, AffineTransform xform) {
        MapIcon mi = getIcon(name);
        mi.icon = img;
        mi.xform.setTransform(xform);
        this.repaint();
    }

    private MapIcon getIcon(String name) {
        synchronized(_icons) {
            if (_icons.containsKey(name)) {
                return _icons.get(name);
            } else {
                MapIcon mi = new MapIcon();
                _icons.put(name, mi);
                return mi;
            }
        }
    }
    
    private MapShape getShape(String name) {
        synchronized(_shapes) {
            if (_shapes.containsKey(name)) {
                return _shapes.get(name);
            } else {
                MapShape ms = new MapShape();
                _shapes.put(name, ms);
                return ms;
            }
        }
    }

    /**
     * Deletes a particular named icon from the panel.
     *
     * @param name the name of the icon to delete
     */
    public void removeIcon(String name) {
        synchronized(_icons) {
            _icons.remove(name);
            this.repaint();
        }
    }

    /**
     * Deletes a particular named shape from the panel.
     *
     * @param name the name of the shape to delete
     */
    public void removeShape(String name) {
        synchronized(_shapes) {
            _shapes.remove(name);
            this.repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Apply transform from frame coords to image coords
        if (downPoint != null && currPoint != null) {
            g2.translate(currPoint.getX() - downPoint.getX(),
                    currPoint.getY() - downPoint.getY());
        }
        
        // Apply transform from image coords to map coords
        g2.transform(_mapTransform);

        // Draw overlays in the window
        synchronized(_icons) {
            for (MapIcon mi : _icons.values()) {
                drawIcon(mi, g2);
            }
        }

        synchronized(_shapes) {
            for (MapShape ms : _shapes.values()) {
                drawShape(ms, g2);
            }
        }

        g2.dispose();
    }

    private void drawIcon(MapIcon mi, Graphics2D g) {
        if (mi.icon == null) return;
        g.drawImage(mi.icon, mi.xform, null);
    }

    private void drawShape(MapShape ms, Graphics2D g) {
        if (ms.shape == null) return;
        
        Color c = g.getColor();
        Stroke s = g.getStroke();

        g.setColor(ms.color);
        g.setStroke(ms.stroke);
        g.draw(ms.shape);
        
        g.setStroke(s);
        g.setColor(c);
    }

    /**
     * Override this method to implement a handler for mouse clicks.  By default
     * it simply prints the coordinates of the click.
     *
     * @param x The x-position (left-right) in the map frame.
     * @param y The y-position (up-down) in the map frame.
     */
    protected void onClick(double x, double y, int button, int numClicks) {
        System.out.println("Click: [" + x + ", " + y + "]");
    }
    
    private class MapMouseListener implements MouseInputListener, MouseWheelListener {

        public void mouseDragged(MouseEvent e) {
            currPoint = e.getPoint();
            MapPanel.this.repaint();
        }

        public void mouseMoved(MouseEvent e) {
            currPoint = e.getPoint();
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            double exp = Math.pow(SCALE_FACTOR, e.getWheelRotation());

            AffineTransform at = new AffineTransform();
            at.translate(currPoint.x, currPoint.y);
            at.scale(exp, exp);
            at.translate(-currPoint.x, -currPoint.y);

            _mapTransform.preConcatenate(at);
            MapPanel.this.repaint();
        }

        public void mouseClicked(MouseEvent evt) {
            Point2D mousePos = new Point2D.Double(evt.getX(), evt.getY());

            AffineTransform at = new AffineTransform();
            if (downPoint != null && currPoint != null) {
                at.translate(currPoint.getX() - downPoint.getX(),
                        currPoint.getY() - downPoint.getY());
            }
            
            Point2D map = null;
            try {
                map = at.inverseTransform(mousePos, null);
                map = _mapTransform.inverseTransform(map, null);
            } catch (NoninvertibleTransformException e) {}

            onClick(map.getX(), map.getY(), evt.getButton(), evt.getClickCount());
        }

        public void mousePressed(MouseEvent e) {
            downPoint = e.getPoint();
        }

        public void mouseReleased(MouseEvent e) {
            double dx = (e.getPoint().getX() - downPoint.getX());
            double dy = (e.getPoint().getY() - downPoint.getY());
            _mapTransform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
            downPoint = null;
            MapPanel.this.repaint();
        }

        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    }
}
