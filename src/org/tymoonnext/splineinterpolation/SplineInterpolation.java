package org.tymoonnext.splineinterpolation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.JFrame;
import jpen.PButtonEvent;
import jpen.PKindEvent;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PScrollEvent;
import jpen.event.PenListener;
import jpen.owner.multiAwt.AwtPenToolkit;

/**
 * Simple test case for the Spline class
 * @author Shinmera
 */
public class SplineInterpolation extends JFrame implements PenListener{
    private Canvas canvas;
    private ArrayList<Double> x = new ArrayList<Double>();
    private ArrayList<Double> y = new ArrayList<Double>();
    private ArrayList<Double> p = new ArrayList<Double>();
    private ArrayList<Spline> s = new ArrayList<Spline>();
    private float lmx=-1, lmy=-1, cmx=0, cmy=0;
    private double len;
    
    static{
        try {
            //Load JPen native libraries.
            String path = new File(new File("."), "data").getAbsolutePath();
            System.setProperty("java.library.path", path);
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (Exception ex) {
            System.out.println("Failed to load native libraries! Cannot continue.");
            System.exit(1);
        }
    }
    
    public static void main(String[] args){SplineInterpolation main = new SplineInterpolation();}
    
    public SplineInterpolation(){
        x.add(20.0);
        y.add(20.0);
        p.add(0.0);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400,400));
        setSize(new Dimension(400,400));
        setLocationRelativeTo(null);
        canvas = new Canvas();
        add(canvas);
        
        AwtPenToolkit.addPenListener(canvas, this);
        AwtPenToolkit.getPenManager().pen.levelEmulator.setPressureTriggerForLeftCursorButton(0.5f);
        
        setVisible(true);
        repaint();
    }

    class Canvas extends Component{
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,  RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setBackground(Color.WHITE);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g2.clearRect(0, 0, getWidth(), getHeight());
            
            g2.setColor(Color.BLACK);
            for(Spline spline : s){
                double[] xi = spline.getIntX();
                double[] yi = spline.getIntY();
                double[] pi = spline.getInt(2);
                for(int i=0; i<xi.length; i++){
                    double len = pi[i]/2048.0*20;
                    g2.fill(new Ellipse2D.Double(xi[i]-len/2, yi[i]-len/2, len, len));
                }
            }

            for(int i=0;i<x.size();i++){
                g2.setColor(Color.RED);
                g2.fillOval((int)(x.get(i)-2), (int)(y.get(i)-2), 4, 4);

                g2.setColor(Color.CYAN);
                double len = p.get(i)/2048.0*20;
                g2.draw(new Ellipse2D.Double(x.get(i)-len/2, y.get(i)-len/2, len, len));
            }
        }
    }
    
    public void addSpline(){
        Spline spline = new Spline(convertToArray(x),convertToArray(y));
        spline.addPointDataArray(convertToArray(p));
        s.add(spline);
        
        x.clear();
        y.clear();
        p.clear();
    }
    
    public void mouseReleased(MouseEvent e){
        System.out.println("Adding Point ("+lmx+", "+lmy+", "+len+")");
        x.add((double)lmx);
        y.add((double)lmy);
        p.add(len);
        lmx=-1;lmy=-1;
        
        canvas.repaint();
    }
    
    public void penLevelEvent(PLevelEvent ev){
        if(!ev.isMovement())
            return;
        
        double pressure=ev.pen.getLevelValue(PLevel.Type.PRESSURE);
        len = pressure * 2048;
        
        if(len>2){
            cmx=ev.pen.getLevelValue(PLevel.Type.X);
            cmy=ev.pen.getLevelValue(PLevel.Type.Y);

            System.out.println("Adding Point ("+cmx+", "+cmy+", "+len+")");
            x.add((double)cmx);
            y.add((double)cmy);
            p.add(len);

            System.out.println("Rebuilding Spline...");
            
            
            canvas.repaint();
        }else if(x.size() > 0){
            addSpline();
            canvas.repaint();
        }
    }
    
    public void penKindEvent(PKindEvent arg0){}
    public void penButtonEvent(PButtonEvent arg0){}
    public void penScrollEvent(PScrollEvent arg0){}
    public void penTock(long availableMillis){}
    
    private double[] convertToArray(ArrayList<Double> l){double[] ds=new double[l.size()];for(int i=0;i<l.size();i++){ds[i]=l.get(i);}return ds;}
}
