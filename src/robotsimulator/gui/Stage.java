package robotsimulator.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import robotsimulator.RobotSimulator;
import robotsimulator.Simulator;
import robotsimulator.robot.SonarSensor;
import robotsimulator.world.CellTheme;
import robotsimulator.world.CellType;
import robotsimulator.world.Point;
import robotsimulator.world.World;
import robotsimulator.worldobject.Block;

@SuppressWarnings("serial")
/*
 * Represents the space the robot moves around in
 * Controls rendering the robot
 */
public class Stage extends JPanel implements MouseListener, Runnable, Scrollable
{
    private Simulator sim;
    private Thread animator;
    private int width, height, fps;
    //Out-of-bounds color and background color
    private Color oobColor = new Color(71, 79, 97);
    private Color bColor = new Color(238, 239, 242);

    public Stage(int w, int h, int f, Simulator s)
    {
            sim = s;
            width = w;
            height = h;
            fps = f;
            this.addMouseListener(this);

            setDoubleBuffered(true);
            setPreferredSize(new Dimension(w, h));
            setSize(w, h);
            setBackground(oobColor);
    }
    
    public void resizeStage(World w)
    {
        width = w.getGridWidth();
        height = w.getGridHeight();
    }

    public void addNotify() 
    {
        super.addNotify();
        animator = new Thread(this);
        animator.start();
    }
	
    //Draws the stage, robot, and each block
    public void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics;

        //Draw the usable area
        g.setPaint(bColor);
        g.fill(sim.getWorld().getBoundary());
        //Draw the boundary
        g.setPaint(Color.black);
        g.setStroke(new BasicStroke(3f));
        g.draw(sim.getWorld().getBoundary());

        ArrayList<Block> blocks = sim.getWorld().getBlocks();
        for(Block b : blocks)
        {
            paintBlock(g, b, b.getColor());
        }

