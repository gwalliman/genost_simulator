package robotsimulator.worldobject;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import robotsimulator.Simulator;
import robotsimulator.world.CellType;
import robotsimulator.world.Point;
import robotsimulator.world.World;

public class Block 
{
	private Simulator sim;
	private double width, height, centerX, centerY, angle;
	private Rectangle2D rect;
	private Color color;
	private CellType cellType;
	
	public Block(int w, int h, double centerX2, double centerY2, double angle2, Simulator s)
	{
		width = w;
		height = h;
		centerX = centerX2;
		centerY = centerY2;
		angle = angle2;
		sim = s;
		
		rect = new Rectangle2D.Float((float)getTopLeftX(), (float)getTopLeftY(), (float)w, (float)h);
	}
	
	public Block(int w, int h, double centerX2, double centerY2, double angle2, Simulator s, Color c)
	{
		width = w;
		height = h;
		centerX = centerX2;
		centerY = centerY2;
		angle = angle2;
		sim = s;
		color = c;
		
		rect = new Rectangle2D.Float((float)getTopLeftX(), (float)getTopLeftY(), (float)w, (float)h);
	}
	
	public double getTopLeftX()
	{
		return centerX - (width / 2);
	}
	
	public double getTopLeftY()
	{
		return centerY - (height / 2);
	}
	
	public double getWidth()
	{
		return width;
	}
	
	public double getHeight()
	{
		return height;
	}
	
	public double getCenterX()
	{
		return centerX;
	}
	
	public double getCenterY()
	{
		return centerY;
	}
	
	public double getDegAngle()
	{
		return angle;
	}
	
