package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import robotsimulator.RobotSimulator;
import robotsimulator.Simulator;
import static robotsimulator.gui.MainApplet.m_instance;
import static robotsimulator.gui.MainApplet.studentBuild;
import robotsimulator.robot.Robot;
import robotsimulator.robot.SonarSensor;

/*
 * Main container class. Holds simulator panel and mazebuilder panel in tabbed panes.
 * Handles keybinds for the robot, and switching between tabs. 
*/
public class MainApplet extends JApplet implements ChangeListener {

	public static MainApplet m_instance;
	
	//GUI variables
	private int width = 800;
	private int height = 600;
	private int fps = 60;
	
	//Structural variables
	private JTabbedPane tabPane;
	private Simulator sim;
	//public SimulatorPanel simPanel;
        public SimPanelNB simPanelNb;
	
	//IO variables
	public String code;
	public String configFile;
	public String mazeId;
        public String mazeXml;
        public String robotXml;
        
        public String codeId;
	
	//If true, this is a student build, and we should disable the maze builder, arrow keys, etc.
	public static final boolean studentBuild = false;
    
    //Robot image
	public static ImageIcon robotSprite;
		
	public void init()
	{
            try
            {
               String url = getDocumentBase().toString();
               Map<String, String> paramValue = new HashMap<String, String>();
               
               if (url.indexOf("?") > -1) 
               {
                       String parameters = url.substring(url.indexOf("?") + 1);
                       StringTokenizer paramGroup = new StringTokenizer(parameters, "&");
 
                       while(paramGroup.hasMoreTokens())
                       {
                           StringTokenizer value = new StringTokenizer(paramGroup.nextToken(), "=");
                           paramValue.put(value.nextToken(), value.nextToken());
                       }
               }
               
               codeId = paramValue.get("codeId");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(codeId == null || codeId.equals(""))
                    codeId =  "default";
            }
            
            RobotSimulator.println("Loading in code from web");
            loadCodeFromWeb();
            mazeXml = getMazeData(mazeId);
            //Create a static reference to the applet if none exists
            if (m_instance == null)
                    m_instance = this;
            
            RobotSimulator.println("Building GUI.");
            buildGUI();
            
            RobotSimulator.println("Launching simulator");
            sim = new Simulator(this);
            
            RobotSimulator.println("Setting up stage");
            simPanelNb.createStage(sim);
            simPanelNb.loadCodefromText(code, "Loaded from Web*");
            
            //Start sensor thread
            RobotSimulator.println("Starting sensors");
            simPanelNb.startSensorThread();
            
            RobotSimulator.println("We are go for launch");

            if (!studentBuild)
                    setKeyBindings();
        }
	
	public static InputStream loadSprite(String themeId, String imageId)
	{
		//Load the sprite from the resources folder in the jar
		try
		{
                    InputStream is = getThemeImage(themeId, imageId);
                    return is;
		}
		catch (Exception e)
		{
			e.printStackTrace();
                        return null;
		}
	}
	
	private void buildGUI()
	{
		setSize(new Dimension(width, height));
		
		tabPane = new JTabbedPane();
		tabPane.setSize(width, height);
        
		//Remove the default keyboard shortcuts for tabs-- conflicts with robot manual control
		tabPane.setActionMap(null);
		//tabPane.addChangeListener(this);
		
		simPanelNb = new SimPanelNB(width, height, fps, sim, this);
                tabPane.addTab("Simulator", simPanelNb);
		
		//mazePanel = new MazeBuilderPanel(fps, sim, this);
                //Add in the maze builder tab if we're not using a student build
		//if (!studentBuild)
			//tabPane.addTab("Maze Builder", mazePanel);
				
		add(tabPane);
	}
	
	//Returns focus back to the GUI and re-enables keyboard controls for the robot
	public void getFocus()
	{
		tabPane.grabFocus();
	}
	
	//Methods copied directly from GUI.java
	public int getFPS()
	{
		return fps;
	}