        paintRobot(g, sim.getRobot().getBlock(), Color.RED);
        paintSonarSensors(g);
    }

    //Draws a block on the stage
    private void paintBlock(Graphics2D g, Block b, Color c)
    {
        g.setColor(c);
        AffineTransform at = AffineTransform.getRotateInstance(b.getRadAngle() - (Math.PI / 2), b.getCenterX(), b.getCenterY());  
        g.fill(at.createTransformedShape(b.getRect()));


        CellType ct = b.getCellType();
        if(ct != null)
        {
            String cellTypeID = ct.getID();
            if(sim.getWorld().getCellThemes().containsKey(cellTypeID))
            {
                CellTheme cellTheme = sim.getWorld().getCellThemes().get(cellTypeID);
                g.drawImage(cellTheme.getImage(), (int)b.getX0(), (int)b.getY0(), (int)b.getX3(), (int)b.getY3(), cellTheme.getWidth(), cellTheme.getHeight(), 0, 0, null);
            }
        }
    }
	
    //Draws the robot itself
    private void paintRobot(Graphics2D g, Block b, Color c)
    {
        //To render Ol' Red: 
        /*
         * g.setColor(c);
         * AffineTransform at = AffineTransform.getRotateInstance(b.getRadAngle() - (Math.PI / 2), b.getCenterX(), b.getCenterY());  
         * g.fill(at.createTransformedShape(b.getRect()));
        */
        AffineTransform at1 = new AffineTransform();
        AffineTransform at2 = new AffineTransform();

        double sWidth = MainApplet.robotSprite.getIconWidth();
        double sHeight = MainApplet.robotSprite.getIconHeight();
        double tx = b.getCenterX() - sWidth / 2;
        double ty = b.getCenterY() - sHeight / 2;

        //Create the translation
        at1.translate(tx, ty);
        //Create the rotation
        at2.rotate(b.getRadAngle() - (Math.PI / 2), sWidth / 2, sHeight / 2);

        //Apply the rotation to the translation
        at1.concatenate(at2);

        //Draw robot sprite
        g.drawImage(MainApplet.robotSprite.getImage(), at1, this);

        CellType ct = b.getCellType();
        if(ct != null)
        {
            String cellTypeID = ct.getID();
            if(sim.getWorld().getCellThemes().containsKey(cellTypeID))
            {
                CellTheme cellTheme = sim.getWorld().getCellThemes().get(cellTypeID);
                g.drawImage(cellTheme.getImage(), (int)b.getX0(), (int)b.getY0(), (int)b.getX3(), (int)b.getY3(), cellTheme.getWidth(), cellTheme.getHeight(), 0, 0, null);
            }
        }
    }
	
    //Unused-- draws the edges of the robot's hitbox.
    //Might be useful for testing, so I'm leaving it in. 
    private void paintRobotEdges(Graphics2D g) 
    {
        g.setColor(Color.red);

        ArrayList<Point> points = World.getLine(sim.getRobot().getX0(), sim.getRobot().getY0(), sim.getRobot().getX1(), sim.getRobot().getY1());
        for(Point p : points)
        {
            g.fill(new Ellipse2D.Double(p.getX() - (5 / 2), p.getY() - (5 / 2), 5, 5));
        }

        points = World.getLine(sim.getRobot().getX0(), sim.getRobot().getY0(), sim.getRobot().getX2(), sim.getRobot().getY2());
        for(Point p : points)
        {
            g.fill(new Ellipse2D.Double(p.getX() - (5 / 2), p.getY() - (5 / 2), 5, 5));
        }

        points = World.getLine(sim.getRobot().getX1(), sim.getRobot().getY1(), sim.getRobot().getX3(), sim.getRobot().getY3());
        for(Point p : points)
        {
            g.fill(new Ellipse2D.Double(p.getX() - (5 / 2), p.getY() - (5 / 2), 5, 5));
        }

        points = World.getLine(sim.getRobot().getX3(), sim.getRobot().getY3(), sim.getRobot().getX2(), sim.getRobot().getY2());
        for(Point p : points)
        {
            g.fill(new Ellipse2D.Double(p.getX() - (5 / 2), p.getY() - (5 / 2), 5, 5));
        }

        g.setColor(Color.black);

        g.fill(new Ellipse2D.Double(sim.getRobot().getCenterX() - (5 / 2), sim.getRobot().getCenterY() - (5 / 2), 5, 5));
        g.fill(new Ellipse2D.Double(sim.getRobot().getX0() - (5 / 2), sim.getRobot().getY0() - (5 / 2), 5, 5));
        g.fill(new Ellipse2D.Double(sim.getRobot().getX1() - (5 / 2), sim.getRobot().getY1() - (5 / 2), 5, 5));
        g.fill(new Ellipse2D.Double(sim.getRobot().getX2() - (5 / 2), sim.getRobot().getY2() - (5 / 2), 5, 5));
        g.fill(new Ellipse2D.Double(sim.getRobot().getX3() - (5 / 2), sim.getRobot().getY3() - (5 / 2), 5, 5));		
    }
	
    //Draws lines from each sonar sensors to the edge of its 'vision'
    //Useful for debugging what the sensors are doing
    private void paintSonarSensors(Graphics2D g)
    {
        for(SonarSensor s : sim.getRobot().getSonarSensors())
        {
            g.setColor(Color.black);
            if(s.getType() == 'l')
            {
                g.draw(s.getShape());
                g.fill(new Ellipse2D.Double(s.getX0() - (5 / 2), s.getY0() - (5 / 2), 5, 5));
                g.fill(new Ellipse2D.Double(s.getX1() - (5 / 2), s.getY1() - (5 / 2), 5, 5));
            }
            else if(s.getType() == 'c')
            {
                g.draw(s.getShape1());
                g.fill(new Ellipse2D.Double(s.getX0() - (5 / 2), s.getY0() - (5 / 2), 5, 5));
                g.fill(new Ellipse2D.Double(s.getX1() - (5 / 2), s.getY1() - (5 / 2), 5, 5));

                g.draw(s.getShape2());
                g.fill(new Ellipse2D.Double(s.getX2() - (5 / 2), s.getY2() - (5 / 2), 5, 5));
            }
        }
    }

    public void mousePressed(MouseEvent click) 
    {
        //Give focus back to the main applet in order for keyboard controls to work
        MainApplet.m_instance.getFocus();
        if (!sim.mainApp.studentBuild)
        {
            //Add the block to the point selected
            sim.getWorld().toggleCell(click.getX(), click.getY());
            repaint();			
        }
    }

    public void run() 
    {
        while(true)
        {
            long beforeTime, timeDiff, sleep;
            beforeTime = System.currentTimeMillis();

            repaint();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = (1000 / fps) - timeDiff;

            if(sleep <= 0) sleep = 2;

            try 
            {
                Thread.sleep(sleep);
            } 
            catch (InterruptedException e) 
            {
                RobotSimulator.println("Interrupted Exception");
            }

            beforeTime = System.currentTimeMillis();
        }
    }

    public void mouseClicked(MouseEvent arg0) { }

    public void mouseEntered(MouseEvent arg0) {	}

    public void mouseExited(MouseEvent arg0) { }

    public void mouseReleased(MouseEvent arg0) { }

    //Needed to allow for variable size maps and scrolling
    @Override
    public Dimension getPreferredScrollableViewportSize() 
    {
        return new Dimension(520, 400);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) 
    {
        return 0;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() 
    {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) 
    {
        return 0;
    }
}
