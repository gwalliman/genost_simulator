package robotsimulator.world;

import robotsimulator.worldobject.Block;

public class Point 
{
	private Block obj;
	private int x, y;
	
	public Point(int a, int b)
	{
		x = a;
		y = b;
	}
	
	public void occupy(Block b)
	{
		obj = b;
	}
	
	public void unOccupy()
	{
		obj = null;
	}
	
	public boolean isOccupied()
	{
		if(obj == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public boolean compare(int a, int b)
	{
		if(x == a && y == b)
		{
			return true;
		}
		else return false;
	}
	
	public Block getOccupier()
	{
		return obj;
	}

	public int getX() 
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
}