    //Defines the keyboard shortcuts for driving the robot. 
	private void setKeyBindings()
	{
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "debug");
		
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "stop");
		
		getRootPane().getActionMap().put("debug", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
                //Debug command-- change this for easy on-demand console printing, debugging, etc.
				RobotSimulator.println("[DEBUG]");
				Robot b = sim.getRobot();
				for (SonarSensor s : b.getSonarSensors())
				{
					RobotSimulator.println("[" + s.getLabel() + "]: " + s.getConeSensorValue());
				}
			}
		});
        
        /*
         * The following all allow manual driving of the robot
         */
		getRootPane().getActionMap().put("up", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().drive('f');
	    	}
	    });
	    
		getRootPane().getActionMap().put("down", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().drive('b');
	    	}
	    });
	    
		getRootPane().getActionMap().put("left", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().turn('l');
	    	}
	    });
	    
		getRootPane().getActionMap().put("right", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().turn('r');
	    	}
	    });
	    
		getRootPane().getActionMap().put("stop", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().stop();
	    	}
	    });
	}
		
	//Returns true if the simulator tab is currently showing
	private boolean inSimulatorView()
	{
		return (tabPane.getSelectedIndex() == 0);
	}
	
	//Returns true if the maze builder tab is currently showing
	private boolean inMazeView()
	{
		return (tabPane.getSelectedIndex() == 1);
	}
	
	//Returns true if the simulator tab is currently running code
	private boolean isExecuting()
	{
		return sim.running;
	}
		
	@Override
	//Fires whenever a tab is changed. Used to save/close/stop execution when changing focus
	//TODO: Reinitialize robot sensor display when switching back from builder view
	public void stateChanged(ChangeEvent e) 
	{
		if (inSimulatorView())
		{
			simPanelNb.resumeSensorThread();
		}
		if (inMazeView())
		{
			//Stop execution
			simPanelNb.stopExecution();
			simPanelNb.stopSensorThread();
			//sim.stop();
		}
	}
        
        //Autogenerated by Netbeans to call the code service
    public String getCode() 
    {
        try
        {
            /*org.tempuri.Service service = new org.tempuri.Service();            //* Autogen'd
            org.tempuri.IService port = service.getBasicHttpBindingIService();  //* Autogen'd
            return port.getCode();                                              //* Autogen'd*/
            
            String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/codeRestSvc/Service.svc/GetCode/";
            if(codeId != null && codeId != "")
            {
                uri = uri + codeId;
            }
            else
            {
                uri = uri + "asdf123";
            }
            
            try
            {
                URL url = new URL(uri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept","application/xml");

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document document = builder.parse(conn.getInputStream());
                Element root = document.getDocumentElement();
                
                Node child = root.getFirstChild();
                if (child instanceof CharacterData) {
                    CharacterData cd = (CharacterData) child;
                    return cd.getData();
                }
            }
            catch(Exception e2)
            {
                e2.printStackTrace();
            }

            return null;
        }
        catch (Exception e)
        {
            RobotSimulator.println("Couldn't load code from web. ");
            return "Couldn't load code from web. ";
        }
    }

    public String[] getMazesFromWeb()
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/listMazes";
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/json");
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(isr);
            ArrayList<String> mazeList = new ArrayList<String>();
            for(int x = 0; x < jsonArray.size(); x++)
            {
                mazeList.add((String) jsonArray.get(x));
            }
            
            return mazeList.toArray(new String[mazeList.size()]);
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    public void openNewMaze()
    {
        /*if(mazeId == null)
        {
            mazeId = getSelectedMaze();
        }
        String mazeXml = getMazeData(mazeId);

        if (mazeXml != null)
        {
            mapData = mazeXml;
            mazeNameLbl.setText("Current Maze: " + mazeId);
            updateRunningStatus();

            //Update the maze here
            sim.importStage(main.mapData);
            reinitializeSensors();

            //Also signal to mazebuilder to update its displays
            if (!MainApplet.studentBuild)
            {
                main.mazePanel.refreshMazeSettings();
            }
        }*/
    }
    
    public String getMazeData(String mazeId)
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/getMaze/" + mazeId;
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/xml");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(conn.getInputStream());
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", ""); 
            
            return output;
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    public static String getLoadoutData(String loadoutId)
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/getLoadout/" + loadoutId;
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/xml");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(conn.getInputStream());
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", ""); 
            
            return output;
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    public static Document getThemeData(String themeId)
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/getTheme/" + themeId;
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/xml");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(conn.getInputStream());
            return document;
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    public static InputStream getThemeImage(String themeId, String imageId)
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/getThemeImage/" + themeId + "/" + imageId;
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/xml");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            return conn.getInputStream();
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    //Calls the web service and loads in the code file from the web
    public void loadCodeFromWeb()
    {
        String webdata = getCode();
        String[] splitWebData = webdata.split("%", 2);

        if(splitWebData.length == 2)
        {
            String mid = splitWebData[0];
            code = splitWebData[1];
            
            List<String> validMazes = Arrays.asList(getMazesFromWeb());
            if(validMazes.contains(mid))
            {
                 mazeId = mid;
            }
        }
        else
        {
            code = splitWebData[0];
        }
    }
}
