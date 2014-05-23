package robotsimulator;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import robotinterpreter.RobotListener;
import robotsimulator.gui.MainApplet;
import robotsimulator.robot.Robot;
import robotsimulator.world.World;
import robotsimulator.worldobject.Block;

/*
 * Listens to the robot interpreter and runs the simulation
 */
public class Simulator implements RobotListener 
{
	public MainApplet mainApp;
	
	private World world;
	private Robot robot;
    
	private static String newline = "\n";
	
	public int guiWidth;
	public int guiHeight;
	int guiFPS = 60;
	public String themeid;
        public byte[] coinBytes;
	
	public boolean running = false;
    
	public Simulator(MainApplet m)
	{	
            RobotSimulator.println("Importing maze from web");
            importStage(m.mazeXml);
            mainApp = m;
	}
	
	public Robot getRobot()
	{
		return robot;
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public void addBlock(int w, int h, int x, int y, int a)
	{
		Block b = new Block(w, h, x, y, a, this);
		world.addBlock(b);
	}

    /*
     * Robot execution commands
     * Each of these is implemented from RobotListener
     * Modify these methods if you want to change how the robot responds to code commands
     */
	public void driveForward() 
	{
		robot.stop();
		robot.drive('f');
	}

	public void driveBackwards() 
	{
		robot.stop();
		robot.drive('b');		
	}

	public void turnLeft() 
	{
		robot.stop();
		robot.turn('l');
	}

	public void turnRight() 
	{
		robot.stop();
		robot.turn('r');
	}

	public void stop() 
	{
		running = false;
		robot.stop();
		robot.abort();		
	}

	public int getSonarData(int num) 
	{
		return (int) Math.round(robot.getSonarSensor(num).getSensorValue());
	}

	public int getBearing() 
	{
		int angle = ((int) robot.getAngle()) + 90;
		if(angle > 360)
			angle -= 360;
		else if(angle < 0)
			angle += 360;
		return angle;
	}
	
	public void driveDistance(int dist)
	{
		robot.stop();
		robot.drive(dist);
		while(robot.getStatus() != 's') 
		{ 
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{

			}
		}
	}
	
	public void turnAngle(int angle) 
	{
		robot.stop();
		robot.turn(angle);
		while(robot.getStatus() != 's') 
		{ 
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{

			}
		}
	}

	public void turnToBearing(int bearing) 
	{
			robot.stop();

			int curBearing = getBearing();
			int turnAngle = bearing - curBearing;
			
			if(turnAngle > 180)
			{
				turnAngle -= 360;
			}
			else if(turnAngle < -180)
			{
				turnAngle += 360;
			}
			
			turnAngle(turnAngle);
	}

	@Override
	public void print(String s) 
	{
		System.out.print(s);		
	}

	@Override
	public void println(String s) {
		System.out.println(s);		
	
	}

	@Override
	public void error(String var, String e) {
		System.out.println(e);		
	}
        
    //Loads in a maze from the given file
	public void importStage(String x)
	{
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(x)));

                Node root = document.getDocumentElement();

                XPathFactory xPathFactory = XPathFactory.newInstance();
                XPath xpath = xPathFactory.newXPath();

                //Collect attributes for the maze
                Node guiWidthNode = root.getAttributes().getNamedItem("guiwidth");
                Node guiHeightNode = root.getAttributes().getNamedItem("guiheight");
                Node themeIDNode = root.getAttributes().getNamedItem("theme");
                themeid = themeIDNode.getNodeValue();

                //Collect robot attributes
                Node robotNode = ((NodeList)xpath.compile("robot").evaluate(root, XPathConstants.NODESET)).item(0);
                Node robotXNode = (((NodeList)xpath.compile("x").evaluate(robotNode, XPathConstants.NODESET))).item(0);
                Node robotYNode = (((NodeList)xpath.compile("y").evaluate(robotNode, XPathConstants.NODESET))).item(0);
                Node robotANode = (((NodeList)xpath.compile("a").evaluate(robotNode, XPathConstants.NODESET))).item(0);
                Node robotSonars = (((NodeList)xpath.compile("sonars").evaluate(robotNode, XPathConstants.NODESET))).item(0);

