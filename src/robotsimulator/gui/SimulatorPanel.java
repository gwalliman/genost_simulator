package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import robotsimulator.RobotSimulator;
import robotinterpreter.RobotInterpreter;
import robotsimulator.Simulator;
import robotsimulator.robot.Robot;
import robotsimulator.robot.SonarSensor;


//Used to organize simulator components. Held by the main applet class in a JTabbedPane
public class SimulatorPanel extends JPanel implements ActionListener {
	
	//Components
	//ButtonGrid
	private JPanel leftPanel;
	private JButton openCodeBtn;			//Button for loading code from a file
	private JLabel codeNameLbl;				//Label to display loaded code file name
	private JButton openLoadoutBtn;			//Button for loading robot loadouts from a file
	private JLabel loadoutNameLbl;			//Label to display loaded loadout file name
	private JButton openMazeBtn;			//Button for loading maze from a file
	private JLabel mazeNameLbl;				//Label to display loaded maze file name
	private JButton runBtn;					//Button to begin executing the simulation
	private JButton stopBtn;				//Button to stop executing the simulation
	private JButton reloadCodeBtn;			//Button to reload the code from the output area
	private JButton resetBtn;				//Button to reset the maze, robot position, and stop execution.
	private JButton webloadCodeBtn;			//Button to load code from the web service
	
	private JButton speedBtn;				//Button to toggle speeds. Can later extend to a slider
	private JLabel speedLbl;                //Label to display current speed
	
	//Right Panel
	private JPanel rightPanel;
	private JLabel runningLbl;				//Robot status-- running or not
	private JTextArea outputTextArea;		//Holds output from running code, errors, etc.
	private JTextArea sensorText;			//Refreshed with sensor data
	private JPanel sensorPanel;				//Holds labels for sensor data
	
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
	
	private JPanel stagePanel;
			
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
        
		fileChooser = new JFileChooser("");
		txtFilter = new FileNameExtensionFilter("Text Files ('.txt')", "txt");
		xmlFilter = new FileNameExtensionFilter("XML Files ('.xml')", "xml");
		
		
		JPanel simPane = new JPanel(new GridBagLayout());		
		//Controls size of left side-- input buttons, stage, etc. 3/4 of panel
		GridBagConstraints leftSideConstraints = new GridBagConstraints();
		leftSideConstraints.gridx = 0;
		leftSideConstraints.gridy = 0;
		leftSideConstraints.gridwidth = 3;
		leftSideConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		leftSideConstraints.insets = new Insets(4, 4, 4, 4);
		
		//Controls size of right side-- status, output, sensor data, etc. 1/4 of panel
		GridBagConstraints rightSideConstraints = new GridBagConstraints();
		rightSideConstraints.gridx = GridBagConstraints.RELATIVE;
        rightSideConstraints.gridy = 0;
		rightSideConstraints.gridwidth = 1;
		rightSideConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
		rightSideConstraints.insets = new Insets(4, 4, 4, 4);
		
		leftPanel = createLeftPanel();
		simPane.add(leftPanel, leftSideConstraints);
		
		rightPanel = createRightPanel();
		simPane.add(rightPanel, rightSideConstraints);

		add(simPane);
		
