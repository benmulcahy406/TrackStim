/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trackstim;

import ij.IJ;
import mmcorej.CMMCore;

// provides the ui for track stim

// implements a ImageJ plugin interface
// **NOTE**: There is a big difference between a micro manager plugin and an imagej plugin
// this program was initially designed as an imageJ plugin, but is now wrapped inside a micromanager plugin
// to migrate it to new versions of micromanager
class TrackStimController {
    // main control objects/state
    // initalized in constructor
    CMMCore mmc;
    
    // gui
    TrackStimGUI gui;

    // Tracker/Stimulator objects
    Tracker tracker;
    Stimulator stimulator;

    public TrackStimController(CMMCore mmc_){
        // get micromanager core
        mmc = mmc_;

        // set up stimulator
        stimulator = new Stimulator(mmc_);
        if( !stimulator.initialize() ){
            IJ.log("TrackStimController Constructor: could not initialize stimulator.  Stimulator related options will not work");
        }
        
        // initialize gui
        gui = new TrackStimGUI(this);
    }

    public void handleRunStimulationBtnPress(){
        if( gui.uiValuesAreValid() && stimulator.initialized ){
            try {
                TrackStimParameters tsp = gui.getTrackStimParameters();
                stimulator.runStimulation(tsp);
            } catch (java.lang.Exception e){
                IJ.showMessage("Unable to start stimulator");
                IJ.log(e.getMessage());
            }
        } else {
            IJ.showMessage("Unable to start stimulation, some values are not valid.  Ensure every text input is a number and that the chosen directory exists.");
        }
    }

    public void handleStopBtnPress(){
        gui.enableControls();
    }

    public void handleGoBtnPress(){
        try {
            gui.disableControls();
            TrackStimParameters tsp = gui.getTrackStimParameters();
            // do stuff with tsp
        } catch (java.lang.Exception e){
            IJ.showMessage("Unable to run TrackStim, some values are not valid.  Ensure every text input is a number and that the chosen directory exists.");
        } 
    }

}