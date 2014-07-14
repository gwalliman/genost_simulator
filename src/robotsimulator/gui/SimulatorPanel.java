package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
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
import robotinterpreter.RobotInterpreter;
import robotsimulator.RobotSimulator;
import robotsimulator.Simulator;
import robotsimulator.robot.Robot;
import robotsimulator.robot.SonarSensor;



//Used to organize simulator components. Held by the main applet class in a JTabbedPane
public class SimulatorPanel extends JPanel implements ActionListener {
	
	//Components
	//ButtonGrid
	public JPanel topPanel;
	private JButton openCodeBtn;			//Button for loading code from a file
	private JLabel codeNameLbl;				//Label to display loaded code file name
	private JButton openLoadoutBtn;			//Button for loading robot loadouts from a file
	private JLabel loadoutNameLbl;			//Label to display loaded loadout file name
	//private JButton openMazeBtn;			//Button for loading maze from a file
	private JComboBox openMazeList;
        private JLabel mazeNameLbl;				//Label to display loaded maze file name
	private JButton runBtn;					//Button to begin executing the simulation
	private JButton stopBtn;				//Button to stop executing the simulation
	private JButton reloadCodeBtn;			//Button to reload the code from the output area
	private JButton resetBtn;				//Button to reset the maze, robot position, and stop execution.
	private JButton webloadCodeBtn;			//Button to load code from the web service
	
	private JButton speedBtn;				//Button to toggle speeds. Can later extend to a slider
	private JLabel speedLbl;                //Label to display current speed
	
	//Right Panel
	private JPanel bottomPanel;
	private JLabel runningLbl;				//Robot status-- running or not
	private JTextArea outputTextArea;		//Holds output from running code, errors, etc.
	private JTextArea sensorText;			//Refreshed with sensor data
	private JPanel sensorPanel;	
        private JPanel stagePanel; //Holds labels for sensor data
	
	//Variables
	private int width, height, fps;
	
	//Reference to the main containing class & simulator
	private MainApplet main;
	private Simulator sim;
		
	//Simulator stage
	private int stageWidth = 520;
	private int stageHeight = 400;
	//Simulator variables
	private RobotInterpreter r;
	SwingWorker<Void, Void> executor;
	
	//Thread for updating sensor values
	sensorThread sThread;
	
	//Whether all necessary files are loaded in-- i.e. can we execute?
	private boolean readyToRun = false;
	
        public GridBagConstraints bottomConstraints;
			
	//File IO
	private JFileChooser fileChooser;           //Call this to let users load files
	private FileNameExtensionFilter txtFilter;  //Use this to restrict to text files (code)
	private FileNameExtensionFilter xmlFilter;  //Use this to restrict to xml files (loadouts & mazes)

	DecimalFormat df = new DecimalFormat("#.0");    //Used by sensor output to make display not horrible
	
	private boolean highSpeed = false;
	
	public SimulatorPanel(int w, int h, int f, Simulator s, MainApplet m)
	{	
		width = w;
		height = h;
		fps = f;
		
		sim = s;
		main = m;
        
		txtFilter = new FileNameExtensionFilter("Text Files ('.txt')", "txt");
		xmlFilter = new FileNameExtensionFilter("XML Files ('.xml')", "xml");
		
		
		JPanel simPane = new JPanel(new GridBagLayout());		
		//Controls size of left side-- input buttons, stage, etc. 3/4 of panel
		GridBagConstraints topSideConstraints = new GridBagConstraints();
		topSideConstraints.fill = GridBagConstraints.HORIZONTAL;
		//leftSideConstraints.insets = new Insets(4, 4, 4, 4);
		
		//Controls size of right side-- status, output, sensor data, etc. 1/4 of panel
		GridBagConstraints bottomSideConstraints = new GridBagConstraints();
		bottomSideConstraints.gridx = 0;
                bottomSideConstraints.gridy = GridBagConstraints.RELATIVE;
		bottomSideConstraints.gridheight = 1;
		bottomSideConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
		//rightSideConstraints.insets = new Insets(4, 4, 4, 4);
		
		topPanel = createTopPanel();
		simPane.add(topPanel, topSideConstraints);
		
		bottomPanel = createRightPanel();
		simPane.add(bottomPanel, bottomSideConstraints);

		add(simPane);
	}
	
