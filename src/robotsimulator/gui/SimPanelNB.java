/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package robotsimulator.gui;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;
import robotinterpreter.RobotInterpreter;
import robotsimulator.RobotSimulator;
import robotsimulator.Simulator;
import robotsimulator.robot.Robot;
import robotsimulator.robot.SonarSensor;

/**
 *
 * @author Garret
 */
public class SimPanelNB extends javax.swing.JPanel {
    
    private int fps;
    private MainApplet main;
    private Simulator sim;
    
    //Simulator variables
    private RobotInterpreter r;
    SwingWorker<Void, Void> executor;

    //Thread for updating sensor values
    sensorThread sThread;
	
    //Whether all necessary files are loaded in-- i.e. can we execute?
    private boolean readyToRun = false;
    DecimalFormat df = new DecimalFormat("#.0");    //Used by sensor output to make display not horrible
    private boolean highSpeed = false;
    private boolean guiStarted = false;

    /**
     * Creates new form SimPanelNB
     */
    public SimPanelNB(int w, int h, int f, Simulator s, MainApplet m) 
    {
        fps = f;
        sim = s;
        main = m;
                
        initComponents();
        int numItems = mazeComboBox.getItemCount();
        Map<String, ArrayList<String>> mazeItemMap = new HashMap<String, ArrayList<String>>();
        
        //Crappy hack to handle reordering the maze items due to the way I named them.
        for (int i = 0;  i < numItems; i++) 
        {
            String mazeName = (String) mazeComboBox.getItemAt(i);
            String[] mazeNameTokens = mazeName.split("_");
            String mazeNum = mazeNameTokens[0];
            char letter = mazeNum.charAt(mazeNum.length() - 1);
            if(!Character.isDigit(letter))
            {
                mazeNum = mazeNum.substring(0, mazeNum.length() - 1);
            }
            
            if(!mazeItemMap.containsKey(mazeNum))
            {
                mazeItemMap.put(mazeNum, new ArrayList<String>());
            }
            mazeItemMap.get(mazeNum).add(mazeName);
        }
        
        ArrayList<String> mapKeys = new ArrayList<String>();
        Set<String> keyMap = mazeItemMap.keySet();
        Map<String, ArrayList<String>> newMazeItemMap = new HashMap<String, ArrayList<String>>();
        
        for(String key : keyMap)
        {
            ArrayList<String> items = mazeItemMap.get(key);
            String lastItem = items.remove(items.size() - 1);
            items.add(0, lastItem);
            newMazeItemMap.put(key, items);
        }
        String[] sortedKeys = keyMap.toArray(new String[keyMap.size()]);
        Arrays.sort(sortedKeys);
        
        mazeComboBox.removeAllItems();
        for(String key : sortedKeys)
        {
            ArrayList<String> items = newMazeItemMap.get(key);
            for(String item : items)
            {
                mazeComboBox.addItem(item);
            }
        }
        //End crappy hack
        
        mazeComboBox.setSelectedItem(main.mazeId);
        outputTextArea.setEditable(false);
        outputTextArea.setBackground(Color.LIGHT_GRAY);
        guiStarted = true;
    }
    
    public void createStage(Simulator s)
    {
        sim = s;
        stageScrollPane.setViewportView(main.createStagePanel(main.stageWidth, main.stageHeight, fps, sim));
    }
    
