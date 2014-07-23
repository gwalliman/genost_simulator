package robotsimulator;

import javax.swing.JFrame;
import robotsimulator.gui.MainApplet;

/*
 * Main entry point for the program when creating a runnable .jar file. 
 * Creates and runs MainApplet, and has methods for printing
 */
public class RobotSimulator
{
    public static MainApplet m;
    //Use these methods to centralize output
    //Could later easily output to a textarea or file, log data, etc.
	public static void println(String message) 
	{
            if(m != null && m.simPanelNb != null)
            {
                m.simPanelNb.printToConsole(message + "\n");
            }
            System.out.println(message);
	}
        
        public static void print(String message) 
	{
            if(m != null && m.simPanelNb != null)
            {
                m.simPanelNb.printToConsole(message);
            }
            System.out.print(message);
	}

	public static void halt() 
	{
		System.exit(0);
	}
	
	public static void main(String[] args) 
	{
		m = new MainApplet();
		m.init();
		m.start();
		
		JFrame window = new JFrame("Robot Simulator");
		window.setContentPane(m);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
    }
}