	//Builds the left side of the window-- input buttons, stage, etc.
	public JPanel createTopPanel()
	{
		JPanel topPanel = new JPanel(new GridBagLayout());
		
		
		/*bottomConstraints = new GridBagConstraints();
		bottomConstraints.gridx = 0;
		bottomConstraints.gridy = GridBagConstraints.RELATIVE;
		bottomConstraints.gridheight = 2;
		bottomConstraints.insets = new Insets(4, 4, 4, 4);*/
                
                
		//stagePanel.add(createStagePanel(stageWidth, stageHeight, fps, sim));
                
		//Create stage and add it with these constraints
		//leftPanel.add(stagePanel, bottomConstraints);
		
		return topPanel;
	}
        
        public void createStage(Simulator s)
        {
            stagePanel = new JPanel();
            sim = s;
            GridBagConstraints stageConstraints = new GridBagConstraints();
            stageConstraints.fill = GridBagConstraints.HORIZONTAL;
            stagePanel.add(createStagePanel(stageWidth, stageHeight, fps, sim), stageConstraints);
                
            //Create stage and add it with these constraints
            topPanel.add(stagePanel);
        }
        
        public void startSensorThread()
        {
            //Start up the sensor thread
            sThread = new SimulatorPanel.sensorThread();
            (new Thread(sThread)).start();
        }
	
	public JPanel createButtonGridPanel()
	{
		//define 4x2 grid
		GridLayout g = new GridLayout(1, 0);
		JPanel bGridPanel = new JPanel(g);
		//bGridPanel.setSize(new Dimension(600, 100));
		//Add buttons and labels to it as needed
		openCodeBtn = new JButton("Load Program");
		openCodeBtn.addActionListener(this);
		//bGridPanel.add(openCodeBtn);
		
		codeNameLbl = new JLabel("Current Program: ");
		//bGridPanel.add(codeNameLbl);
			
		/*openMazeBtn = new JButton("Load Maze");
		openMazeBtn.addActionListener(this);
		bGridPanel.add(openMazeBtn);*/
                
                openMazeList = new JComboBox(main.getMazesFromWeb());
                openMazeList.setSelectedItem(main.mazeId);
                openMazeList.addActionListener(this);
                bGridPanel.add(openMazeList);
		
		mazeNameLbl = new JLabel("Current Maze: ");
		//bGridPanel.add(mazeNameLbl);
		
		openLoadoutBtn = new JButton("Load Config");
		openLoadoutBtn.addActionListener(this);
		openLoadoutBtn.setEnabled(false);
		//bGridPanel.add(openLoadoutBtn);
		
		loadoutNameLbl = new JLabel("Current Config: ");
		//bGridPanel.add(loadoutNameLbl);
		
		JPanel commandPanel = new JPanel();
		
		runBtn = new JButton("Execute!");
		runBtn.addActionListener(this);
		runBtn.setEnabled(false);
		commandPanel.add(runBtn);
		
		stopBtn = new JButton("Stop");
		stopBtn.addActionListener(this);
		stopBtn.setEnabled(false);
		commandPanel.add(stopBtn);
		
		resetBtn = new JButton("Reset");
		resetBtn.addActionListener(this);
		commandPanel.add(resetBtn);
				
		bGridPanel.add(commandPanel);
		return bGridPanel;
	}
	
	//Builds the right side of the window-- status, output, sensor data, etc.
	public JPanel createRightPanel()
	{

                
		JPanel rightPanel = new JPanel(new GridBagLayout());
		//rightPanel.setSize(200, 600);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.PAGE_START;
		
		c.gridheight = 1;
		//Add 'robot status' label
		JLabel statusLbl = new JLabel("Robot Status");
		//rightPanel.add(statusLbl, c);
		
                GridBagConstraints topConstraints = new GridBagConstraints();
		topConstraints.gridx = 0;
		topConstraints.gridy = 0;
		topConstraints.gridheight = 1;
		topConstraints.insets = new Insets(4, 4, 4, 4);
		//Create button grid panel and add it with these constraints
		rightPanel.add(createButtonGridPanel(), topConstraints);
                
		c.gridheight = 1;
		//add 'running/not running' label
		runningLbl = new JLabel("Waiting for Program...");
		//rightPanel.add(runningLbl, c);
		
		c.gridheight = 2;
		c.insets = new Insets(4, 4, 4, 4);
		
		//add output textarea
		outputTextArea = new JTextArea(12, 19);
		outputTextArea.setLineWrap(false);
		outputTextArea.setTabSize(2);
		
		JScrollPane outputScroll = new JScrollPane(outputTextArea);
		outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		rightPanel.add(outputScroll, c);
		
		JPanel actionPanel = new JPanel(new GridLayout(2, 1));
		
		reloadCodeBtn = new JButton("Reload Code from Text");
		reloadCodeBtn.addActionListener(this);		
		//actionPanel.add(reloadCodeBtn);
		
		webloadCodeBtn = new JButton("Load Code from Web");
		webloadCodeBtn.addActionListener(this);
		actionPanel.add(webloadCodeBtn);
		
		//rightPanel.add(actionPanel, c);

		//Experimental speed toggle!
                //Can later replace this with a slider
		c.gridheight = 1;
		c.insets = new Insets(4, 4, 4, 4);
		JPanel speedPanel = new JPanel();

		speedBtn = new JButton("Toggle Speed");
		speedBtn.addActionListener(this);
                speedPanel.add(speedBtn);
		
		speedLbl = new JLabel("Speed: Slow");
		speedPanel.add(speedLbl);
		
		rightPanel.add(speedPanel, c);

		c.gridheight = 1;
		c.insets = new Insets(1, 1, 1, 1);
		//add 'sensor data' label
		JLabel sensorLbl = new JLabel("Sensor Data");
		rightPanel.add(sensorLbl, c);
		
		c.gridheight = GridBagConstraints.RELATIVE;
		c.insets = new Insets(4, 4, 4, 4);
		//add sensor data panel 
		sensorPanel = createSensorPanel();
		rightPanel.add(sensorPanel, c);
					
		return rightPanel;
	}
	
