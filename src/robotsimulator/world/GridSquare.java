package robotsimulator.world;

public class GridSquare 
{
	int x0, y0, width, height, angle;
	Cell c;
	
	public GridSquare(int x, int y, int w, int h, int a)
	{
		x0 = x;
		y0 = y;
		width = w;
		height = h;
		angle = a;
	}
	
	public boolean isOccupied()
	{
		if(c != null)
			return true;
		else return false;
	}
	
	public void occupy(Cell c0)
	{
		c = c0;
	}
	
	public void unOccupy()
	{
		c = null;
	}

	public int getWidth() 
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getAngle()
	{
		return angle;
	}
	
	public int getCenterX()
	{
		return x0 + width / 2;
	}
	
	public int getCenterY()
	{
		return y0 + width / 2;
	}
	
	public Cell getCell()
	{
		return c;
	}
}
