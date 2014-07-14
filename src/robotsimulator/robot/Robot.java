package robotsimulator.robot;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import robotsimulator.RobotSimulator;
import robotsimulator.Simulator;
import robotsimulator.worldobject.Block;

public class Robot implements Runnable
{
	private Simulator sim;
	private Block b;
	private volatile Thread robotThread;
	private int delay = 50;
	private boolean driveDist, rotateDist = false;
	private int distTotal, distGoal, angTotal, angGoal = 0;
	
	//Forward = f, Backwards = b                            -
	//Left = l, Right = r                                   -
	//Strafe Left = h, Strafe Right = i (Hook and slIce)    -Unused, I think
	//Stop = s                                              -Pause between commands / threads
	//Off = o                                               -Wait until execution begins (perma-stop)
	private char status = 'o';
	
	//What factor to divide the wait time between instructions by. 
    //Set to 1 for default (no modifier). 2 = twice as fast, etc 
	public static int speedModifier = 1;
	
	private ArrayList<SonarSensor> sonars = new ArrayList<SonarSensor>();
	
	public Robot(int cx, int cy, int a, Simulator s)
	{
		sim = s;
		int centerX = cx;
		int centerY = cy;
		
		//Degrees, 0 is direct north, counterclockwise
		int angle = a;

		b = new Block(20, 30, centerX, centerY, angle, sim);
	}

	public Block getBlock() 
	{
		return b;
	}
	
	public ArrayList<SonarSensor> getSonarSensors() 
	{
		return sonars;
	}
	
	public void addSonar(Simulator s, String n, double x, double y, int l, int a)
	{
		sonars.add(new SonarSensor(s, n, x + b.getCenterX(), y + b.getCenterY(), l, a, false));
	}
	
	public void addSonar(Simulator s, String n, double x, double y, int l, int a, int fov)
	{
		sonars.add(new SonarSensor(s, n, x + b.getCenterX(), y + b.getCenterY(), l, a, fov, false));
	}
        
        public void addSonarAbsolute(Simulator s, String n, double x, double y, int l, int a)
	{
		sonars.add(new SonarSensor(s, n, x, y, l, a, true));
	}
        
        public void addSonarAbsolute(Simulator s, String n, double x, double y, int l, int a, int fov)
	{
		sonars.add(new SonarSensor(s, n, x, y, l, a, fov, true));
	}
	
	public SonarSensor getSonarSensor(int num) 
	{
		return sonars.get(num);
	}
	
	public double getCenterX() 
	{
		return b.getCenterX();
	}
	
	public double getCenterY() 
	{
		return b.getCenterY();
	}
	
	public double getX0()
	{
		return b.getX0();
	}
	
	public double getY0()
	{
		return b.getY0();
	}
	
	public double getX1()
	{
		return b.getX1();
	}
	
	public double getY1()
	{
		return b.getY1();
	}
	
	public double getX2()
	{
		return b.getX2();
	}
	
	public double getY2()
	{
		return b.getY2();
	}
	
	public double getX3()
	{
		return b.getX3();
	}
	
	public double getY3()
	{
		return b.getY3();
	}
	
	public double getCenterFrontX()
	{
		return (getX0() + getX1()) / 2;
	}
	
	public double getCenterFrontY()
	{
		return (getY0() + getY1()) / 2;
	}
	
	public double getCenterLeftX()
	{
		return (getX0() + getX2()) / 2;
	}
	
	public double getCenterLeftY()
	{
		return (getY0() + getY2()) / 2;
	}
	
	public double getCenterRightX()
	{
		return (getX1() + getX3()) / 2;
	}
	
	public double getCenterRightY()
	{
		return (getY1() + getY3()) / 2;
	}
	
	public double getCenterRearX()
	{
		return (getX2() + getX3()) / 2;
	}
	
	public double getCenterRearY()
	{
		return (getY2() + getY3()) / 2;
	}
	
	public double getAngle() 
	{
		return b.getDegAngle();
	}
	
	public char getStatus()
	{
		return status;
	}

	public void drive(char d)
	{
		if(robotThread == null)
		{
                    if(d == 'f')
                    {
                        RobotSimulator.println("Driving forward");
                    }
                    else if(d == 'b')
                    {
                        RobotSimulator.println("Driving backwards");
                    }
                    status = d;
                    robotThread = new Thread(this);
                    robotThread.start();
		}
	}
	
	public void drive(int dist)
	{
		if(robotThread == null)
		{
			if(dist < 0)
			{
				status = 'b';
			}
			else if(dist > 0)
			{
				status = 'f';
			}
                        
			if(status == 'f')
                        {
                            RobotSimulator.println("Driving forward " + Integer.toString(dist) + " pixels");
                        }
                        else if(status == 'b')
                        {
                            RobotSimulator.println("Driving backwards " + Integer.toString(dist) + " pixels");
                        }
                        
			driveDist = true;
			distGoal = Math.abs(dist);
			distTotal = 0;

			robotThread = new Thread(this);
			robotThread.start();
		}
	}
	
	public void turn(char d)
	{
		if(robotThread == null)
		{
                    if(d == 'l')
                    {
                        RobotSimulator.println("Turning left");
                    }
                    else if(d == 'r')
                    {
                        RobotSimulator.println("Turning right");
                    }
                    status = d;
                    robotThread = new Thread(this);
                    robotThread.start();
		}
	}
	