	public double getRadAngle()
	{
		return Math.toRadians(angle);
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public void setCenterX(double c)
	{
		centerX = c;
		rect.setRect(getTopLeftX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}
	
	public void setCenterY(double c)
	{
		centerY = c;
		rect.setRect(rect.getX(), getTopLeftY(), rect.getWidth(), rect.getHeight());
	}
	
	public void setCenter(double x, double y) 
	{
		centerX = x;
		centerY = y;
		
		rect.setRect(getTopLeftX(), getTopLeftY(), rect.getWidth(), rect.getHeight());
	}
	
	public void setAngle(double c)
	{
		if(c > 360)
		{
			setAngle(c - 360);
		}
		else if(c < 0)
		{
			setAngle(c + 360);
		}
		else
		{
			angle = c;
		}
	}
	
	public double getX0()
	{
		return getCenterX() + (getHeight() / 2) * Math.cos(getRadAngle()) + (getWidth() / 2) * Math.sin(getRadAngle());
	}
	
	public double getY0()
	{
		return getCenterY() - (getWidth() / 2) * Math.cos(getRadAngle()) + (getHeight() / 2) * Math.sin(getRadAngle());
	}
	
	public double getX1()
	{
		return getCenterX() + (getHeight() / 2) * Math.cos(getRadAngle()) - (getWidth() / 2) * Math.sin(getRadAngle());
	}
	
	public double getY1()
	{
		return getCenterY() + (getWidth() / 2) * Math.cos(getRadAngle()) + (getHeight() / 2) * Math.sin(getRadAngle());
	}
	
	public double getX2()
	{
		return getCenterX() - (getHeight() / 2) * Math.cos(getRadAngle()) + (getWidth() / 2) * Math.sin(getRadAngle());
	}
	
	public double getY2()
	{
		return getCenterY() - (getWidth() / 2) * Math.cos(getRadAngle()) - (getHeight() / 2) * Math.sin(getRadAngle());
	}
	
	public double getX3()
	{
		return getCenterX() - (getHeight() / 2) * Math.cos(getRadAngle()) - (getWidth() / 2) * Math.sin(getRadAngle());
	}
	
	public double getY3()
	{
		return getCenterY() + (getWidth() / 2) * Math.cos(getRadAngle()) - (getHeight() / 2) * Math.sin(getRadAngle());
	}
	
	public void setCellType(CellType c)
	{
		cellType = c;
	}
        
        public void setCellType(String ct_id)
        {
            ArrayList<CellType> ct_list =  sim.getWorld().getCellTypes();
            for(CellType c : ct_list)
            {
                if(c.getID().equals(ct_id))
                {
                    cellType = c;
                    break;
                }
            }
        }
	
	public CellType getCellType()
	{
		return cellType;
	}
	
	public void translate(int r)
	{
		double oldCenterX = centerX;
		double oldCenterY = centerY;
		
		centerX = getCenterX() + (r * Math.cos(getRadAngle()));
		centerY = getCenterY() + (r * Math.sin(getRadAngle()));
		setCenter(centerX, centerY);
		
		if(checkCollision())
		{
			centerX = oldCenterX;
			centerY = oldCenterY;
			setCenter(centerX, centerY);
                        sim.mainApp.simPanelNb.stopExecution();
		}
	}

	public void rotate(double d)
	{
		double oldAngle = angle;
		
		setAngle(angle + d);
		
		if(checkCollision())
		{
			setAngle(oldAngle);
                        sim.mainApp.simPanelNb.stopExecution();
			//angle = getDegAngle();
		}
	}
	
	public boolean checkCollision() 
	{
		Point[][] worldPoints = sim.getWorld().getWorldPoints();
		ArrayList<Point> pfront = World.getLine(getX0(), getY0(), getX1(), getY1());
		ArrayList<Point> pleft = World.getLine(getX0(), getY0(), getX2(), getY2());
		ArrayList<Point> pright  = World.getLine(getX1(), getY1(), getX3(), getY3());
		ArrayList<Point> prear = World.getLine(getX3(), getY3(), getX2(), getY2());
		
		return checkEdgeCollision(pfront, worldPoints) || checkEdgeCollision(pleft, worldPoints) || checkEdgeCollision(pright, worldPoints) || checkEdgeCollision(prear, worldPoints);
	}
	
	private boolean checkEdgeCollision(ArrayList<Point> points, Point[][] worldPoints) 
	{
            for(Point p : points)
            {
                if(p.getX() < 0 || p.getX() >= sim.getWorld().getWidth() || p.getY() < 0 || p.getY() >= sim.getWorld().getHeight())
                {
                    return true;
                }

                if(worldPoints[p.getX()][p.getY()].isOccupied() && worldPoints[p.getX()][p.getY()].getOccupier().getCellType().doesClip())
                {
                    return true;
                }
                else
                {
                    if(worldPoints[p.getX()][p.getY()].getOccupier().getCellType().isCoin())
                    {
                        worldPoints[p.getX()][p.getY()].getOccupier().setCellType(
                                worldPoints[p.getX()][p.getY()].getOccupier().getCellType().getCoinUnder()
                        );
                        try
                        {
                            InputStream is = new ByteArrayInputStream(sim.coinBytes);
                            Clip clip = AudioSystem.getClip();
                            clip.open(AudioSystem.getAudioInputStream(is));
                            clip.start();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }

                        return false;
                    }
                }
            }
            return false;
	}
	
	public Rectangle2D getRect()
	{
		return rect;
	}
	
	public void export(Document doc, Element cellElement)
	{
		Element xe = doc.createElement("x");
		xe.appendChild(doc.createTextNode(Double.toString(getTopLeftX())));
		cellElement.appendChild(xe);
		
		Element ye = doc.createElement("y");
		ye.appendChild(doc.createTextNode(Double.toString(getTopLeftY())));
		cellElement.appendChild(ye);
		
		Element ae = doc.createElement("a");
		ae.appendChild(doc.createTextNode(Double.toString(getDegAngle())));
		cellElement.appendChild(ae);
		
		Element cte = doc.createElement("celltype");
		cte.appendChild(doc.createTextNode(cellType.getID()));
		cellElement.appendChild(cte);
	}
}
