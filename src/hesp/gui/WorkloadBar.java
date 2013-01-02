package hesp.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

/**
 * @author marcinlos
 * 
 * Custom component for displaying workload with nice gradient.
 */
public class WorkloadBar extends JPanel {
    
    private int maximum;
    private int value;
    
    public WorkloadBar(int value, int maximum) {
        this.value = value;
        this.maximum = maximum;
        setMinimumSize(new Dimension(1, 18));
        setPreferredSize(new Dimension(1, 18));
    }
    
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D gfx = (Graphics2D) g;
        
        // Leave space for some padding
        int w = getWidth() - 2;
        int h = getHeight() - 2;
        
        GradientPaint gradient = new GradientPaint(0, 0, Color.GREEN, 
                w, 0, Color.RED);
        
        int rw = w * value / maximum;

        float[] points = {0f, 0.5f, 1f};
        Color[] colors = {Color.GREEN, Color.YELLOW, Color.RED};
        gfx.setPaint(new LinearGradientPaint(0, 0, w, 0, points, colors));
        
        //gfx.setPaint(gradient);
        
        /*Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(50, 50);
        float[] dist = {0.0f, 0.2f, 1.0f};
        Color[] colors = {Color.RED, Color.WHITE, Color.BLUE};
        LinearGradientPaint p =
            new LinearGradientPaint(start, end, dist, colors);*/

        //gfx.setPaint(p);
        gfx.fillRoundRect(1, 1, rw, h, 4, 4);

        // Border
        gfx.setColor(Color.BLACK);
        gfx.drawRoundRect(1, 1, w, h, 4, 4);
        
    }
    
    /**
     * Sets the value of current workload and updates the component.
     * 
     * @param value Value of current workload
     */
    public void setValue(int value) {
        this.value = value;
        revalidate();
        repaint();
    }
    
    /**
     * Sets the upper limit of workload.
     * 
     * @param max Maximum workload
     */
    public void setMaxValue(int max) {
        this.maximum = max;
        revalidate();
        repaint();
    }

}
