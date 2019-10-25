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
class TrackStimGUI extends PlugInFrame implements ActionListener, ImageListener, MouseListener, ItemListener {
    // globals just in this class
    Preferences prefs;
    TextField framenumtext;
    TextField skiptext;

    // filed used in Tracker
    Tracker tt = null;

    java.awt.Checkbox closest;// target definition method.
    java.awt.Checkbox right;// field for tacking source
    java.awt.Checkbox objective_ten;// change stage movement velocity
    java.awt.Checkbox objective_40;// change stage movement velocity
    java.awt.Checkbox textpos;// if save xy pos data into txt file. not inclued z.
    java.awt.Choice acceleration;
    java.awt.Choice thresholdmethod;
    java.awt.Checkbox CoM;// center of mass method
    java.awt.Checkbox manualtracking;
    java.awt.Checkbox FULL;// full size filed.
    java.awt.Checkbox BF;// Bright field tracking only works with full size
    TextField savedir;
    String dir;

    // camera trigger
    java.awt.Choice exposureduration;
    java.awt.Choice cyclelength;

    // Stimulation
    Stimulator stimulator;
    java.awt.Checkbox STIM;
    TextField prestimulation;
    TextField stimstrength;
    TextField stimduration;
    TextField stimcyclelength;
    TextField stimcyclenum;
    java.awt.Checkbox ramp;// ramp
    TextField rampbase;
    TextField rampstart;
    TextField rampend;

    // pass to Tracker
    CMMCore mmc_;
    ImagePlus imp;
    ImageCanvas ic;
    String dirforsave;
    int frame = 1200;
    boolean ready;

