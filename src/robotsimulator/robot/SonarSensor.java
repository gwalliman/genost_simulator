package robotsimulator.robot;

import java.awt.Label;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import robotsimulator.Simulator;
import robotsimulator.world.Point;
import robotsimulator.world.World;

/*
 * Represents one sonar sensor attached to the robot
 * Note that the update speed is tied to the robot update speed (speedModifier)
 */
public class SonarSensor implements Runnable
{
    private Simulator sim;
    private volatile Thread robotThread;
    private int delay = 50;
	
    //'l' if line
    //'c' if cone
    private char type;

    private String label;
    private Label t;

    private double x0, y0, x1, y1, x2, y2;

    private int angle;

    //The maximum distance that the sensor can detect items at
    private int length;

    //The angle in degrees (with respect to the robot) that the sensor is pointing.
    //0 is right, 90 is forwards, etc.
    //private int angle;

    //The field of view. If 60, then the sensor can detect items +- 30 degrees off its angle
    private int fov;

    //private Line2D line;
    private Shape shape1, shape2;

    /**
     * Line sensor
     * 
     * @param s	the simulator
     * @param n	the label (name)
     * @param x	the x coordinate where the sonar is located
     * @param y	the y coordinate where the sonar is located
     * @param l	the length of the sonar from the robot
     * @param a	the angle of the sonar sensor with respect to the robot
     */
    public SonarSensor(Simulator s, String n, double x, double y, int l, int a, boolean absolute)
    {
            sim = s;
            x0 = x;
            y0 = y;
            length = l;
            label = n;
            type = 'l';
            if(absolute)
                angle = a + (int)sim.getRobot().getAngle();
            else
                angle = a;

            int actualA = (int)Math.round(angle);
            if(actualA > 360)
            {
                    actualA -= 360;
            }

            x1 = getEndpointX(actualA);
            y1 = getEndpointY(actualA);

            Line2D line = new Line2D.Double(x0, y0, x1, y1);
            shape1 = line;

            if(!absolute)
                rotate(s.getRobot().getAngle());

            robotThread = new Thread(this);
            robotThread.start();
    }

    /**
     * Cone sensor
     * 
     * @param s	the simulator
     * @param n	the label (name)
     * @param x	the x coordinate where the sonar is located
     * @param y	the y coordinate where the sonar is located
     * @param l	the length of the sonar from the robot
     * @param a	the angle of the sonar sensor with respect to the robot
     */
    public SonarSensor(Simulator s, String n, double x, double y, int l, int a, int f, boolean absolute)
    {
            sim = s;
            x0 = x;
            y0 = y;
            length = l;
            label = n;
            type = 'c';
            if(absolute)
                angle = a + (int)sim.getRobot().getAngle();
            else
                angle = a;
            fov = f;

            int actualA = (int)Math.round(angle);
            if(actualA > 360)
            {
                    actualA -= 360;
            }

            x1 = getEndpointX(actualA - fov / 2);
            y1 = getEndpointY(actualA - fov / 2);
            x2 = getEndpointX(actualA + fov / 2);
            y2 = getEndpointY(actualA + fov / 2);

            Line2D line = new Line2D.Double(x0, y0, x1, y1);
            shape1 = line;
            line = new Line2D.Double(x0, y0, x2, y2);
            shape2 = line;

            if(!absolute)
                rotate(s.getRobot().getAngle());

            robotThread = new Thread(this);
            robotThread.start();
    }

    public String getLabel()
    {
            return label;
    }

    public double getX0()
    {
            return x0;
    }

    public double getY0()
    {
            return y0;
    }

    public double getX1()
    {
            return x1;
    }

    public double getY1()
    {
            return y1;
    }

    public double getX2()
    {
            return x2;
    }

    public double getY2()
    {
            return y2;
    }

    public Shape getShape()
    {
            return shape1;
    }

    public Shape getShape1()
    {
            return shape1;
    }

    public Shape getShape2()
    {
            return shape2;
    }

    public double getEndpointX(int a)
    {
            double x = Math.cos(Math.toRadians(a)) * length;
            return x + x0;
    }

    public double getEndpointY(int a)
    {
            double y = Math.sin(Math.toRadians(a)) * length;
            return y + y0;
    }

    public char getType() 
    {
            return type;
    }

    public void translate(double x, double y)
    {

            AffineTransform at = AffineTransform.getTranslateInstance(x, y);
            shape1 = at.createTransformedShape(shape1);
            setEndpoints1(shape1);

            if(type == 'c')
            {
                    at = AffineTransform.getTranslateInstance(x, y);
                    shape2 = at.createTransformedShape(shape2);
                    setEndpoints2(shape2);
            }
    }

    public void rotate(double a)
    {
            AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(a), sim.getRobot().getCenterX(), sim.getRobot().getCenterY());
            shape1 = at.createTransformedShape(shape1);
            setEndpoints1(shape1);

