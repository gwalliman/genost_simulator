package robotsimulator;

import javax.swing.JFrame;
import robotsimulator.gui.MainApplet;

/*
 * Main entry point for the program when creating a runnable .jar file. 
 * Creates and runs MainApplet, and has methods for printing
 */
public class RobotSimulator
{
    //Use these methods to centralize output
    //Could later easily output to a textarea or file, log data, etc.
	public static void println(String m) 
	{
		System.out.println(m);
	}

	public static void halt() 
	{
		System.exit(0);
	}
	
	public static void main(String[] args) 
	{
		MainApplet m = new MainApplet();
		m.init();
		m.start();
		
		JFrame window = new JFrame("Robot Simulator");
		window.setContentPane(m);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
    }
}
