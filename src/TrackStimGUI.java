import ij.io.DirectoryChooser;

import java.io.File;

import javax.swing.JOptionPane;


class TrackStimGUI extends javax.swing.JFrame {

    public TrackStimController tsc;
    /**
     * Creates new form TrackStimGUI
     */
    public TrackStimGUI(TrackStimController tsc_) {
        initComponents();

        tsc = tsc_;
        setInitialState();
    }

    // set initial state in the ui from the state in the controller
    void setInitialState(){

        // save directory text should only change when someone clicks a directory
        saveDirectoryText.setEditable(false);

        // set camera state
        numFramesText.setText(String.valueOf(tsc.numFrames));
        skipFrameText.setText(String.valueOf(tsc.skipFrame));
        cameraExposureSelector.setSelectedIndex(tsc.cameraExposureMsIndex);
        cameraCycleLengthSelector.setSelectedIndex(tsc.cameraCycleLengthMsIndex);

        // set tracker state
        useClosestTrackingCheckbox.setSelected(true);
        centerOfMassTrackingCheckbox.setSelected(false);
        manualTrackingCheckbox.setSelected(false);
        useRightCheckbox.setSelected(false);
        useFullfieldCheckbox.setSelected(false);
        useBrightFieldCheckbox.setSelected(false);
        saveXYPositionsAsTextCheckbox.setSelected(false);
        stageAccelerationSelector.setSelectedIndex(0);
        detectionAlgorithmSelector.setSelectedIndex(0);

        // set stimulator state
        enableStimulatorBtn.setSelected(false);
        preStimulationDurationMsText.setText(String.valueOf(tsc.preStimulationTimeMs));
        stimulationDurationMsText.setText(String.valueOf(tsc.stimulationDurationMs));
        stimulationStrengthText.setText(String.valueOf(tsc.stimulationStrength));
        stimulationCycleLengthText.setText(String.valueOf(tsc.stimulationCycleLengthMs));
        numStimulationCyclesText.setText(String.valueOf(tsc.numStimulationCycles));
        enableRampStimulationBtn.setSelected(false);
        rampBaseText.setText(String.valueOf(tsc.rampBase));
        rampStartText.setText(String.valueOf(tsc.rampStart));
        rampEndText.setText(String.valueOf(tsc.rampEnd));

        saveDirectoryText.setText(tsc.savePath);


        // validation logic to validate text fields whenever they have changed
        final TrackStimGUI g = this;
        numFramesText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(numFramesText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.numFrames = parsedValue;
                }
            }
        });

        skipFrameText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(skipFrameText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.skipFrame = parsedValue;
                }
            }
        });
           
        preStimulationDurationMsText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(preStimulationDurationMsText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.preStimulationTimeMs = parsedValue;
                }
            }
        });

        rampBaseText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(rampBaseText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.rampBase = parsedValue;
                }
            }
        });

        stimulationDurationMsText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(stimulationDurationMsText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.stimulationDurationMs = parsedValue;
                }
            }
        });

        stimulationStrengthText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(stimulationStrengthText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.stimulationStrength = parsedValue;
                }
            }
        });


        stimulationCycleLengthText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(stimulationCycleLengthText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.stimulationCycleLengthMs = parsedValue;
                }
            }
        });

        numStimulationCyclesText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(numStimulationCyclesText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.numStimulationCycles = parsedValue;
                }
            }
        });

        rampStartText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(rampStartText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.rampStart = parsedValue;
                }
            }
        });

        rampEndText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validate();
            }

            public void validate() {
                int[] result = validateNumberInput(rampEndText.getText(), Integer.valueOf(0), null);
                boolean isValid = result[0] != 0;
                int parsedValue = result[1];

                if(isValid){
                    tsc.rampEnd = parsedValue;
                }
            }
        });
    }
    // first element of int[] is whether the input is valid, second is the parsed int
    int[] validateNumberInput(String input, Integer min, Integer max){
        int valid = 1;
        int parsed = 0;
        int[] validParsed = new int[]{ 0, 0};
        try {
            parsed = Integer.parseInt(input);

            if( min != null && parsed < min ){
                valid = 0;
            }

            if (max != null && parsed > max ){
                valid = 0;
            }
        } catch(java.lang.Exception e){
            return validParsed;
        }

        validParsed[0] = valid;
        validParsed[1] = parsed;

        return validParsed;
    }

    boolean pathIsValidDirectory(String path){
        File f = new File(path);

        boolean valid = true;
        if(!f.exists()){
            valid = false;
        }

        if(!f.isDirectory()){
            valid = false;
        }

        return valid;
    }

    
    private void readyBtnActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
        tsc.handleReadyBtnPress();
    }                                        

    private void goBtnActionPerformed(java.awt.event.ActionEvent evt) {                                      
        // TODO add your handling code here:
        if( pathIsValidDirectory(saveDirectoryText.getText()) ){
            tsc.handleGoBtnPress();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "please choose a directory to save the images to");
        }
    }                                     

    private void stopBtnActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // TODO add your handling code here:
        tsc.handleStopBtnPress();
    }                                       

    private void changeDirectoryBtnActionPerformed(java.awt.event.ActionEvent evt) {                                                   
        // TODO add your handling code here:
        DirectoryChooser dc = new DirectoryChooser("Directory for temp folder");
        String directoryPath = dc.getDirectory();
        saveDirectoryText.setText(directoryPath);
    }                                                  

    private void manualTrackingCheckboxActionPerformed(java.awt.event.ActionEvent evt) {

        tsc.useManualTracking = manualTrackingCheckbox.isSelected();
    }                                                      

    private void centerOfMassTrackingCheckboxActionPerformed(java.awt.event.ActionEvent evt) {                                                             
        tsc.useCenterOfMassTracking = centerOfMassTrackingCheckbox.isSelected();
    }                                                                                                                                       

    private void cameraExposureSelectorActionPerformed(java.awt.event.ActionEvent evt) {                                    tsc.cameraExposureMsIndex = cameraExposureSelector.getSelectedIndex();                        
    }                                                      

    private void cameraCycleLengthSelectorActionPerformed(java.awt.event.ActionEvent evt) {                                                          
        tsc.cameraCycleLengthMsIndex = cameraCycleLengthSelector.getSelectedIndex(); 
    }                                                         

    private void useClosestTrackingCheckboxActionPerformed(java.awt.event.ActionEvent evt) {                                                           
        tsc.useClosestTracking = useClosestTrackingCheckbox.isSelected();
    }                                                          

    private void useRightCheckboxActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        tsc.useRightSideScreenTracking = useRightCheckbox.isSelected();
    }                                                

    private void useFullfieldCheckboxActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        tsc.useFullFieldImaging = useFullfieldCheckbox.isSelected();
    }                                                    

    private void useBrightFieldCheckboxActionPerformed(java.awt.event.ActionEvent evt) {                                                       
        tsc.useBrightFieldImaging = useBrightFieldCheckbox.isSelected();
    }                                                      

    private void stageAccelerationSelectorActionPerformed(java.awt.event.ActionEvent evt) {                                                          
        tsc.stageAccelerationFactor = stageAccelerationSelector.getSelectedIndex();
    }                                                         

    private void detectionAlgorithmSelectorActionPerformed(java.awt.event.ActionEvent evt) {                                                           
        tsc.detectionAlgorithm = (String) detectionAlgorithmSelector.getSelectedItem();
    }                                                          

    private void saveXYPositionsAsTextCheckboxActionPerformed(java.awt.event.ActionEvent evt) {                                                              
        tsc.saveXYPositionsAsTextFile = saveXYPositionsAsTextCheckbox.isSelected();
    }                                                             

    private void enableStimulatorBtnActionPerformed(java.awt.event.ActionEvent evt) {                                   
        tsc.enableStimulator = enableStimulatorBtn.isSelected();  
    }                                                   

    private void testStimulationBtnActionPerformed(java.awt.event.ActionEvent evt) {                                   

        // tsc needs to ensure that stimulation is not already running
        tsc.runStimulation();
    }                                                                                                   

    private void enableRampStimulationBtnActionPerformed(java.awt.event.ActionEvent evt) {                                                         
        // TODO add your handling code here:
        tsc.useRamp = enableRampStimulationBtn.isSelected();  

    }                                                        

    private void rampStartTextActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // TODO add your handling code here:
        int[] result = validateNumberInput(rampStartText.getText(), Integer.valueOf(0), null);

        if(result[0] != 0){
            tsc.rampStart = result[1];
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "ramp start is invalid");
            rampStartText.setText(String.valueOf(tsc.rampStart));
        }       
    }                                                                   

    private void saveDirectoryTextActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        // TODO add your handling code here:
    }             


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {

        readyBtn = new javax.swing.JButton();
        goBtn = new javax.swing.JButton();
        stopBtn = new javax.swing.JButton();
        saveDirectoryText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        changeDirectoryBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        numFramesText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        skipFrameText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cameraExposureSelector = new javax.swing.JComboBox();
        cameraCycleLengthSelector = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        manualTrackingCheckbox = new javax.swing.JCheckBox();
        useRightCheckbox = new javax.swing.JCheckBox();
        centerOfMassTrackingCheckbox = new javax.swing.JCheckBox();
        useBrightFieldCheckbox = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        stageAccelerationSelector = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        detectionAlgorithmSelector = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        testStimulationBtn = new javax.swing.JButton();
        enableStimulatorBtn = new javax.swing.JToggleButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        preStimulationDurationMsText = new javax.swing.JTextField();
        stimulationDurationMsText = new javax.swing.JTextField();
        stimulationStrengthText = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        stimulationCycleLengthText = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        numStimulationCyclesText = new javax.swing.JTextField();
        enableRampStimulationBtn = new javax.swing.JToggleButton();
        rampBaseText = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        rampStartText = new javax.swing.JTextField();
        rampEndText = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        saveXYPositionsAsTextCheckbox = new javax.swing.JCheckBox();
        useFullfieldCheckbox = new javax.swing.JCheckBox();
        useClosestTrackingCheckbox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        readyBtn.setText("READY");
        readyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readyBtnActionPerformed(evt);
            }
        });

        goBtn.setBackground(new java.awt.Color(153, 153, 153));
        goBtn.setForeground(new java.awt.Color(51, 153, 0));
        goBtn.setText("GO");
        goBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goBtnActionPerformed(evt);
            }
        });

        stopBtn.setForeground(new java.awt.Color(255, 0, 0));
        stopBtn.setText("STOP");
        stopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBtnActionPerformed(evt);
            }
        });

        saveDirectoryText.setText("jTextField1");
        saveDirectoryText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDirectoryTextActionPerformed(evt);
            }
        });

        jLabel1.setText("Save directory");

        changeDirectoryBtn.setText("change directory");
        changeDirectoryBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeDirectoryBtnActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel2.setText("Camera");

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel3.setText("Number of frames");

        numFramesText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        numFramesText.setText("3000");

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel4.setText("Keep 1 of");

        skipFrameText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        skipFrameText.setText("1");

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel5.setText("frame(s)");

        jLabel6.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel6.setText("Exposure (ms)");

        cameraExposureSelector.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        cameraExposureSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "10", "50", "100", "200", "500", "1000" }));
        cameraExposureSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraExposureSelectorActionPerformed(evt);
            }
        });

        cameraCycleLengthSelector.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        cameraCycleLengthSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "50", "100", "200", "500", "1000", "2000" }));
        cameraCycleLengthSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraCycleLengthSelectorActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel7.setText("Cycle length (ms)");

        jLabel8.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel8.setText("Tracker");

        manualTrackingCheckbox.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        manualTrackingCheckbox.setText("Manual");
        manualTrackingCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualTrackingCheckboxActionPerformed(evt);
            }
        });

        useRightCheckbox.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        useRightCheckbox.setText("Use right");
        useRightCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useRightCheckboxActionPerformed(evt);
            }
        });

        centerOfMassTrackingCheckbox.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        centerOfMassTrackingCheckbox.setText("Center of mass");
        centerOfMassTrackingCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                centerOfMassTrackingCheckboxActionPerformed(evt);
            }
        });

        useBrightFieldCheckbox.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        useBrightFieldCheckbox.setText("Bright field");
        useBrightFieldCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useBrightFieldCheckboxActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel9.setText("Stage acceleration ");

        stageAccelerationSelector.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        stageAccelerationSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1x", "2x", "4x", "5x", "6x" }));
        stageAccelerationSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stageAccelerationSelectorActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel10.setText("Detection algorithm");

        detectionAlgorithmSelector.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        detectionAlgorithmSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yen", "Triangle", "Otsu", "Default", "Huang", "Intermodes", "IsoData", "Li", "MaxEntropy", "Mean", "MinError(I)", "Minimum", "Moments", "Percentile", "RenyiEntropy", "Shanbhag" }));
        detectionAlgorithmSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detectionAlgorithmSelectorActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel11.setText("Stimulator");

        testStimulationBtn.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        testStimulationBtn.setText("Test with these settings");
        testStimulationBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testStimulationBtnActionPerformed(evt);
            }
        });

        enableStimulatorBtn.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        enableStimulatorBtn.setText("Enable");
        enableStimulatorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableStimulatorBtnActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel12.setText("Pre-stim (ms)");

        jLabel13.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel13.setText("Strength (< 64)");

        jLabel14.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel14.setText("Duration (ms)");

        preStimulationDurationMsText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        preStimulationDurationMsText.setText("3000");

        stimulationDurationMsText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        stimulationDurationMsText.setText("1000");

        stimulationStrengthText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        stimulationStrengthText.setText("63");

        jLabel15.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel15.setText("Cycle length (ms)");

        stimulationCycleLengthText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        stimulationCycleLengthText.setText("5000");

        jLabel16.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel16.setText("Number of cycles");

        numStimulationCyclesText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        numStimulationCyclesText.setText("10");

        enableRampStimulationBtn.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        enableRampStimulationBtn.setText("Enable ramp");
        enableRampStimulationBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableRampStimulationBtnActionPerformed(evt);
            }
        });

        rampBaseText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        rampBaseText.setText("0");

        jLabel17.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel17.setText("Ramp base");

        jLabel18.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel18.setText("Ramp start");

        jLabel19.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel19.setText("Ramp end");

        rampStartText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        rampStartText.setText("0");
        rampStartText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rampStartTextActionPerformed(evt);
            }
        });

        rampEndText.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        rampEndText.setText("63");

        saveXYPositionsAsTextCheckbox.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        saveXYPositionsAsTextCheckbox.setText("Save XY positions as a text file");
        saveXYPositionsAsTextCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveXYPositionsAsTextCheckboxActionPerformed(evt);
            }
        });

        useFullfieldCheckbox.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        useFullfieldCheckbox.setText("Full field");
        useFullfieldCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useFullfieldCheckboxActionPerformed(evt);
            }
        });

        useClosestTrackingCheckbox.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        useClosestTrackingCheckbox.setText("Closest");
        useClosestTrackingCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useClosestTrackingCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(18, 18, 18)
                                .addComponent(numStimulationCyclesText, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel14)
                                                .addComponent(jLabel13))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel12)
                                                .addGap(8, 8, 8)))
                                        .addGap(26, 26, 26)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(stimulationDurationMsText, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(preStimulationDurationMsText, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(stimulationStrengthText, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(stimulationCycleLengthText, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel17)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(rampBaseText, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(enableRampStimulationBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(jLabel19)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(rampEndText))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(jLabel18)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(rampStartText, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(numFramesText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(skipFrameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cameraExposureSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cameraCycleLengthSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel8))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(useFullfieldCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(useBrightFieldCheckbox))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(useRightCheckbox)
                                    .addComponent(useClosestTrackingCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(centerOfMassTrackingCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(manualTrackingCheckbox)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(detectionAlgorithmSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(stageAccelerationSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addComponent(saveDirectoryText, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(changeDirectoryBtn))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 258, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(saveXYPositionsAsTextCheckbox)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(enableStimulatorBtn)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(testStimulationBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(6, 6, 6))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(readyBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(63, 63, 63)
                        .addComponent(goBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(numFramesText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(cameraExposureSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(skipFrameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(cameraCycleLengthSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(stageAccelerationSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(detectionAlgorithmSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveXYPositionsAsTextCheckbox)
                        .addGap(26, 26, 26))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(centerOfMassTrackingCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(manualTrackingCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(useClosestTrackingCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(useRightCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(useFullfieldCheckbox)
                            .addComponent(useBrightFieldCheckbox))
                        .addGap(35, 35, 35)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(enableStimulatorBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testStimulationBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(preStimulationDurationMsText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(enableRampStimulationBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(stimulationDurationMsText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rampBaseText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(stimulationStrengthText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(rampStartText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(stimulationCycleLengthText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(rampEndText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(numStimulationCyclesText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(changeDirectoryBtn)
                    .addComponent(saveDirectoryText, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(goBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(readyBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>  
                                      
    // Variables declaration - do not modify                     
    private javax.swing.JComboBox cameraCycleLengthSelector;
    private javax.swing.JComboBox cameraExposureSelector;
    private javax.swing.JCheckBox centerOfMassTrackingCheckbox;
    private javax.swing.JButton changeDirectoryBtn;
    private javax.swing.JComboBox detectionAlgorithmSelector;
    private javax.swing.JToggleButton enableRampStimulationBtn;
    private javax.swing.JToggleButton enableStimulatorBtn;
    private javax.swing.JButton goBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox manualTrackingCheckbox;
    private javax.swing.JTextField numFramesText;
    private javax.swing.JTextField numStimulationCyclesText;
    private javax.swing.JTextField preStimulationDurationMsText;
    private javax.swing.JTextField rampBaseText;
    private javax.swing.JTextField rampEndText;
    private javax.swing.JTextField rampStartText;
    private javax.swing.JButton readyBtn;
    private javax.swing.JTextField saveDirectoryText;
    private javax.swing.JCheckBox saveXYPositionsAsTextCheckbox;
    private javax.swing.JTextField skipFrameText;
    private javax.swing.JComboBox stageAccelerationSelector;
    private javax.swing.JTextField stimulationCycleLengthText;
    private javax.swing.JTextField stimulationDurationMsText;
    private javax.swing.JTextField stimulationStrengthText;
    private javax.swing.JButton stopBtn;
    private javax.swing.JButton testStimulationBtn;
    private javax.swing.JCheckBox useBrightFieldCheckbox;
    private javax.swing.JCheckBox useClosestTrackingCheckbox;
    private javax.swing.JCheckBox useFullfieldCheckbox;
    private javax.swing.JCheckBox useRightCheckbox;
    // End of variables declaration                
}
