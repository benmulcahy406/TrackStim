/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trackstim;
import ij.IJ;

import mmcorej.CharVector;
import mmcorej.CMMCore;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// send signals to the stimulator to turn on/off the LED light
class SignalSender implements Runnable {
    CMMCore mmc;
    int channel;
    int signal;
    String stimulatorPort;

    SignalSender(CMMCore cmmcore_, String stimulatorPort_) {
        mmc = cmmcore_;
        stimulatorPort = stimulatorPort_;
    }

    void setChannel(int channel_) {
        channel = channel_;
    }

    void setSignal(int signal_) {
        signal = signal_;
    }

    // send signal data to the stimulator through the serial port
    public void run() {
        IJ.log("SignalSender: system time is " + String.valueOf(System.nanoTime() / 1000000));
        IJ.log("SignalSender: signal is " + String.valueOf(signal));

        int signalData = channel << 7 | signal;
        CharVector signalDataVec = new CharVector();
        signalDataVec.add((char) signalData);

        try {
            mmc.writeToSerialPort(stimulatorPort, signalDataVec);
        } catch (java.lang.Exception e) {
            IJ.log("SignalSender: error trying to write data " + String.valueOf(signalDataVec) + " to the serial port " + stimulatorPort);
            IJ.log(e.getMessage());
        }
    }
}

class Stimulator {
    CMMCore mmc;
    String stimulatorPort;
    public boolean initialized = false;

    static final int STIMULATION_CHANNEL = 0; // the channel to send ths signals to
    static final String STIMULATOR_DEVICE_LABEL = "FreeSerialPort"; // hardcoded device label found in config trackstim-mm1.4.23mac.cfg

    Stimulator(CMMCore cmmcore){
        mmc = cmmcore;
        stimulatorPort = "";
    }

    // find and connect to the LED light stimulator
    // initialize the stimulator by sending an initial signal
    public boolean initialize(){
        boolean portFound = false;

        // see ./documentation/arduino.c line 16-21 for the signal format
        // binary 192 -> 11000000
           // 11 -> set trigger setting
           // 000 -> set lower three bits for tigger cycle
           // 000 -> set lower three bits for trigger length 
        // if we dont set trigger cycle and trigger length to 0,
        // we wont be able to turn the light on and off at the right times
        int initialSignal = (STIMULATION_CHANNEL << 8) | 192;
        CharVector initialSignalData = new CharVector();
        initialSignalData.add((char) initialSignal);


        try {
            stimulatorPort = mmc.getProperty(STIMULATOR_DEVICE_LABEL, "Port");

            // send initial signal to stimulator port
            mmc.writeToSerialPort(stimulatorPort, initialSignalData);
            portFound = true;

            IJ.log("stimulator port found");
            IJ.log("stimulator is connected at " + stimulatorPort);

        } catch (Exception e){
            IJ.log("error getting stimulator port");
            IJ.log(e.getMessage());
        }

        initialized = portFound;
        return portFound;
    }

    // schedules signals that will be run in the future at specific time points and intervals based on
    // the arguments:
    //    useRamp: whether to ramp up gradually to the full signal strength i.e. go from a weaker signal to a stronger signal
    //    preStimTimeMs: time in ms before any signals are sent
    //    signal: signal to send to the light -- usually 63 and it is rare if it is changed
    //    stimDurationMs: duration that the light is on in ms
    //    stimCycleDurationMs: duration that the light is off + duration that the light is on
    //    numStimCycles: number of cycles 
    //    rampBase: strength applied
    //    rampStart: signal at the start of the interval
    //    rampEnd: signal at the end of the interval
    void runStimulation(TrackStimParameters tsp) throws java.lang.Exception {
        if(!initialized){
            throw new Exception("could not run stimulation.  the stimulator is not initialized");
        }

        try {
            if (tsp.enableRamp) {
                // incrementally increase light strength
                int rampSignalDelta = Math.abs(tsp.rampEnd - tsp.rampStart);
                int rampSign = Integer.signum(tsp.rampEnd - tsp.rampStart);


                for (int i = 0; i < tsp.numStimulationCycles; i++) {

                    // schedule signals with incrementally increasing light strength
                    for (int j = 0; j < rampSignalDelta + 1; j++) {
                        scheduleSignal(tsp.preStimulationLengthMs + i * tsp.stimulationCycleLengthMs + j * (tsp.stimulationDurationMs / rampSignalDelta),
                                tsp.rampStart + j * rampSign);
                    }

                    // schedule signal to turn off light at end of cycle
                    scheduleSignal(tsp.preStimulationLengthMs + tsp.stimulationDurationMs + i * tsp.stimulationCycleLengthMs, tsp.rampBase);
                }
            } else {
                // send full light strength right away
                for (int i = 0; i < tsp.numStimulationCycles; i++) {
                    int signalTimePtBegin = tsp.preStimulationLengthMs + i * tsp.stimulationCycleLengthMs;
                    int signalTimePtEnd = signalTimePtBegin + tsp.stimulationDurationMs;

                    // schedule signal to turn on light at beginning cycle
                    scheduleSignal(signalTimePtBegin, tsp.stimulationStrength);

                    // schedule signal to turn off light at end of cycle
                    scheduleSignal(signalTimePtEnd, tsp.rampBase);
                }
            }
        } catch (java.lang.Exception e) {
            IJ.log("Stimulator.prepSignals: error sending signals");
            IJ.log(e.getMessage());
        }
    }

    // schedule a signal to be run at a specific timepoint(ms) in the future
    void scheduleSignal(int timePointMs, int signal) {
        SignalSender sd = new SignalSender(mmc, stimulatorPort);
        sd.setChannel(STIMULATION_CHANNEL);
        sd.setSignal(signal);

        ScheduledExecutorService ses;
        ScheduledFuture future = null;
        ses = Executors.newSingleThreadScheduledExecutor();

        IJ.log("Stimulator.scheduleSignal: timePointMs converted to microseconds is: " + String.valueOf(timePointMs * 1000));
        IJ.log("Stimulator.scheduleSignal: signal is: " + String.valueOf(signal));

        // convert the timepoint to microseconds
        // legacy decision, not sure why we need to do it like this
        future = ses.schedule(sd, timePointMs * 1000, TimeUnit.MICROSECONDS);
    }

    // send new camera exposure and cycle length values to the stimulator, 
    // which then sends them to the hamamatsu camera controller
    // NOTE: 
        // these arguments are indexes consisting of at most three bits i.e. in the range of [0, 8] 
        // each index is then mapped to a specific value
        // see documentation/arduino.c lines 41-44
    void updateCameraSettings(int newExposureIndex, int newCycleLengthIndex) throws java.lang.Exception {
        if(!initialized){
            throw new Exception("could not update camera settings.  the stimulator is not initialized");
        }

        // pack the new values into a buffer to send
        int newSettingsData = 1 << 6 | newCycleLengthIndex << 3 | newExposureIndex;

        CharVector newSettingsDataVec = new CharVector();
        newSettingsDataVec.add((char) newSettingsData);

        // send the data to the stimulator to set the new exposure and new cycle length
        try {
            mmc.writeToSerialPort(stimulatorPort, newSettingsDataVec);
        } catch(java.lang.Exception e){
            IJ.log("Stimulator.updateStimulatorSignal: unable to write new signal data to serial port");
            IJ.log(e.getMessage());
        }
    }
}