		loadDefaults();		
		//Start up the sensor thread
        sThread = new sensorThread();
        (new Thread(sThread)).start();
	}
	
	//Load in default options-- default map, sensor loadout, and program
	private void loadDefaults()
	{
		//Load loadout config
		//Load the initial config from the jar
		ClassLoader cl = this.getClass().getClassLoader();

        InputStream is = null;
		FileOutputStream os = null;
		File newConfig = new File("tempConfig");
		
        //Gross procedure to serialize the resource file into a File object
		try
		{
			is = cl.getResourceAsStream("Resources/Loadouts/DefaultLoadout.xml");
			os = new FileOutputStream(newConfig);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = is.read(bytes)) != -1)
			{
				os.write(bytes, 0, read);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (is != null)
			{
				try 
				{
					is.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (os != null)
			{
				try 
				{
					os.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		main.configFile = newConfig;
		loadoutNameLbl.setText("Current Config: " + "DefaultLoadout.xml");
		updateRunningStatus();
		
		//Update the robot's sensor and loadout configuration
		sim.importLoadout(main.configFile);
        
        //Load the initial code from the web service
        loadCodeFromWeb();
	}
	
	//Builds the left side of the window-- input buttons, stage, etc.
	public JPanel createLeftPanel()
	{
		JPanel leftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints topConstraints = new GridBagConstraints();
		topConstraints.gridx = 0;
		topConstraints.gridy = 0;
		topConstraints.gridheight = 1;
		topConstraints.insets = new Insets(4, 4, 4, 4);
		//Create button grid panel and add it with these constraints
		leftPanel.add(createButtonGridPanel(), topConstraints);
		
		GridBagConstraints bottomConstraints = new GridBagConstraints();
		bottomConstraints.gridx = 0;
		bottomConstraints.gridy = GridBagConstraints.RELATIVE;
		bottomConstraints.gridheight = 2;
		bottomConstraints.insets = new Insets(4, 4, 4, 4);
		//Create stage and add it with these constraints
		stagePanel = Stage.createStagePanel(stageWidth, stageHeight, fps, sim, false);
		leftPanel.add(stagePanel, bottomConstraints);
		
		return leftPanel;
	}
	
	public JPanel createButtonGridPanel()
	{
		//define 4x2 grid
		GridLayout g = new GridLayout(4, 2, 20, 4);
		JPanel bGridPanel = new JPanel(g);
		bGridPanel.setSize(new Dimension(600, 100));
		//Add buttons and labels to it as needed
		openCodeBtn = new JButton("Load Program");
		openCodeBtn.addActionListener(this);
		bGridPanel.add(openCodeBtn);
		
		codeNameLbl = new JLabel("Current Program: ");
		bGridPanel.add(codeNameLbl);
			
		openMazeBtn = new JButton("Load Maze");
		openMazeBtn.addActionListener(this);
		bGridPanel.add(openMazeBtn);
		
		mazeNameLbl = new JLabel("Current Maze: ");
		bGridPanel.add(mazeNameLbl);
		
		openLoadoutBtn = new JButton("Load Config");
		openLoadoutBtn.addActionListener(this);
		openLoadoutBtn.setEnabled(false);
		bGridPanel.add(openLoadoutBtn);
		
		loadoutNameLbl = new JLabel("Current Config: ");
		bGridPanel.add(loadoutNameLbl);
		
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
		rightPanel.setSize(200, 600);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.PAGE_START;
		
		c.gridheight = 1;
		//Add 'robot status' label
		JLabel statusLbl = new JLabel("Robot Status");
		rightPanel.add(statusLbl, c);
		
		c.gridheight = 1;
		//add 'running/not running' label
		runningLbl = new JLabel("Waiting for Program...");
		rightPanel.add(runningLbl, c);
		
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
		actionPanel.add(reloadCodeBtn);
		
		webloadCodeBtn = new JButton("Load Code from Web");
		webloadCodeBtn.addActionListener(this);
		actionPanel.add(webloadCodeBtn);
		
		rightPanel.add(actionPanel, c);

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
		
		if (main.codeFile == null)
			{
				runningLbl.setText("Waiting for Program...");
			}
		else if (main.mapFile == null)
			{
				runningLbl.setText("Waiting for maze file...");
			}
		else if (main.configFile == null)
			{
				openLoadoutBtn.setEnabled(true);
				runningLbl.setText("Waiting for robot configuration...");
			}
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
			//Open a file chooser dialog to load in code.
			//Restrict filechooser to the correct datatype
			fileChooser.setFileFilter(txtFilter);
			
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				main.codeFile = fileChooser.getSelectedFile();
				codeNameLbl.setText("Current Program: " + main.codeFile.getName());
				
				runBtn.setEnabled(true);
				updateRunningStatus();
				
				loadCodeFile();
			}
		}
		else if (e.getSource() == openLoadoutBtn)
		{
			//Open a file chooser dialog to load in robot loadouts
			fileChooser.setFileFilter(xmlFilter);
			
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				main.configFile = fileChooser.getSelectedFile();
				loadoutNameLbl.setText("Current Config: " + main.configFile.getName());
				updateRunningStatus();
				
				//Update the robot's sensor and loadout configuration
				reinitializeSensors();
			}
		}
		else if (e.getSource() == openMazeBtn)
		{
			//Open a file chooser dialog to load in maze layouts
			fileChooser.setFileFilter(xmlFilter);
			
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				main.mapFile = fileChooser.getSelectedFile();
				mazeNameLbl.setText("Current Maze: " + main.mapFile.getName());
				updateRunningStatus();
				
				//Update the maze here
				sim.importStage(main.mapFile);
				reinitializeSensors();
                
                //Also signal to mazebuilder to update its displays
                if (!MainApplet.studentBuild)
                    main.mazePanel.refreshMazeSettings();
			}
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
			
			if (main.mapFile != null)
			{
				sim.importStage(main.mapFile);
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
            loadCodeFromWeb();
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
	
    //Calls the web service and loads in the code file from the web
    public void loadCodeFromWeb()
    {
		loadCodefromText(getCode(), "Loaded from Web*");
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
        BufferedWriter w = null;
		try
		{
		File textCode = new File("ModifiedCode");
		w = new BufferedWriter(new FileWriter(textCode));
		w.write(code);
		w.close();
		
		main.codeFile = textCode;
		codeNameLbl.setText("Current Program: " + newCodeName);
		
		runBtn.setEnabled(true);
		updateRunningStatus();
		
		loadCodeFile();
		
		}
		catch (Exception writerE)
		{
			writerE.printStackTrace();
		}
		finally
		{
			try 
			{
				w.close();
			} 
			catch (Exception e1) 
			{
				e1.printStackTrace();
			}
		}

	}
	
    //Loads the code into the program from the codeFile in main
	public void loadCodeFile()
    {
		try 
		{
			FileReader fr = new FileReader(main.codeFile);
		    BufferedReader br = new BufferedReader(fr);
		    String line = "";
            String code = "";
            
            while((line = br.readLine()) != null)
            {
                 code += line + "\n";
            }
             
            br.close();
            fr.close();
             
            outputTextArea.setText(null);
            outputTextArea.append(code);
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
		stagePanel = Stage.createStagePanel(width, height, fps, sim, false);
		sim.getWorld().setGridWidth(width);
		sim.getWorld().setGridHeight(height);
	}
	
	//Hacky method to reinitialize sensors and have them work again after the maze has changed
	public void reinitializeSensors()
	{
		if (main.configFile != null)
			sim.importLoadout(main.configFile);	
		
		//Pause the sensor thread
		stopSensorThread();
		
		//Also update the sensor panel
		rightPanel.remove(sensorPanel);
		sensorPanel = null;
		sensorPanel = createSensorPanel();
		rightPanel.add(sensorPanel);
		rightPanel.revalidate();
		
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

    //Autogenerated by Netbeans to call the code service
    private static String getCode() 
    {
        try
        {
            org.tempuri.Service service = new org.tempuri.Service();            //* Autogen'd
            org.tempuri.IService port = service.getBasicHttpBindingIService();  //* Autogen'd
            return port.getCode();                                              //* Autogen'd
        }
        catch (Exception e)
        {
            RobotSimulator.println("Couldn't load code from web. ");
            return "Couldn't load code from web. ";
        }
    }
	
}