                RobotSimulator.println("Building robot");
                //Redefine the robot
                robot = new Robot(
                    (int)Math.round(Double.parseDouble(robotXNode.getTextContent())), 
                    (int)Math.round(Double.parseDouble(robotYNode.getTextContent())), 
                    (int)Math.round(Double.parseDouble(robotANode.getTextContent())), 
                    this
                );
                
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(robotSonars), new StreamResult(writer));
                String loadoutXml = writer.getBuffer().toString().replaceAll("\n|\r", ""); 
                
                RobotSimulator.println("Setting up robot loadout");
                //mainApp.configFile = loadoutXml;
                importLoadout(loadoutXml);

                //Collect world attributes
                Node worldNode = ((NodeList)xpath.compile("world").evaluate(root, XPathConstants.NODESET)).item(0);
                Node worldGridWidthNode = (((NodeList)xpath.compile("gridwidth").evaluate(worldNode, XPathConstants.NODESET))).item(0);
                Node worldGridHeighthNode = (((NodeList)xpath.compile("gridheight").evaluate(worldNode, XPathConstants.NODESET))).item(0);

                guiWidth = Integer.parseInt(guiWidthNode.getNodeValue());
                guiHeight = Integer.parseInt(guiHeightNode.getNodeValue());
                world = new World(guiWidth, guiHeight, this);
                world.setGridWidth(Integer.parseInt(worldGridWidthNode.getTextContent()));
                world.setGridHeight(Integer.parseInt(worldGridHeighthNode.getTextContent())); 
                
                RobotSimulator.println("Creating world");
                world.setTheme(themeIDNode.getNodeValue());
                

                //Add objects to the world
                RobotSimulator.println("Adding object to world");
                NodeList cellNodes = ((NodeList)xpath.compile("cells/cell").evaluate(worldNode, XPathConstants.NODESET));
                for(int i = 0; i < cellNodes.getLength(); i++)
                {
                    Node cellXNode = (((NodeList)xpath.compile("x").evaluate(cellNodes.item(i), XPathConstants.NODESET))).item(0);
                    Node cellYNode = (((NodeList)xpath.compile("y").evaluate(cellNodes.item(i), XPathConstants.NODESET))).item(0);
                    Node cellTypeNode = (((NodeList)xpath.compile("celltype").evaluate(cellNodes.item(i), XPathConstants.NODESET))).item(0);

                    world.toggleCell(
                        (int)Math.floor(Double.parseDouble(cellXNode.getTextContent())),
                        (int)Math.floor(Double.parseDouble(cellYNode.getTextContent())),
                        cellTypeNode.getTextContent()
                    );
                }
            }
            catch(Exception e)
            {
                    e.printStackTrace();
            }
	}
	
    //Saves the current stage to the provided file
	public String exportStage() 
	{
		try 
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("stage");
			doc.appendChild(rootElement);
			rootElement.setAttribute("guiwidth", Integer.toString(guiWidth));
			rootElement.setAttribute("guiheight", Integer.toString(guiHeight));
			rootElement.setAttribute("theme", themeid);
			
			robot.export(doc);
			world.export(doc);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        StringWriter writer = new StringWriter();
                        transformer.transform(new DOMSource(doc), new StreamResult(writer));
                        String output = writer.getBuffer().toString().replaceAll("\n|\r", ""); 
                        
                        return output;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
                        return null;
		}		
	}
	
	//Resets the stage back to the default, featureless void of white space
	public void resetStage()
	{
		world = new World(guiWidth, guiHeight, this);
		world.setTheme(themeid);		
	}
	
	//Changes the robot's sensor loadout based on the file
	//Doesn't change x/y and angle-- that's stored in the maze
	public void importLoadout(String loadoutXml)
	{
		try
		{
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(loadoutXml)));

                    Node root = document.getDocumentElement();

                    XPathFactory xPathFactory = XPathFactory.newInstance();
                    XPath xpath = xPathFactory.newXPath();
		    
		    NodeList sonarNodes = ((NodeList)xpath.compile("sonar").evaluate(root, XPathConstants.NODESET));

		    for(int i = 0; i < sonarNodes.getLength(); i++)
		    {
			    Node sonarTypeNode = (((NodeList)xpath.compile("type").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarNameNode = (((NodeList)xpath.compile("name").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarXNode = (((NodeList)xpath.compile("x").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarYNode = (((NodeList)xpath.compile("y").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarAngleNode = (((NodeList)xpath.compile("angle").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarLengthNode = (((NodeList)xpath.compile("length").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    
			    char sonarType = sonarTypeNode.getTextContent().charAt(0);
			    if(sonarType == 'l')
			    {
					robot.addSonarAbsolute(
							this, 
							sonarNameNode.getTextContent(), 
							Double.parseDouble(sonarXNode.getTextContent()),
							Double.parseDouble(sonarYNode.getTextContent()),
							Integer.parseInt(sonarLengthNode.getTextContent()),
							Integer.parseInt(sonarAngleNode.getTextContent())
						);

			    }
			    else if(sonarType == 'c')
			    {
				    Node sonarFOVNode = (((NodeList)xpath.compile("fov").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
				    robot.addSonarAbsolute(
							this, 
							sonarNameNode.getTextContent(), 
							Double.parseDouble(sonarXNode.getTextContent()),
							Double.parseDouble(sonarYNode.getTextContent()),
							Integer.parseInt(sonarLengthNode.getTextContent()),
							Integer.parseInt(sonarAngleNode.getTextContent()),
							Integer.parseInt(sonarFOVNode.getTextContent())
						);
			    }
		    }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