    public TrackStimGUI(CMMCore cmmcore) {
        super("TrackerwithStimulater");
        mmc_ = cmmcore;
        IJ.log("TrackStimGUI Constructor: MMCore initialized");

        prefs = Preferences.userNodeForPackage(this.getClass());// make instance?
        imp = WindowManager.getCurrentImage();
        ImageWindow iw = imp.getWindow();
        ic = iw.getCanvas();
        ic.addMouseListener(this);

        stimulator = new Stimulator(mmc_);
        boolean stimulatorConnected = stimulator.initialize();
        if( !stimulatorConnected ){
            IJ.log("TrackStimGUI Constructor: could not initialize stimulator.  Stimulator related options will not work");
        }


        // try to get preferences directory and frame
        try {
            dir = prefs.get("DIR", "");
            IJ.log("pref dir " + dir);
            if (prefs.get("FRAME", "") == "") {
                frame = 3000;
            } else {
                frame = Integer.parseInt(prefs.get("FRAME", ""));
            }
            IJ.log("TrackStimGUI Constructor: Frame value is " + String.valueOf(frame));
        } catch (java.lang.Exception e){
            IJ.log("TrackStimGUI Constructor: Could not get frame value from preferences obj");
            IJ.log(e.getMessage());
        }

        if (dir == "") {
            // check directry in the current
            dir = IJ.getDirectory("current");
            if (dir == null) {
                dir = IJ.getDirectory("home");
            }
        }
        IJ.log("TrackStimGUI Constructor: initial dir is " + dir);

        ImagePlus.addImageListener(this);
        requestFocus(); // may need for keylistener

        // Prepare GUI
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();

        Button b = new Button("Ready");
        b.setPreferredSize(new Dimension(100, 60));
        b.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbl.setConstraints(b, gbc);
        add(b);

        Button b2 = new Button("Go");
        b2.setPreferredSize(new Dimension(100, 60));
        b2.addActionListener(this);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbl.setConstraints(b2, gbc);
        add(b2);

        Button b3 = new Button("Stop");
        b3.setPreferredSize(new Dimension(100, 60));
        b3.addActionListener(this);
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbl.setConstraints(b3, gbc);
        add(b3);

        Label labelexpduration = new Label("exposure");
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbl.setConstraints(labelexpduration, gbc);
        add(labelexpduration);

        exposureduration = new Choice();
        exposureduration.setPreferredSize(new Dimension(80, 20));
        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        exposureduration.add("0");
        exposureduration.add("1");
        exposureduration.add("10");
        exposureduration.add("50");
        exposureduration.add("100");
        exposureduration.add("200");
        exposureduration.add("500");
        exposureduration.add("1000");
        exposureduration.addItemListener(this);
        gbl.setConstraints(exposureduration, gbc);
        add(exposureduration);

        Label labelcameracyclelength = new Label("cycle len.");
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbl.setConstraints(labelcameracyclelength, gbc);
        add(labelcameracyclelength);

        cyclelength = new Choice();
        cyclelength.setPreferredSize(new Dimension(80, 20));
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        cyclelength.add("0");
        cyclelength.add("50");
        cyclelength.add("100");
        cyclelength.add("200");
        cyclelength.add("500");
        cyclelength.add("1000");
        cyclelength.add("2000");
        cyclelength.addItemListener(this);
        gbl.setConstraints(cyclelength, gbc);
        add(cyclelength);

        Label labelframe = new Label("Frame num");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbl.setConstraints(labelframe, gbc);
        add(labelframe);

        framenumtext = new TextField(String.valueOf(frame), 5);
        framenumtext.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbl.setConstraints(framenumtext, gbc);
        add(framenumtext);

        closest = new Checkbox("Just closest", true);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbl.setConstraints(closest, gbc);
        add(closest);

        acceleration = new Choice();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        acceleration.add("1x");
        acceleration.add("2x");
        acceleration.add("4x");
        acceleration.add("5x");
        acceleration.add("6x");
        gbl.setConstraints(acceleration, gbc);
        add(acceleration);

        thresholdmethod = new Choice();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        thresholdmethod.add("Yen");// good for normal
        thresholdmethod.add("Triangle");// good for on coli
        thresholdmethod.add("Otsu");// good for ventral cord?

        thresholdmethod.add("Default");
        thresholdmethod.add("Huang");
        thresholdmethod.add("Intermodes");
        thresholdmethod.add("IsoData");
        thresholdmethod.add("Li");
        thresholdmethod.add("MaxEntropy");
        thresholdmethod.add("Mean");
        thresholdmethod.add("MinError(I)");
        thresholdmethod.add("Minimum");
        thresholdmethod.add("Moments");
        thresholdmethod.add("Percentile");
        thresholdmethod.add("RenyiEntropy");
        thresholdmethod.add("Shanbhag");
        thresholdmethod.setPreferredSize(new Dimension(80, 20));
        gbl.setConstraints(thresholdmethod, gbc);
        add(thresholdmethod);

        right = new Checkbox("Use right", false);
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbl.setConstraints(right, gbc);
        add(right);

        textpos = new Checkbox("Save xypos file", false);
        gbc.gridx = 6;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbl.setConstraints(textpos, gbc);
        add(textpos);

        Label labelskip = new Label("one of ");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbl.setConstraints(labelskip, gbc);
        add(labelskip);

        skiptext = new TextField("1", 2);
        skiptext.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbl.setConstraints(skiptext, gbc);
        add(skiptext);

        CoM = new Checkbox("Center of Mass", false);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbl.setConstraints(CoM, gbc);
        add(CoM);

        manualtracking = new Checkbox("manual track", false);
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbl.setConstraints(manualtracking, gbc);
        add(manualtracking);

        FULL = new Checkbox("Full field", false);
        gbc.gridx = 5;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbl.setConstraints(FULL, gbc);
        add(FULL);

        BF = new Checkbox("Bright field", false);
        gbc.gridx = 6;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbl.setConstraints(BF, gbc);
        add(BF);

        Label labeldir = new Label("Save at");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbl.setConstraints(labeldir, gbc);
        add(labeldir);

        savedir = new TextField(dir, 40);
        savedir.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbl.setConstraints(savedir, gbc);
        add(savedir);
        gbc.fill = GridBagConstraints.NONE;// return to default

        Button b4 = new Button("Change dir");
        b4.addActionListener(this);
        gbc.gridx = 6;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbl.setConstraints(b4, gbc);
        add(b4);

        // gui for stimulation
        STIM = new Checkbox("Light", false);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbl.setConstraints(STIM, gbc);
        add(STIM);

        b = new Button("Run");
        b.setPreferredSize(new Dimension(40, 20));
        b.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(b, gbc);
        add(b);
        gbc.gridheight = 1;

        Label labelpre = new Label("Pre-stim");
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(labelpre, gbc);
        add(labelpre);

        prestimulation = new TextField(String.valueOf(10000), 6);
        prestimulation.setPreferredSize(new Dimension(50, 30));
        prestimulation.addActionListener(this);
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(prestimulation, gbc);
        add(prestimulation);

        Label labelstrength = new Label("Strength <63");
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(labelstrength, gbc);
        add(labelstrength);

        stimstrength = new TextField(String.valueOf(63), 6);
        stimstrength.setPreferredSize(new Dimension(50, 30));
        stimstrength.addActionListener(this);
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(stimstrength, gbc);
        add(stimstrength);

        Label labelduration = new Label("Duration");
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(labelduration, gbc);
        add(labelduration);

        stimduration = new TextField(String.valueOf(5000), 6);
        stimduration.setPreferredSize(new Dimension(50, 30));
        stimduration.addActionListener(this);
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(stimduration, gbc);
        add(stimduration);

        Label labelcyclelength = new Label("Cycle length");
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(labelcyclelength, gbc);
        add(labelcyclelength);

        stimcyclelength = new TextField(String.valueOf(10000), 6);
        stimcyclelength.setPreferredSize(new Dimension(50, 30));
        stimcyclelength.addActionListener(this);
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(stimcyclelength, gbc);
        add(stimcyclelength);

        Label labelcyclenum = new Label("Cycle num");
        gbc.gridx = 3;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(labelcyclenum, gbc);
        add(labelcyclenum);

        stimcyclenum = new TextField(String.valueOf(3), 6);
        stimcyclenum.setPreferredSize(new Dimension(50, 30));
        stimcyclenum.addActionListener(this);
        gbc.gridx = 4;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(stimcyclenum, gbc);
        add(stimcyclenum);

        ramp = new Checkbox("ramp", false);
        gbc.gridx = 5;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        // gbc.gridheight=3;
        gbc.anchor = GridBagConstraints.NORTH;
        gbl.setConstraints(ramp, gbc);
        add(ramp);

        Label labelbase = new Label("base");
        gbc.gridx = 6;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(labelbase, gbc);
        add(labelbase);

        rampbase = new TextField(String.valueOf(0), 3);
        rampbase.setPreferredSize(new Dimension(30, 30));
        rampbase.addActionListener(this);
        gbc.gridx = 7;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(rampbase, gbc);
        add(rampbase);

        Label labelrampstart = new Label("start");
        gbc.gridx = 6;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(labelrampstart, gbc);
        add(labelrampstart);

        rampstart = new TextField(String.valueOf(0), 3);
        rampstart.setPreferredSize(new Dimension(30, 30));
        rampstart.addActionListener(this);
        gbc.gridx = 7;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(rampstart, gbc);
        add(rampstart);

        Label labelrampend = new Label("end");
        gbc.gridx = 6;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(labelrampend, gbc);
        add(labelrampend);

        rampend = new TextField(String.valueOf(63), 3);
        rampend.setPreferredSize(new Dimension(30, 30));
        rampend.addActionListener(this);
        gbc.gridx = 7;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(rampend, gbc);
        add(rampend);
        setSize(700, 225);
        setVisible(true);
    }

