/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trackstim;

import java.util.prefs.Preferences;


import ij.IJ;
import ij.plugin.frame.PlugInFrame;
import mmcorej.CMMCore;

// provides the ui for track stim

// implements a ImageJ plugin interface
// **NOTE**: There is a big difference between a micro manager plugin and an imagej plugin
// this program was initially designed as an imageJ plugin, but is now wrapped inside a micromanager plugin
// to migrate it to new versions of micromanager
class TrackStimController {
    // save user preferences
    Preferences prefs;

    // intial state defaults
    // camera state
    int numFrames = 3000;
    int skipFrame = 1;
    int cameraExposureMsIndex = 0;
    int cameraCycleLengthMsIndex = 0;

    // tracker state
    boolean useClosestTracking = true;
    boolean useCenterOfMassTracking = false;
    boolean useManualTracking = false;
    boolean useRightSideScreenTracking = false;
    boolean useFullFieldImaging = false;
    boolean useBrightFieldImaging = false;
    boolean saveXYPositionsAsTextFile = false;
    int stageAccelerationFactor = 1;
    String detectionAlgorithm = "Yen";

    // stimulator state
    boolean enableStimulator = false;
    int preStimulationTimeMs = 5000;
    int stimulationDurationMs = 1000;
    int stimulationStrength = 63;
    int stimulationCycleLengthMs = 5000;
    int numStimulationCycles = 5;
    boolean useRamp = false;
    int rampBase = 0;
    int rampStart = 0;
    int rampEnd = 63;

    // main control objects/state
    // initalized in constructor
    CMMCore mmc;
    String savePath;
    
    // gui
    TrackStimGUI gui;

    // Tracker/Stimulator objects
    Tracker tracker;
    Stimulator stimulator;

    public void runStimulation(){
        // stimulator.runStimulation();
    }

    public void handleReadyBtnPress(){

    }

    public void handleStopBtnPress(){

    }

    public void handleGoBtnPress(){
        
    }

    public TrackStimController(CMMCore mmc_){
        // get micromanager core
        mmc = mmc_;

        // set up stimulator
        stimulator = new Stimulator(mmc_);
        if( !stimulator.initialize() ){
            IJ.log("TrackStimController Constructor: could not initialize stimulator.  Stimulator related options will not work");
        }

        savePath = "";

        // initialize gui
        gui = new TrackStimGUI(this);
    }
}