	public void turn(int angle)
	{
		if(robotThread == null && angle != 0)
		{
			if(angle < 0)
			{
				status = 'l';
			}
			else if(angle > 0)
			{
				status = 'r';
			}
			
                        if(status == 'l')
                        {
                            RobotSimulator.println("Turning left " + Integer.toString(angle) + " degrees");
                        }
                        else if(status == 'r')
                        {
                            RobotSimulator.println("Turning right " + Integer.toString(angle) + " degrees");
                        }
                        
                        
			rotateDist = true;
			angGoal = angle;
			angTotal = 0;
			robotThread = new Thread(this);
			robotThread.start();
		}
	}
	
	//Puts the robot on 'stopped' state and allows execution to begin
	public void start()
	{
		status = 's';
		robotThread = null;
	}

	//Once a thread finishes executing, it calls stop to end that step of commands
	public void stop()
	{
            RobotSimulator.println("Stopping");
		status = 's';
                robotThread = null;

                //Used to determine if we have finished maze
                b.checkCollision();
	}

    //Main method of the robot-- determines what to do based on status code
    @Override
	public void run() 
	{
		Thread me = Thread.currentThread();
		while(robotThread == me)
		{
			long beforeTime, timeDiff, sleep;
	        beforeTime = System.currentTimeMillis();
	        
	        double oldX, oldY, oldA;
			
			switch(status)
			{
				case 'o':
					//Idle until we're told to execute				
				break;
				case 'f':
					oldX = b.getCenterX();
					oldY = b.getCenterY();
					b.translate(1);
					for(SonarSensor s : sonars)
					{
						s.translate(b.getCenterX() - oldX, b.getCenterY() - oldY);
					}
					if(driveDist)
					{
						double curX = b.getCenterX();
						double curY = b.getCenterY();
						distTotal += Math.hypot(curX - oldX, curY - oldY);

						if(distTotal >= distGoal)
						{
							driveDist = false;
							distTotal = 0;
							distGoal = 0;
							stop();
						}
					}
				break;
				case 'b':
					oldX = b.getCenterX();
					oldY = b.getCenterY();
					b.translate(-1);
					for(SonarSensor s : sonars)
					{
						s.translate(b.getCenterX() - oldX, b.getCenterY() - oldY);
					}
					if(driveDist)
					{
						double curX = b.getCenterX();
						double curY = b.getCenterY();
						distTotal += Math.hypot(curX - oldX, curY - oldY);

						if(distTotal >= distGoal)
						{
							driveDist = false;
							distTotal = 0;
							distGoal = 0;
							stop();
						}
					}
				break;
				case 'l':
					oldA = b.getDegAngle();
					b.rotate(-1);
					for(SonarSensor s : sonars)
					{
						s.rotate(b.getDegAngle() - oldA);
					}
					if(rotateDist)
					{
						double curAngle = b.getDegAngle();
						if(curAngle > oldA)
						{
							angTotal += curAngle - 360 - oldA;
						}
						else
						{
							angTotal += curAngle - oldA;
						}

						if(angGoal >= angTotal)
						{
							rotateDist = false;
							angTotal = 0;
							angGoal = 0;
							stop();
						}
					}
				break;
				case 'r':
					oldA = b.getDegAngle();
					b.rotate(1);
					for(SonarSensor s : sonars)
					{
						s.rotate(b.getDegAngle() - oldA);					
					}
					if(rotateDist)
					{
						double curAngle = b.getDegAngle();
						if(curAngle < oldA)
						{
							angTotal += curAngle + 360 - oldA;
						}
						else
						{
							angTotal += curAngle - oldA;
						}

						if(angGoal <= angTotal)
						{
							rotateDist = false;
							angTotal = 0;
							angGoal = 0;
							stop();
						}
					}
				break;
			}
			
			timeDiff = System.currentTimeMillis() - beforeTime;
	        sleep = (delay / speedModifier) - timeDiff;
	        
	        if(sleep <= 0) sleep = 1;
	        
	        try 
			{
				Thread.sleep(sleep);
			} 
			catch (InterruptedException e) 
			{
				RobotSimulator.println("InterruptedException");
			}
			
            beforeTime = System.currentTimeMillis();
		}
	}
	
	//Cancels execution and stops moving. Prep for next execution. 
	public void abort()
	{
		status = 'o';
		robotThread = null;
	}
	
	public void export(Document doc) 
	{
		Element root = doc.getDocumentElement();
		Element robotElement = doc.createElement("robot");
		root.appendChild(robotElement);

		Element xe = doc.createElement("x");
		xe.appendChild(doc.createTextNode(Double.toString(getCenterX())));
		robotElement.appendChild(xe);
		
		Element ye = doc.createElement("y");
		ye.appendChild(doc.createTextNode(Double.toString(getCenterY())));
		robotElement.appendChild(ye);
		
		Element ae = doc.createElement("a");
		ae.appendChild(doc.createTextNode(Double.toString(getAngle())));
		robotElement.appendChild(ae);
		
		Element sonarParent = doc.createElement("sonars");
		robotElement.appendChild(sonarParent);
		
		for(SonarSensor s : sonars)
		{
			Element sonarElement = doc.createElement("sonar");
			sonarParent.appendChild(sonarElement);
			s.export(doc, sonarElement);
		}
	}
	
	//Debug function: prints out the names of each sensor it owns
    //Use this to verify sensors exist and check values
	public void printSensors()
	{
		System.out.println("[Sensors]");
		int n = 0;
		for (SonarSensor s : sonars)
		{
			RobotSimulator.println("Sensor " + n + ": " + s.getLabel());
			n++;
		}	
	}	
}
