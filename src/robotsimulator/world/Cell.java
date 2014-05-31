package robotsimulator.world;

import robotsimulator.Simulator;
import robotsimulator.worldobject.Block;

public class Cell 
{
	Simulator sim;
	CellType cellType;
	Block b;
	
	public Cell(int x, int y, int a, CellType c, Simulator s)
	{
		sim = s;
		cellType = c;
		int w = sim.getWorld().getGridWidth() * cellType.getWidth();
		int h = sim.getWorld().getGridHeight() * cellType.getHeight();
		b = new Block(w, h, x + (w / 2), y + (h / 2), a, s, cellType.getColor());
		b.setCellType(cellType);
	}
	
	public Block getBlock()
	{
		return b;
	}

	public CellType getCellType() 
	{
		return cellType;
	}
}
