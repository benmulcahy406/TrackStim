/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trackstim;

/**
 *
 * @author dylan
 */
public class TrackStimParameters {
    int numFrames;
    int numSkipFrames;

    int preStimulationLengthMs;
    int stimulationCycleLengthMs;
    int stimulationDurationMs;
    int stimulationStrength;
    int numStimulationCycles;
    int rampBase;
    int rampStart;
    int rampEnd;

    boolean enableStimulator;
    boolean enableRamp;
    String saveDirectory;

    TrackStimParameters(
        int numFrames_, int numSkipFrames_,
        int preStimulationLengthMs_, int stimulationCycleLengthMs_,
        int stimulationDurationMs_, int stimulationStrength_, int numStimCycles_,
        int rampBase_, int rampStart_, int rampEnd_,
        boolean enableStimulator_, boolean enableRamp_,
        String saveDirectory_){
        
        this.numFrames = numFrames_;
        this.numSkipFrames = numSkipFrames_;

        this.preStimulationLengthMs = preStimulationLengthMs_;
        this.stimulationCycleLengthMs = stimulationCycleLengthMs_;
        this.stimulationDurationMs = stimulationDurationMs_;
        this.stimulationStrength = stimulationStrength_;
        this.numStimulationCycles = numStimCycles_;
        this.rampBase = rampBase_;
        this.rampStart = rampStart_;
        this.rampEnd = rampEnd_;

        this.enableStimulator = enableStimulator_;
        this.enableRamp = enableRamp_;
        this.saveDirectory = saveDirectory_;
    }
}