	private JPanel createSensorPanel()
	{
		RobotSimulator.println("Creating Sensor Panel...");
		JPanel rtn = new JPanel(new GridLayout(2,1));
		rtn.setPreferredSize(new Dimension(200, 400));

		sensorText = new JTextArea(24, 40);
		sensorText.setEditable(false);
		rtn.add(sensorText);
		
		//Can add any other mission critical data here as well
		
		return rtn;
	}
		
	private ArrayList<SonarSensor> getRobotSonars()
	{
		return sim.getRobot().getSonarSensors();
	}
	
	//Updates the runningLbl with what it's waiting on (code, maze, etc.) and its current status
	private void updateRunningStatus()
	{
		readyToRun = false;
		runBtn.setEnabled(false);
		openLoadoutBtn.setEnabled(false);
		
		if (main.code == null)
			{
				runningLbl.setText("Waiting for Program...");
			}
		else if (main.mazeId == null)
			{
				runningLbl.setText("Waiting for maze file...");
			}
		/*else if (main.configFile == null)
			{
				openLoadoutBtn.setEnabled(true);
				runningLbl.setText("Waiting for robot configuration...");
			}*/
		else
			{
				runningLbl.setText("Ready!");	
				readyToRun = true;
				openLoadoutBtn.setEnabled(true);
    			runBtn.setEnabled(true);
    			resetBtn.setEnabled(true);
			}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == openCodeBtn)
		{
			
		}
		else if (e.getSource() == openLoadoutBtn)
		{

		}
		else if (e.getSource() == openMazeList)
		{
                    //main.openNewMaze();
                    JComboBox jcb = (JComboBox) e.getSource();
                    main.mazeId = (String) jcb.getSelectedItem();
                    main.mazeXml = sim.mainApp.getMazeData(main.mazeId);
                    sim.importStage(main.mazeXml);
                    reinitializeSensors();
		}
		else if (e.getSource() == runBtn)
		{
			if (readyToRun)
			{
				//Begin execution, enable the stopBtn, and disable ourselves
				runBtn.setEnabled(false);
				stopBtn.setEnabled(true);
				runningLbl.setText("Running!");
				
				sim.running = true;
	
				//Begin running the simulation
	            executor = new SwingWorker<Void, Void>()
	            {
	            	@Override
	            	public Void doInBackground()
	            	{
	            		RobotSimulator.println("doInBackground: " + this.hashCode());
	            		
	            		r = new RobotInterpreter();
	            		r.addRobotListener(sim);
	            		String code = outputTextArea.getText();
	            		r.load(code);
	
	        			sim.getRobot().start();
	        			sim.running = true;
	        			
	            		if(r.isReady())
	            		{
	            			r.execute();
	            		}
						return null;
	            	}
	            	
                    @Override
	            	public void done()
	            	{
	            		RobotSimulator.println("done: " + this.hashCode());
	            		
	        			runBtn.setEnabled(true);
	        			stopBtn.setEnabled(false);
	        			runningLbl.setText("Stopped.");
	        			r.stop();
	        			r.removeRobotListener(sim);		//--Need to tell it to stop listening to the interpreter
	        			sim.stop();
	        			
	            	}
	            };
	            executor.execute();
	        }
			else
			{
				//Not ready to run!	
			}
		}
		else if (e.getSource() == stopBtn)
		{
			//Stop execution, enable the runBtn, and disable ourselves
			stopExecution();
		}
		else if (e.getSource() == resetBtn)
		{
			//Stop execution, and reload the maze
			stopExecution();
			
			if (main.mazeId != null)
			{
				sim.importStage(main.mazeXml);
				reinitializeSensors();
			}
		}
		else if (e.getSource() == reloadCodeBtn)
		{
			//Loads the program from the edited text area
			loadCodefromText(outputTextArea.getText(), "Modified from Text*");			
		}
		else if (e.getSource() == webloadCodeBtn)
		{
			//Loads the program from the web service
                    main.loadCodeFromWeb();
		}
		else if (e.getSource() == speedBtn)
		{
			//Toggle between high and low speeds for robot
            //Speed is a multiplier-- e.g. sM=2 is twice as fast as sM=1
			if (highSpeed)
			{
				highSpeed = false;
				Robot.speedModifier = 1;
				speedLbl.setText("Speed: Slow");
			}
			else
			{
				highSpeed = true;
				Robot.speedModifier = 2;
				speedLbl.setText("Speed: Fast");
			}
		}
	}
    
	public void stopExecution()
	{
		if(executor != null)
                {
                    executor.cancel(true);
			
			if (r != null)
				r.stop();
                }       
		
                sim.stop();
		executor = null;
		
		updateRunningStatus();
	}
	
	public void loadCodefromText(String code, String newCodeName)
	{
		
            //Convert 'code' to a file and load the code in from there
            	main.code = code;
		codeNameLbl.setText("Current Program: " + newCodeName);
		
		runBtn.setEnabled(true);
		updateRunningStatus();
		
		loadCodeFile();
	}
	
    //Loads the code into the program from the codeFile in main
    public void loadCodeFile()
    {
		try 
		{
                    outputTextArea.setText(null);
                    outputTextArea.append(main.code);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }
	
	public void updateStage(int width, int height)
	{
		stageWidth = width;
		stageHeight = height;
		stagePanel = new JPanel();
                stagePanel.add(createStagePanel(width, height, fps, sim));
		sim.getWorld().setGridWidth(width);
		sim.getWorld().setGridHeight(height);
	}
	
	//Hacky method to reinitialize sensors and have them work again after the maze has changed
	public void reinitializeSensors()
	{
		//Pause the sensor thread
		stopSensorThread();
		
		//Also update the sensor panel
		bottomPanel.remove(sensorPanel);
		sensorPanel = null;
		sensorPanel = createSensorPanel();
		bottomPanel.add(sensorPanel);
		bottomPanel.revalidate();
		
		//Resume the sensor thread
		resumeSensorThread();
	}

    //Updates the sensor output text
	private void updateSensors()
	{
		try
		{
			String newSensorData = "[Sonar Sensors]\n";
			ArrayList<SonarSensor> sensorList = sim.getRobot().getSonarSensors();
			
			for (SonarSensor s : sensorList)
			{
				String t = "" + s.getLabel() + ": " + df.format(s.getConeSensorValue());
				t +=  "\n";
				newSensorData += t;
			}
	
			sensorText.setText(newSensorData);
		}
		catch (ConcurrentModificationException e)
		{
                    e.printStackTrace();
		}
	}
	
	public void resumeSensorThread()
	{
		sThread.running = true;
	}
	
	public void stopSensorThread()
	{
		sThread.running = false;
	}
	
    //The sensor update happens on a timer in this thread
	private class sensorThread implements Runnable
	{
		SimulatorPanel s;
		long sleepInterval = 100L;
		boolean running = true;
		
        @Override
		public void run() 
		{
			while (running)
            {
				//call 'updateSensors', then sleep
				updateSensors();
				try 
				{
					Thread.sleep(sleepInterval);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
    
    private String getSelectedMaze()
    {
        return (String) openMazeList.getSelectedItem();
    }
    
    //Creates a standard scrollable stage
    //Use this any time you need to add a stage somewhere
    public JScrollPane createStagePanel(int mazeWidth, int mazeHeight, int fps, Simulator sim)
    {
            JPanel sp = new JPanel();
            Stage simStage = new Stage(mazeWidth * 2, mazeHeight * 2, fps, sim);

            JScrollPane stageScroll = new JScrollPane(simStage);
            stageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            stageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            //stageScroll.setSize(mazeWidth * 2, mazeHeight * 2);
            return stageScroll;
            
            /*sp.add(stageScroll);
            return sp;*/
    }
}


