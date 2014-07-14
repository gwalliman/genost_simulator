package robotsimulator.world;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import robotsimulator.RobotSimulator;
import robotsimulator.gui.MainApplet;

public class CellTheme 
{
	String id;
        String themeId;
        String imageName;
	BufferedImage image;
	int width, height;
	
	public CellTheme(String i, InputStream in)
	{
		id = i;
		try 
		{
			image = ImageIO.read(in);
			width = image.getWidth(null);
			height = image.getHeight(null);
			
		}
		catch(IOException e)
		{
			RobotSimulator.println("Cannot find image.");
		}
	}
        
        public CellTheme(String i, String tid, String iu)
	{
            themeId = tid;
            id = i;
            imageName = iu;
	}
	
	public BufferedImage getImage()
	{
            if(image != null)
            {
		return image;
            }
            else
            {
                try 
		{
			image = ImageIO.read(MainApplet.loadSprite(themeId, imageName));
			width = image.getWidth(null);
			height = image.getHeight(null);
                        return image;
		}
		catch(IOException e)
		{
			RobotSimulator.println("Cannot find image.");
                        return null;
		}
            }
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
}