    //Updates the runningLbl with what it's waiting on (code, maze, etc.) and its current status
    private void updateRunningStatus()
    {
        readyToRun = false;
        executeBtn.setEnabled(false);

        if (main.code == null)
        {
        }
        else if (main.mazeId == null)
        {
        }
        else
        {
            readyToRun = true;
            executeBtn.setEnabled(true);
            resetBtn.setEnabled(true);
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

        executeBtn.setEnabled(true);
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
        main.stageWidth = width;
        main.stageHeight = height;
        stageScrollPane.setViewportView(main.createStagePanel(width, height, fps, sim));
        sim.getWorld().setGridWidth(width);
        sim.getWorld().setGridHeight(height);
    }

    //Hacky method to reinitialize sensors and have them work again after the maze has changed
    public void reinitializeSensors()
    {
        //Pause the sensor thread
        stopSensorThread();

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

            sensorTextArea.setText(newSensorData);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mazeComboBox = new JComboBox(main.getMazesFromWeb());
        resetBtn = new javax.swing.JButton();
        stopBtn = new javax.swing.JButton();
        executeBtn = new javax.swing.JButton();
        stageScrollPane = new javax.swing.JScrollPane();
        speedBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        sensorTextArea = new javax.swing.JTextArea();

        setPreferredSize(new java.awt.Dimension(800, 600));

        mazeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mazeComboBoxActionPerformed(evt);
            }
        });

        resetBtn.setText("Reset");
        resetBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetBtnActionPerformed(evt);
            }
        });

        stopBtn.setText("Stop");
        stopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBtnActionPerformed(evt);
            }
        });

        executeBtn.setText("Execute");
        executeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeBtnActionPerformed(evt);
            }
        });

        stageScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        stageScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        speedBtn.setText("Slow");
        speedBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speedBtnActionPerformed(evt);
            }
        });

        outputTextArea.setColumns(20);
        outputTextArea.setRows(5);
        jScrollPane1.setViewportView(outputTextArea);

        sensorTextArea.setColumns(20);
        sensorTextArea.setRows(5);
        jScrollPane2.setViewportView(sensorTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(stageScrollPane)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                    .addComponent(mazeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(executeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                .addComponent(speedBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(stageScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 438, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(resetBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(executeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(speedBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(mazeComboBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 21, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mazeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mazeComboBoxActionPerformed
        //main.openNewMaze();
        JComboBox jcb = (JComboBox) evt.getSource();
        if(guiStarted)
            main.mazeId = (String) jcb.getSelectedItem();
        if(sim != null)
        {
            main.mazeXml = sim.mainApp.getMazeData(main.mazeId);
            sim.importStage(main.mazeXml);
            reinitializeSensors();
        }
    }//GEN-LAST:event_mazeComboBoxActionPerformed

    private void executeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeBtnActionPerformed
        if (readyToRun)
        {
            //Begin execution, enable the stopBtn, and disable ourselves
            executeBtn.setEnabled(false);
            stopBtn.setEnabled(true);
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

                    executeBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
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
    }//GEN-LAST:event_executeBtnActionPerformed

    private void stopBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopBtnActionPerformed
        //Stop execution, enable the runBtn, and disable ourselves
        stopExecution();
    }//GEN-LAST:event_stopBtnActionPerformed

    private void resetBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetBtnActionPerformed
        //Stop execution, and reload the maze
        stopExecution();

        if (main.mazeId != null)
        {
            sim.importStage(main.mazeXml);
            reinitializeSensors();
        }
    }//GEN-LAST:event_resetBtnActionPerformed

    private void speedBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speedBtnActionPerformed
        //Toggle between high and low speeds for robot
        //Speed is a multiplier-- e.g. sM=2 is twice as fast as sM=1
        
        if (highSpeed)
        {
                highSpeed = false;
                Robot.speedModifier = 1;
                speedBtn.setText("Slow");
        }
        else
        {
                highSpeed = true;
                Robot.speedModifier = 2;
                speedBtn.setText("Fast");
        }
    }//GEN-LAST:event_speedBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton executeBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox mazeComboBox;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JButton resetBtn;
    private javax.swing.JTextArea sensorTextArea;
    private javax.swing.JButton speedBtn;
    private javax.swing.JScrollPane stageScrollPane;
    private javax.swing.JButton stopBtn;
    // End of variables declaration//GEN-END:variables

    public void startSensorThread()
    {
        //Start up the sensor thread
        sThread = new sensorThread();
        (new Thread(sThread)).start();
    }
    
    private ArrayList<SonarSensor> getRobotSonars()
    {
        return sim.getRobot().getSonarSensors();
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
}