            if(type == 'c')
            {
                    at = AffineTransform.getRotateInstance(Math.toRadians(a), sim.getRobot().getCenterX(), sim.getRobot().getCenterY());
                    shape2 = at.createTransformedShape(shape2);
                    setEndpoints2(shape2);
            }
    }

    public double getSensorValue() 
    {
            if(type == 'l')
            {
                    return getLineSensorValue();
            }
            else if(type == 'c')
            {
                    return getConeSensorValue();
            }

            //We should never get here
            return 0;
    }

    public double getLineSensorValue() 
    {
            Point[][] worldPoints = sim.getWorld().getWorldPoints();
            ArrayList<Point> points = World.getLine(x0, y0, x1, y1);
            int x = -1;
            int y = -1;

            for(Point p : points)
            {
                    if(p.getX() < 0 || p.getX() >= sim.getWorld().getWidth() || p.getY() < 0 || p.getY() >= sim.getWorld().getHeight())
                    {
                            if(p.getX() < 0)
                            {
                                    x = 0;
                            }
                            else if(p.getX() >= sim.getWorld().getWidth())
                            {
                                    x = sim.getWorld().getWidth() - 1;
                            }
                            else
                            {
                                    x = p.getX();
                            }

                            if(p.getY() < 0)
                            {
                                    y = 0;
                            }
                            else if(p.getY() >= sim.getWorld().getHeight())
                            {
                                    y = sim.getWorld().getHeight() - 1;
                            }
                            else
                            {
                                    y = p.getY();
                            }
                            break;
                    }

                    if(worldPoints[p.getX()][p.getY()].isOccupied() && worldPoints[p.getX()][p.getY()].getOccupier().getCellType().doesClip())
                    {
                            x = p.getX();
                            y = p.getY();
                            break;
                    }
            }

            if(x == -1 || y == -1)
            {
                    x = length;
                    y = length;
            }

            return Math.hypot(x - x0, y - y0);
    }

    public double getConeSensorValue() 
    {
            Point[][] worldPoints = sim.getWorld().getWorldPoints();
            ArrayList<Point> points1 = World.getLine(x0, y0, x1, y1);
            ArrayList<Point> points2 = World.getLine(x0, y0, x2, y2);

            int x = length + 10;
            int y = length + 10;

            int max = Math.min(points1.size(), points2.size());			//Needs to be minimum, or else we get an array out of bounds exception

            for(int i = 0; i < max; i++)
            {
                    Point p1 = points1.get(i);		
                    Point p2 = points2.get(i);		

                    ArrayList<Point> ray = World.getLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());

                    for(Point p : ray)
                    {
                            if(p.getX() < 0 || p.getX() >= sim.getWorld().getWidth() || p.getY() < 0 || p.getY() >= sim.getWorld().getHeight())
                            {
                                    if(p.getX() < 0)
                                    {
                                            x = 0;
                                    }
                                    else if(p.getX() >= sim.getWorld().getWidth())
                                    {
                                            x = sim.getWorld().getWidth() - 1;
                                    }
                                    else
                                    {
                                            x = p.getX();
                                    }

                                    if(p.getY() < 0)
                                    {
                                            y = 0;
                                    }
                                    else if(p.getY() >= sim.getWorld().getHeight())
                                    {
                                            y = sim.getWorld().getHeight() - 1;
                                    }
                                    else
                                    {
                                            y = p.getY();
                                    }
                                    break;
                            }

                            if(worldPoints[p.getX()][p.getY()].isOccupied() && Math.hypot(p.getX() - x0, p.getY() - y0) < Math.hypot(x - x0, y - y0) && worldPoints[p.getX()][p.getY()].getOccupier().getCellType().doesClip())
                            {
                                    x = p.getX();
                                    y = p.getY();
                            }
                    }
                    if(x < length + 10 || y < length + 10)
                    {
                            return Math.hypot(x - x0, y - y0);
                    }
            }

            return length;
    }

    private void setEndpoints1(Shape shape)
    {
            PathIterator pi = shape.getPathIterator(null);

            double[] coords = new double[6];
            pi.currentSegment(coords);
            x0 = coords[0];
            y0 = coords[1];
            pi.next();
            pi.currentSegment(coords);
            x1 = coords[0];
            y1 = coords[1];
    }

    private void setEndpoints2(Shape shape)
    {
            PathIterator pi = shape.getPathIterator(null);

            double[] coords = new double[6];
            pi.currentSegment(coords);
            x0 = coords[0];
            y0 = coords[1];
            pi.next();
            pi.currentSegment(coords);
            x2 = coords[0];
            y2 = coords[1];
    }

    public void setTextField(Label text)
    {
            t = text;
    }

    public void run() 
    {
            while(true)
            {
                    long beforeTime, timeDiff, sleep;
            beforeTime = System.currentTimeMillis();

            if(t != null)
            {
                    t.setText(Double.toString(Double.parseDouble(new DecimalFormat("#.##").format(getSensorValue()))));
            }

                    timeDiff = System.currentTimeMillis() - beforeTime;
                    sleep = (delay / Robot.speedModifier) - timeDiff;

            if(sleep <= 0) sleep = 1;

                    try 
                    {
                            Thread.sleep(sleep);
                    } 
                    catch (InterruptedException e) 
                    {
                    }

        beforeTime = System.currentTimeMillis();
            }
    }

    public void export(Document doc, Element sonarElement) 
    {
            Element te = doc.createElement("type");
            te.appendChild(doc.createTextNode(Character.toString(type)));
            sonarElement.appendChild(te);

            Element ne = doc.createElement("name");
            ne.appendChild(doc.createTextNode(label));
            sonarElement.appendChild(ne);

            Element xe = doc.createElement("x");
            xe.appendChild(doc.createTextNode(Double.toString(x0)));
            sonarElement.appendChild(xe);

            Element ye = doc.createElement("y");
            ye.appendChild(doc.createTextNode(Double.toString(y0)));
            sonarElement.appendChild(ye);

            Element ae = doc.createElement("angle");
            Double trueAngle = angle - sim.getRobot().getAngle();
            ae.appendChild(doc.createTextNode(Integer.toString((int)Math.round(trueAngle))));
            sonarElement.appendChild(ae);

            Element le = doc.createElement("length");
            le.appendChild(doc.createTextNode(Integer.toString(length)));
            sonarElement.appendChild(le);

            if(type == 'c')
            {
                    Element fe = doc.createElement("fov");
                    fe.appendChild(doc.createTextNode(Integer.toString(fov)));
                    sonarElement.appendChild(fe);		
            }
    }
}
