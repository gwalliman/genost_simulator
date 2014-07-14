package robotsimulator.world;

import java.awt.Color;

public class CellType 
{
	private String id;
	private String label;
	private int width, height;
	private Color color;
	private boolean clip;
        private boolean coin;
        private boolean finish;
        private String coinUnder;
	
	public CellType(String i, String n, int w, int h, boolean cl, boolean c2, String cu, boolean fin, Color c)
	{
		id = i;
		label = n;
		width = w;
		height = h;
		clip = cl;
		color = c;
                coin = c2;
                coinUnder = cu;
                finish = fin;
	}
	
	public String getID()
	{
		return id;
	}

	public String getLabel() 
	{
		return label;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public boolean doesClip()
	{
		return clip;
	}
        
        public boolean isCoin()
        {
            return coin;
        }
        
        public boolean isFinish()
        {
            return finish;
        }

	public Color getColor() 
	{
		return color;
	}
        
        public String getCoinUnder()
        {
            return coinUnder;
        }
}