    // method required by ItemListener
    public void itemStateChanged(ItemEvent e) {
        int selectedindex = 0;
        int exposurechoice = 0;
        int lengthchoice = 0;
        int sendingdata = 0;
        Choice cho = (Choice) e.getSource();
        selectedindex = cho.getSelectedIndex();

        IJ.log("itemStateChanged: the item at index " + String.valueOf(selectedindex) + " has been selected");

        if (e.getSource() == exposureduration || e.getSource() == cyclelength) {
            /*
             * arduino code int triggerlengtharray[]={ 0,1,10,50,100,200,500,1000};//3bit 8
             * values msec. int cyclelengtharray[]={ 0,50,100,200,500,1000,2000};//use 3bit
             */
            exposurechoice = exposureduration.getSelectedIndex();
            lengthchoice = cyclelength.getSelectedIndex();
            if ((exposurechoice == 1 || exposurechoice == 2) && lengthchoice == 0) {
                cyclelength.select(1);
                lengthchoice = cyclelength.getSelectedIndex();
            } else if (lengthchoice + 1 <= exposurechoice) {
                cyclelength.select(exposurechoice - 1);
                lengthchoice = cyclelength.getSelectedIndex();
            }
            sendingdata = 1 << 6 | lengthchoice << 3 | exposurechoice;

            try {
                stimulator.updateStimulatorSignal(exposurechoice, lengthchoice);

            } catch (java.lang.Exception ex){
                IJ.log("itemStateChanged: error trying to update the stimulator signal");
                IJ.log(ex.getMessage());
            }
        }

    }

