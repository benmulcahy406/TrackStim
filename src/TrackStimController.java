import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.prefs.Preferences;


import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.frame.PlugInFrame;
import ij.io.DirectoryChooser;
import mmcorej.CMMCore;

// provides the ui for track stim

// implements a ImageJ plugin interface
// **NOTE**: There is a big difference between a micro manager plugin and an imagej plugin
// this program was initially designed as an imageJ plugin, but is now wrapped inside a micromanager plugin
// to migrate it to new versions of micromanager
class TrackStimController extends PlugInFrame implements ImageListener, MouseListener {
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
    ImagePlus imp;
    ImageCanvas ic;
    String savePath;
    boolean ready;

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
        super("TrackStim");

        // get micromanager core
        mmc = mmc_;

        // set up imagej specifc stuff
        // // not sure if this is needed
        // imp = WindowManager.getCurrentImage();
        // ic = imp.getWindow().getCanvas();
        // ic.addMouseListener(this);
        // ImagePlus.addImageListener(this);
        // requestFocus();

        // set up stimulator
        stimulator = new Stimulator(mmc_);
        if( !stimulator.initialize() ){
            IJ.log("TrackStimController Constructor: could not initialize stimulator.  Stimulator related options will not work");
        }

        savePath = "";
        ready = false;

        // get initial directory to save to
        // savePath = prefs.get("savePath", "");
        // if( savePath == "" ){
        //     savePath = IJ.getDirectory("current");
        // }

        // initialize gui
        TrackStimGUI gui = new TrackStimGUI(this);
        gui.setVisible(true);
    }


        // Write logic to clear valables when image closed here
        public void imageClosed(ImagePlus impc) {
            IJ.log("imageClosed: cleaning up");
            if (imp == impc) {
                imp = null;
                IJ.log("imageClosed: imp set to null");
            }
        }
    
        // Handle mouse click
        public void mouseClicked(MouseEvent e) {
        }
    
    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
    }

    /** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
    }

    /** Handle the key-released event from the text field. */
    public void keyReleased(KeyEvent e) {
    }

    public void imageOpened(ImagePlus imp) {
    }

    public void imageUpdated(ImagePlus imp) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
    
}