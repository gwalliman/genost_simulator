﻿#pragma checksum "C:\Users\IAN\Documents\GitHub\robotsimulator\C#_Simulator\RobotSimulator_AdHoc\SilverlightTest\MainPage.xaml" "{406ea660-64cf-4c82-b6f0-42d48172a799}" "8C01FFF95077687215C321639DD68423"
//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Runtime Version:4.0.30319.18408
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

using System;
using System.Windows;
using System.Windows.Automation;
using System.Windows.Automation.Peers;
using System.Windows.Automation.Provider;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Interop;
using System.Windows.Markup;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Media.Imaging;
using System.Windows.Resources;
using System.Windows.Shapes;
using System.Windows.Threading;


namespace SilverlightTest {
    
    
    public partial class RobotSimulator : System.Windows.Controls.UserControl {
        
        internal System.Windows.Controls.Grid LayoutRoot;
        
        internal System.Windows.Controls.Canvas canvas1;
        
        internal System.Windows.Controls.Button forwardButton;
        
        internal System.Windows.Controls.Button leftButton;
        
        internal System.Windows.Controls.Button rightButton;
        
        internal System.Windows.Controls.Button reverseButton;
        
        internal System.Windows.Controls.Button stopButton;
        
        internal System.Windows.Controls.CheckBox manualControlCheckBox;
        
        internal System.Windows.Controls.CheckBox robotConnectionCheckBox;
        
        internal System.Windows.Controls.Image theRobot;
        
        internal System.Windows.Controls.Label frontSensorLabel;
        
        internal System.Windows.Controls.Label leftSensorLabel;
        
        internal System.Windows.Controls.Label rightSensorLabel;
        
        internal System.Windows.Controls.Label rearSensorLabel;
        
        internal System.Windows.Controls.Label sonarLabel;
        
        internal System.Windows.Controls.Button autonomousButton;
        
        internal System.Windows.Controls.TextBox textBox1;
        
        internal System.Windows.Controls.ComboBox defaultActionComboBox;
        
        internal System.Windows.Controls.Button addProgrammingIfButton;
        
        internal System.Windows.Controls.Button executeProgrammingButton;
        
        internal System.Windows.Controls.Button resetButton;
        
        internal System.Windows.Controls.Label label1;
        
        internal System.Windows.Controls.CheckBox useActualSonars;
        
        private bool _contentLoaded;
        
        /// <summary>
        /// InitializeComponent
        /// </summary>
        [System.Diagnostics.DebuggerNonUserCodeAttribute()]
        public void InitializeComponent() {
            if (_contentLoaded) {
                return;
            }
            _contentLoaded = true;
            System.Windows.Application.LoadComponent(this, new System.Uri("/SilverlightTest;component/MainPage.xaml", System.UriKind.Relative));
            this.LayoutRoot = ((System.Windows.Controls.Grid)(this.FindName("LayoutRoot")));
            this.canvas1 = ((System.Windows.Controls.Canvas)(this.FindName("canvas1")));
            this.forwardButton = ((System.Windows.Controls.Button)(this.FindName("forwardButton")));
            this.leftButton = ((System.Windows.Controls.Button)(this.FindName("leftButton")));
            this.rightButton = ((System.Windows.Controls.Button)(this.FindName("rightButton")));
            this.reverseButton = ((System.Windows.Controls.Button)(this.FindName("reverseButton")));
            this.stopButton = ((System.Windows.Controls.Button)(this.FindName("stopButton")));
            this.manualControlCheckBox = ((System.Windows.Controls.CheckBox)(this.FindName("manualControlCheckBox")));
            this.robotConnectionCheckBox = ((System.Windows.Controls.CheckBox)(this.FindName("robotConnectionCheckBox")));
            this.theRobot = ((System.Windows.Controls.Image)(this.FindName("theRobot")));
            this.frontSensorLabel = ((System.Windows.Controls.Label)(this.FindName("frontSensorLabel")));
            this.leftSensorLabel = ((System.Windows.Controls.Label)(this.FindName("leftSensorLabel")));
            this.rightSensorLabel = ((System.Windows.Controls.Label)(this.FindName("rightSensorLabel")));
            this.rearSensorLabel = ((System.Windows.Controls.Label)(this.FindName("rearSensorLabel")));
            this.sonarLabel = ((System.Windows.Controls.Label)(this.FindName("sonarLabel")));
            this.autonomousButton = ((System.Windows.Controls.Button)(this.FindName("autonomousButton")));
            this.textBox1 = ((System.Windows.Controls.TextBox)(this.FindName("textBox1")));
            this.defaultActionComboBox = ((System.Windows.Controls.ComboBox)(this.FindName("defaultActionComboBox")));
            this.addProgrammingIfButton = ((System.Windows.Controls.Button)(this.FindName("addProgrammingIfButton")));
            this.executeProgrammingButton = ((System.Windows.Controls.Button)(this.FindName("executeProgrammingButton")));
            this.resetButton = ((System.Windows.Controls.Button)(this.FindName("resetButton")));
            this.label1 = ((System.Windows.Controls.Label)(this.FindName("label1")));
            this.useActualSonars = ((System.Windows.Controls.CheckBox)(this.FindName("useActualSonars")));
        }
    }
}