    // handle button presses
    public void actionPerformed(ActionEvent e) {
        String itemChanged = e.getActionCommand();

        IJ.log("actionPerformed: button clicked is " + itemChanged);

        // frame num changed
        if (itemChanged.equals(framenumtext.getText())){
            validateFrameField();
            IJ.log("actionPerformed: frame is " + String.valueOf(frame));

            // savedir has changed
        } else if (itemChanged.equals(savedir.getText())){
            validateDirField();
            IJ.log("actionPerformed: directory is " + savedir.getText());

            // any button pushed
        } else {

            // ready button pushed
            if (itemChanged.equals("Ready")) {
                ready = true;
                validateAndStartTracker();

                // go button pressed
            } else if (itemChanged.equals("Go")) {

                // frame number is valid and directory exists
                if (validateFrameField() && validateDirField()) {
                    dir = savedir.getText();
                    createNewTempDirectory(dir);
                    stopTracker();

                    ready = false;

                    if (STIM.getState()) {
                        validateAndStartStimulation();
                    }
                    validateAndStartTracker();
                }
            } else if (itemChanged.equals("Stop")) {
                stopTracker();

            } else if (itemChanged.equals("Change dir")) {
                DirectoryChooser dc = new DirectoryChooser("Directory for temp folder");
                String dcdir = dc.getDirectory();
                savedir.setText(dcdir);
            } else if (itemChanged.equals("Run")){
                IJ.log("actionPerformed: RUN was pressed, running stimulation");
                validateAndStartStimulation();
            }
        }
    }

    // kill the Tracker, and also stop the image acquisition loop
    void stopTracker(){
        if (tt != null && tt.isAlive()){
            try {
                mmc_.stopSequenceAcquisition();
            } catch (java.lang.Exception ex) {
                IJ.log("TrackStim.stopTracker: error trying to stop tracker");
                IJ.log(ex.getMessage());
            }
        }
        tt = null;
    }

    // given directoryRoot d, find the number of sub directories sd in d that are of the form tempN where N is a number
    // create a new temp directory tempN+1
    // e.g. the highest current temp directory is temp10
    // create temp directory temp11
    void createNewTempDirectory(String directoryRoot){
        File currentDir = new File(directoryRoot);
        File[] fileList = currentDir.listFiles();
        int dirCount = 0;


        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                dirCount++;
            }
        }

        int i = 1;
        File newDir = new File(directoryRoot + "temp" + String.valueOf(dirCount + i));
        while (newDir.exists()) {
            i++;
            newDir = new File(directoryRoot + "temp" + String.valueOf(dirCount + i));
        }

        newDir.mkdir();
        dirforsave = newDir.getPath();// this one doen't have "/" at the end
        IJ.log("createNewTempDirectory: created new directory " + dirforsave);
    }

    void validateAndStartTracker(){
        tt = new Tracker(this);
        tt.start();
    }

    void validateAndStartStimulation(){
        try {
            int preStimulationVal = Integer.parseInt(prestimulation.getText());// initial delay
            int strengthVal = Integer.parseInt(stimstrength.getText());// strength
            int stimDurationVal = Integer.parseInt(stimduration.getText());// duration
            int stimCycleLengthVal = Integer.parseInt(stimcyclelength.getText());// cycle time
            int stimCycleVal = Integer.parseInt(stimcyclenum.getText());// repeat
            int rampBaseVal = Integer.parseInt(rampbase.getText());// base
            int rampStartVal = Integer.parseInt(rampstart.getText());// start
            int rampEndVal = Integer.parseInt(rampend.getText());// end
            boolean useRamp = ramp.getState();
            stimulator.runStimulation(useRamp, preStimulationVal, strengthVal, stimDurationVal, stimCycleLengthVal, stimCycleVal, rampBaseVal, rampStartVal, rampEndVal);

        } catch (java.lang.Exception e){
            IJ.log("TrackStimGUI.validateAndStartStimulation: error trying to run stimulation");
            IJ.log(e.getMessage());
        }
    }

    // validate the frame field in the UI
    boolean validateFrameField(){
        int testint;

        try {
            testint = Integer.parseInt(framenumtext.getText());
            frame = testint;
            prefs.put("FRAME", String.valueOf(frame));
            return true;
        } catch (java.lang.Exception e) {
            IJ.log("TrackStimGUI.validateFrameField: the current value of the frame field is not an int");
            IJ.log(e.getMessage());
            framenumtext.setText(String.valueOf(frame));
            return false;
        }
    }

    // validate the directory field in the UI
    boolean validateDirField(){
        String dirName = savedir.getText();
        File checkdir = new File(dirName);

        if (checkdir.exists()) {
            IJ.log("TrackStimGUI.checkDirField: directory " + dirName + " exists");
            dir = savedir.getText();
            prefs.put("DIR", dir); // save in the preference
            return true;
        } else {
            IJ.log("TrackStimGUI.checkDirField: directory " + dirName + " DOES NOT EXIST! Please create the directory first");
            savedir.setText(dir);
            return false;
        }
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
        IJ.log("mouseClicked: ");
        if (tt != null) {
            if (tt.isAlive()) {
                IJ.log("mouseClicked: tracking thread is active, changing target...");
                tt.changeTarget();
            }
        }
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