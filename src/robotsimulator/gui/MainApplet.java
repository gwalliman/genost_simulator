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
import java.util.Scanner;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
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
import org.w3c.dom.Document;
import robotsimulator.RobotSimulator;
import robotsimulator.Simulator;
import static robotsimulator.gui.MainApplet.m_instance;
import robotsimulator.robot.Robot;
import robotsimulator.robot.SonarSensor;

/*
 * Main container class. Holds simulator panel and mazebuilder panel in tabbed panes.
 * Handles keybinds for the robot, and switching between tabs. 
*/
public class MainApplet extends JApplet implements ChangeListener 
{
    public static MainApplet m_instance;
    
    public final String[] finishModes = new String[] {"NONE", "DRIVE_TO_FINISH", "DRIVE_TO_FINISH_AND_STOP", "COLLECT_ALL_COINS" };
    public static final String serviceBaseUri = "http://genost.org/api/";

    //GUI variables
    private int width = 800;
    private int height = 600;
    public int stageWidth = 520;
    public int stageHeight = 400;
    private int fps = 60;

    //Structural variables
    private Simulator sim;
    //public SimulatorPanel simPanel;
    public SimPanelNB simPanelNb;
    public MazeBuilderNB mazeBuilderNb;

    //IO variables
    public String code;
    public String configFile;
    public String mazeId;
    public String mazeXml;
    public String robotXml;
    public int finishMode = 0;
    public int numCoins = 0;

    public String codeId;
    public String username;
    public String password;
    
    public boolean collided = false;

    //If true, this is a student build, and we should disable the maze builder, arrow keys, etc.
    public boolean studentBuild = false;

    //Robot image
    public static ImageIcon robotSprite;

    public void init()
    {
        try
        {
           RobotSimulator.m = this;
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
            RobotSimulator.println(e.getMessage());
        }
        finally
        {
            if(codeId == null || codeId.equals(""))
            {
                codeId =  "default";
            }
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
        if(studentBuild)
        {
            simPanelNb.createStage(sim);
            simPanelNb.loadCodefromText(code, "Loaded from Web*");
        }
        else
        {
            mazeBuilderNb.createStage(sim);
        }

        //Start sensor thread
        if(studentBuild)
        {
            RobotSimulator.println("Starting sensors");
            simPanelNb.startSensorThread();
        }
        else
        {
            RobotSimulator.println("Running prelaunch functions");
            mazeBuilderNb.preLaunchSetup();
        }
        
        //if(!studentBuild)
            setKeyBindings();
        RobotSimulator.println("We are go for launch");
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
                    RobotSimulator.println(e.getMessage());
                    return null;
            }
    }

    private void buildGUI()
    {
            setSize(new Dimension(width, height));
            if(studentBuild)
            {
                simPanelNb = new SimPanelNB(width, height, fps, sim, this);
                add(simPanelNb);
            }
            else
            {
                mazeBuilderNb = new MazeBuilderNB(fps, sim, this);
                add(mazeBuilderNb);
            }
    }

    //Returns focus back to the GUI and re-enables keyboard controls for the robot
    public void getFocus()
    {
        if(studentBuild)
            simPanelNb.grabFocus();
        else
            mazeBuilderNb.grabFocus();
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
        getRootPane().getActionMap().put("up", new AbstractAction() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                if (inSimulatorView() && !isExecuting())
                {
                    sim.getRobot().drive('f');
                }
            }
        });

        getRootPane().getActionMap().put("down", new AbstractAction() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                if (inSimulatorView() && !isExecuting())
                        sim.getRobot().drive('b');
            }
        });

        getRootPane().getActionMap().put("left", new AbstractAction() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                if (inSimulatorView() && !isExecuting())
                    sim.getRobot().turn('l');
            }
        });

        getRootPane().getActionMap().put("right", new AbstractAction() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                if (inSimulatorView() && !isExecuting())
                        sim.getRobot().turn('r');
            }
        });

        getRootPane().getActionMap().put("stop", new AbstractAction() 
        {
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
        return true;
    }

    //Returns true if the maze builder tab is currently showing
    private boolean inMazeView()
    {
        return true;
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
            
            String uri = serviceBaseUri + "getCode/";
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
                 conn.setRequestProperty("Accept", "text/plain");
                String inputStreamString = new Scanner(conn.getInputStream(),"UTF-8").useDelimiter("\\A").next();
                return inputStreamString;
            }
            catch(Exception e2)
            {
                e2.printStackTrace();
                RobotSimulator.println(e2.getMessage());
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
        String uri = serviceBaseUri + "listMazes";
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
            RobotSimulator.println(e2.getMessage());
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
        String uri = serviceBaseUri + "getMaze/" + mazeId;
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
            RobotSimulator.println(e2.getMessage());
        }
        
        return null;
    }
    
    public static String getLoadoutData(String loadoutId)
    {
        String uri = serviceBaseUri + "getLoadout/" + loadoutId;
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
            RobotSimulator.println(e2.getMessage());
        }
        
        return null;
    }
    
    public static Document getThemeData(String themeId)
    {
        String uri = serviceBaseUri + "getTheme/" + themeId;
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
            RobotSimulator.println(e2.getMessage());
        }
        
        return null;
    }
    
    public static InputStream getThemeImage(String themeId, String imageId)
    {
        String uri = serviceBaseUri + "getThemeImage/" + themeId + "/" + imageId;
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
            RobotSimulator.println(e2.getMessage());
        }
        
        return null;
    }
    
    //Calls the web service and loads in the code file from the web
    public void loadCodeFromWeb()
    {
        String webdata = getCode();
        String[] splitWebData = webdata.split("%", 4);
        username = splitWebData[0].trim();
        password = splitWebData[1].trim();
        String mid = splitWebData[2].trim();
        code = splitWebData[3];

        List<String> validMazes = Arrays.asList(getMazesFromWeb());
        if(validMazes.contains(mid))
        {
             mazeId = mid;
        }
    }
    
    public String[] getThemesFromWeb()
    {
        String uri = serviceBaseUri + "listThemes";
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
            ArrayList<String> themeList = new ArrayList<String>();
            for(int x = 0; x < jsonArray.size(); x++)
            {
                themeList.add((String) jsonArray.get(x));
            }
            
            return themeList.toArray(new String[themeList.size()]);
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
            RobotSimulator.println(e2.getMessage());
        }
        
        return null;
    }
    
    public Stage createStagePanel(int mazeWidth, int mazeHeight, int fps, Simulator sim)
    {
        Stage simStage = new Stage(mazeWidth * 2, mazeHeight * 2, fps, sim);

        return simStage;
    }
}
