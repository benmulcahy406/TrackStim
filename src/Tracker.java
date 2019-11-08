import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.awt.Rectangle;
import java.awt.Point;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

import ij.io.TiffEncoder;
import ij.io.FileInfo;


import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import ij.plugin.filter.RankFilters;
import ij.plugin.filter.EDM;



import ij.gui.Roi;
import ij.gui.PolygonRoi;
import ij.gui.Wand;
import ij.gui.ImageCanvas;

import mmcorej.CMMCore;

class XYStageController {
    String xyStagePort;
    String xyStageDeviceLabel;
    String zStageDeviceLabel;

    CMMCore core;

    XYStageController(CMMCore core_){
        core = core_;
    }

    boolean initialize(){
        boolean initialized = false;

        try {
            xyStageDeviceLabel = core.getXYStageDevice();
            zStageDeviceLabel = core.getFocusDevice();
            xyStagePort = core.getProperty(xyStageDeviceLabel, "Port");
            initialized = true;
        } catch(java.lang.Exception e) {
            IJ.log("could not get xy stage port");
            IJ.log(e.getMessage());
        }

        IJ.log("xyStagePort is " + xyStagePort);
        IJ.log("xyStageDeviceLabel is " + xyStageDeviceLabel);
        IJ.log("zStageLabel is " + zStageDeviceLabel);
        
        return initialized;
    }

    // return current xyz position in an array
    // position[0] is x coordinate
    // position[1] is y coordinate
    // posiiton[2] is z coordinate
    double[] getXYZPosition(){
        double [] position = { 0.0, 0.0, 0.0 };
        try {
            position[0] = core.getXPosition(xyStageDeviceLabel);
            position[1] = core.getYPosition(xyStageDeviceLabel);
            position[2] = core.getPosition(zStageDeviceLabel);
        } catch (java.lang.Exception e) {
            IJ.log("startAcq: error getting z position from zstage " + zStageDeviceLabel);
            IJ.log(e.getMessage());
        }
        return position;
    }

    // set XY stage velocity by sending a command using the serial port
    void setXYStageVelocity(double xVelocity, double yVelocity){
        String xyStageCommand = "VECTOR X=" + String.valueOf(xVelocity) + " Y=" + String.valueOf(yVelocity);
        try {
            core.setSerialPortCommand(xyStagePort, xyStageCommand, "\r");

            String xyStageCommandAnswer = core.getSerialPortAnswer(xyStagePort, "\r\n");
            IJ.log("setXYStageVelocity: response from xy stage is " + xyStageCommandAnswer);

        } catch (java.lang.Exception e) {
            IJ.log("setXYStageVelocity: error setting xy stage velocity " + xyStageCommand);
            IJ.log(e.getMessage());
        }
    }

    // get XY stage velocity by sending a command using the serial port
    String getXYStageVelocity(double xVelocity, double yVelocity){
        String xyStageCommand = "VECTOR X=? Y=?";
        String xyStageVelocityAnswer = "";
        try {
            core.setSerialPortCommand(xyStagePort, xyStageCommand, "\r");

            xyStageVelocityAnswer = core.getSerialPortAnswer(xyStagePort, "\r\n");
            IJ.log("getXYStageVelocity: response from xy stage is " + xyStageVelocityAnswer);
        } catch (java.lang.Exception e) {
            IJ.log("getXYStageVelocity: error getting xy stage velocity " + xyStageCommand);
            IJ.log(e.getMessage());
        }

        return xyStageVelocityAnswer;
    }
}


class Tracker extends Thread {
    // vaiables recieve from GUI
    TrackStimGUI tpf;
    CMMCore mmc_;
    ImagePlus imp;
    ImageCanvas ic;
    String imageSaveDirectory;
    int numFrames;
    boolean ready;

    // XY Stage Controller
    XYStageController xyStageController;

    // inside of thread
    static int countslice = 0;
    static ImageStack binaryimgstack = null;
    static ArrayList<Roi> preroiarraylist = null;
    static double predistance = 0.0;
    static double pretheta = 10.0;// normal radian must beteween =-pi. so this value could use to check if there
                                  // is pretheta.
    static int[] threshbuffer = new int[30];
    static int threshsum = 0;
    static int threahaverage = 0;

    static ImageProcessor ip_;
    static ImageProcessor ip_resized;
    static ImageProcessor ip_resizedori;
    static ImagePlus impresizedori;
    static ImageProcessor iplbyte;

    double[][] measurespre;
    double[][] measures;
    double[][] targethistory;

    /*---------------------------------------  constant variables-------------------------------------------------*/
    // if the shift is larger than limit, move stage.
    double LIMIT = 5;
    double COEF = 1.0;
    // allowance angle change
    static double minanglecos = Math.cos(60.0 / 180 * Math.PI);// 45/180=0 because int...... so need to change order or
                                                               // add .0
    // allowance distance change
    static double mindistancechange = 0.3;

    Tracker(TrackStimGUI gui) {
        IJ.log("Tracker constructor");
        this.tpf = gui;
        mmc_ = tpf.mmc;
        imp = tpf.currentImage;
        ic = tpf.currentImageCanvas;
        imageSaveDirectory = tpf.imageSaveDirectory;
        numFrames = tpf.numFrames;
        ready = tpf.ready;

        xyStageController = new XYStageController(mmc_);

    }

    public void run() {
        IJ.log("Tracker: run start");

        // ensure mmc is not running a sequence acquisition already
        if (mmc_.isSequenceRunning()) {
            try {
                IJ.log("startAcq: previous acquisition running, trying stopSequenceAcquisition");
                mmc_.stopSequenceAcquisition(); // need to be catched
            } catch (java.lang.Exception e) {
                IJ.log("startAcq: error trying to stop sequence acquisition");
                IJ.log(e.getMessage());
            }
        }

        this.startAcq("from thread");
    }

    public void changeTarget() {
        // this is called when a click event is triggered
        Point cursorpoint = ic.getCursorLoc();
        IJ.log("changeTarget: x is " + String.valueOf(cursorpoint.x) + ", y is " + String.valueOf(cursorpoint.y));

        // center of mass method
        if (tpf.useClosest.getState()){
            targethistory[countslice - 1][1] = cursorpoint.x;
            targethistory[countslice - 1][2] = cursorpoint.y;
            // also change current slice's data...
            targethistory[countslice][1] = cursorpoint.x;
            targethistory[countslice][2] = cursorpoint.y;
        } else {
            // normal thresholding method
            // compare with measurespre[roinumber][area,mean,x,y]
            double distancescalar = 0;
            double minval = 0;
            int minindex = 0;
            double dx = 0;
            double dy = 0;
            double mindx = 0;
            double mindy = 0;
            for (int i = 0; i < measurespre.length; i++) {
                dx = cursorpoint.x - measurespre[i][2];
                dy = cursorpoint.y - measurespre[i][3];
                distancescalar = Math.sqrt(dx * dx + dy * dy);
                if (i != 0) {
                    if (minval > distancescalar) {
                        minval = distancescalar;
                        minindex = i;
                        mindx = dx;
                        mindy = dy;
                    }
                } else {
                    minval = distancescalar;
                    minindex = 0;
                }
            }

            double correctedx = measurespre[minindex][2];
            double correctedy = measurespre[minindex][3];
            // change the targethistory[slicenum][roiindex,x,y]

            targethistory[countslice - 1][0] = minindex;
            targethistory[countslice - 1][1] = correctedx;
            targethistory[countslice - 1][2] = correctedy;
            // also change current slice's data...
            targethistory[countslice][0] = minindex;
            targethistory[countslice][1] = correctedx;
            targethistory[countslice][2] = correctedy;
            IJ.log("changeTarget: changed the target to roi " + String.valueOf(minindex) + "; " + String.valueOf(correctedx) + " "
                    + String.valueOf(correctedy));
        }
    }


    // second new method to track
    // make bynary image, watershed, detect objects above particular size
    // measure objects positions (and area mean values)
    // conpare previous image and determine which object is a target using closest
    // position
    // calculate distance the target from centor of image
    // returnval=[roinumber][area,mean,xCentroid,yCentroid]
    // static is bit faster
    static double[][] getObjmeasures(ImagePlus imp, ImageProcessor ip, boolean savebinary, String method) {
        String thresholdmethodstring = method;
        // the imp ip must left half or something not full image
        ip_ = ip.duplicate();
        ip_resized = ip_.resize(ip_.getWidth() / 2);
        RankFilters rf = new RankFilters();
        rf.rank(ip_resized, 0.0, 4);// median 4 periodic black white noize cause miss thresholding, so eliminate
                                    // those noize

        ip_resizedori = ip_resized.duplicate();
        impresizedori = new ImagePlus("l", ip_resizedori);

        // initiall 31 slice calculate Autothresho at every time
        // also static value doesnt clear after the imaging by "ready" ,use it to reduce
        // calc.?
        // changed to clear every time. since hard to clear manually
        if (countslice < 30) {
            ip_resized.setAutoThreshold(thresholdmethodstring, true, 0);// seems good. and fast? 13msec/per->less than
                                                                        // 10msec. better than otsu at head
            // need check after median filter if this is better.
            // good for head
            threshbuffer[countslice] = (int) ip_resized.getMinThreshold();
            threahaverage = (int) ip_resized.getMinThreshold();
            if (countslice == 29) {
                IJ.log("getObjMeasures: threshsum is " + String.valueOf(threshsum));
                // sum and average threshold
                for (int i = 0; i < 30; i++) {
                    threshsum = threshsum + threshbuffer[i];
                }
                threahaverage = threshsum / 30;
            }
        } else {
            // every 50 slice calculate setAutoThreshold
            if (countslice % 50 == 0) {
                ip_resized.setAutoThreshold(thresholdmethodstring, true, 0);
                int slotindex = ((countslice - 30) / 50) % 30;
                int newdata = (int) ip_resized.getMinThreshold();
                int olddata = threshbuffer[slotindex];
                threshbuffer[slotindex] = newdata;
                threshsum = threshsum - olddata + newdata;
                threahaverage = threshsum / 30;
            }
        }
        ip_resized.threshold(threahaverage);
        ImageStatistics imstat;
        iplbyte = ip_resized.convertToByte(false);
        EDM edm = new EDM();
        edm.toWatershed(iplbyte);
        iplbyte.invert();

        ImagePlus impleftbyte = new ImagePlus("lbyte", iplbyte);
        countslice++;
        if (savebinary) {
            binaryimgstack.setPixels(iplbyte.getPixelsCopy(), countslice);
        }
        int widthleft = impleftbyte.getWidth();
        int heightleft = impleftbyte.getHeight();

        Wand wand = new Wand(iplbyte);
        int minimamarea = 6;
        int x;
        int y;
        iplbyte.setValue(255);
        ArrayList<Roi> roiarraylist = new ArrayList<Roi>();
        preroiarraylist = roiarraylist;
        Roi roi;

        // this whole image scan loop is too heaby. take 200msec per resized half image
        // need faster method....
        // So, don't scan every row/column. every 3 line may enough?
        // now 0.5/10 slice. 50 msec, 2sec/40
        // non resized image with each 6. 2.8sec/40, 70msec.
        // this might because get()? using pixel arry is better?
        // ...All these methods should only be used if you intend to modify just a few
        // pixels. If you
        // want to modify large parts of the image it is faster to work with the pixel
        // array....yoru
        // Don't need get every time. just have array and pick up
        // every 2line tri in ver2
        byte[] pixels = (byte[]) iplbyte.getPixels();
        for (y = 0; y < heightleft; y = y + 3) {
            for (x = 0; x < widthleft; x = x + 3) {

                if (pixels[y * widthleft + x] == 0){

                    wand.autoOutline(x, y, 0.0, 1.0, 4);
                    roi = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, 2);
                    impleftbyte.setRoi(roi);
                    imstat = impleftbyte.getStatistics(1);// area 1 mean 2
                    if (imstat.area > minimamarea) {
                        roiarraylist.add(roi);
                    }
                    // delet already detected roi.
                    iplbyte.fill(roi);
                }
            }
        }

        // 3 measurement factors, area, mean, centroid
        double[][] roimeasures = new double[roiarraylist.size()][4];
        Roi roi_;
        for (int i = 0; i < roiarraylist.size(); i++) {
            roi_ = (Roi) roiarraylist.get(i);
            impresizedori.setRoi(roi_);
            imstat = impresizedori.getStatistics(1 + 2 + 32 + 4 + 64);// area 1 mean 2, sd 4, centerofmass 64
            roimeasures[i][0] = imstat.area;
            roimeasures[i][1] = imstat.mean;
            roimeasures[i][2] = imstat.xCentroid;
            roimeasures[i][3] = imstat.yCentroid;
        }
        return roimeasures;
    }

    // direction from arg1 to arg2
    // output is [meauresroinum][minindex,minimaldistancel,dx,dy]
    // measures=[roinum][area,mean,centroidx,centroidy]
    static double[][] getMinDist(double[][] measurespre_, double[][] measures_) {
        int i;
        int j;
        double distancescalar = 0;
        double minval = 0;
        int minindex = 0;
        double dx = 0;
        double dy = 0;
        double mindx = 0;
        double mindy = 0;
        IJ.log("getMinDist: measurespre_ length is " + String.valueOf(measurespre_.length));
        double[][] returnval = new double[measurespre_.length][4];
        for (i = 0; i < measurespre_.length; i++) {
            for (j = 0; j < measures_.length; j++) {
                dx = measures_[j][2] - measurespre_[i][2];
                dy = measures_[j][3] - measurespre_[i][3];
                distancescalar = Math.sqrt(dx * dx + dy * dy);
                if (j != 0) {
                    if (minval > distancescalar) {
                        minval = distancescalar;
                        minindex = j;
                        mindx = dx;
                        mindy = dy;
                    }
                } else {
                    minval = distancescalar;
                    minindex = 0;
                }
            }
            returnval[i][0] = minindex;
            returnval[i][1] = minval;
            returnval[i][2] = mindx;
            returnval[i][3] = mindy;
        }
        return returnval;
    }

    // arg is measures, or roiorder is also ok
    // return [roi#][order by distance from target, distance, dx, dy]
    // roi# is orederd by getObjmeasures, which means top left most is fisrt roi
    static double[][] getRoiOrder(int targetroinum, double[][] measures_) {
        double[] targetcoordinate = new double[2];
        double dx = 0;
        double dy = 0;
        double[][] returnval = new double[measures_.length][4];
        targetcoordinate[0] = measures_[targetroinum][2];// x
        targetcoordinate[1] = measures_[targetroinum][3];// y
        IJ.log("getRoiOrder: target num is" + String.valueOf(targetroinum));
        IJ.log("getRoiOrder: target coordinate is (x: " + String.valueOf(targetcoordinate[0]) + ", y: " + String.valueOf(targetcoordinate[1]) + ")");

        double[] distancescaler = new double[measures_.length];
        for (int i = 0; i < measures_.length; i++) {
            dx = targetcoordinate[0] - measures_[i][2];
            dy = targetcoordinate[1] - measures_[i][3];
            returnval[i][2] = dx;
            returnval[i][3] = dy;
            distancescaler[i] = Math.sqrt(dx * dx + dy * dy);
            returnval[i][1] = distancescaler[i];
            IJ.log("getRoiOrder: distance is " + String.valueOf(distancescaler[i]) + " roi " + String.valueOf(i));
            IJ.log("getRoiOrder: current roi index is " + String.valueOf(i));
        }
        double[] copydistance = distancescaler.clone();
        Arrays.sort(copydistance);// is there any method to get sorted index?
        for (int i = 0; i < distancescaler.length; i++) {
            for (int j = 0; j < copydistance.length; j++) {
                if (distancescaler[i] == copydistance[j]) {
                    returnval[i][0] = (double) j;
                }
            }
        }

        return returnval;
    }

    // roiorder [roi#][order by distance from target, distance, dx, dy]
    // returen same format of roiorder
    // slice is first image is 1 not 0
    static double[][] checkDirDis(int slice, double[][] roiorder, double[][] measures_) {
        double[][] returnvalue;// new roi order, if could found, or same value as input, if cannot found better
                               // pattern.
        boolean trackstatus = false;
        int adjacentroi = 0;
        for (int j = 0; j < roiorder.length; j++) {
            // == 1 means the closest roi. 0 is target.
            if ((int) (roiorder[j][0]) == 1){
                adjacentroi = j;
            }
        }
        double theta = Math.atan2(roiorder[adjacentroi][2], roiorder[adjacentroi][3]);// 2 x, 3y, ???? this should be
                                                                                      // mistake? atan2(Y,X) not (X,Y)
        IJ.log("checkDirDis: theta is" + String.valueOf(theta / Math.PI * 180));

        // pretheta<10 means there is slice having more than 2 rois and processed before
        // than this slice
        if (pretheta < 10){
            // minanglecos
            double deltaanglecos = Math.cos(pretheta - theta);
            double deltadistanceratio = Math.abs(predistance - roiorder[adjacentroi][1]) / predistance;
            IJ.log("checkDirDis: pretheta degree " + String.valueOf(pretheta / Math.PI * 180));
            IJ.log("checkDirDis: theta degree " + String.valueOf(theta / Math.PI * 180));
            IJ.log("checkDirDis: deltaanglecos " + String.valueOf(deltaanglecos));
            IJ.log("checkDirDis: deltadistanceratio "+ String.valueOf(deltadistanceratio));
            IJ.log("checkDirDis: minanglecos " + String.valueOf(minanglecos));
            IJ.log("checkDirDis: predistance " + String.valueOf(predistance));

            if (deltaanglecos < minanglecos || deltadistanceratio > mindistancechange)// 1st time
            {
                if (deltaanglecos < minanglecos) {
                    IJ.log("checkDirDis: wrong direction because deltaanglecos < minanglecos");
                } else {
                    IJ.log("checkDirDis: distance change is large because deltaanglecos >= minanglecos");
                }

                // try getroiorder again using next roi as target
                // return [roi#][order by distance from target, distance, dx, dy]
                // static double[][] getRoiOrder(int targetroinum, double[][] measures_)
                double[][] roiorder2 = getRoiOrder(adjacentroi, measures_);
                int adjacentroi2 = 0;
                for (int j = 0; j < roiorder2.length; j++) {
                    if ((int) (roiorder2[j][0]) == 1) {
                        adjacentroi2 = j;
                    }
                }
                theta = Math.atan2(roiorder2[adjacentroi2][2], roiorder2[adjacentroi2][3]);
                deltaanglecos = Math.cos(pretheta - theta);
                deltadistanceratio = Math.abs(predistance - roiorder2[adjacentroi2][1]) / predistance;
                IJ.log("checkDirDis: pretheta degree " + String.valueOf(pretheta / Math.PI * 180));
                IJ.log("checkDirDis: theta degree " + String.valueOf(theta / Math.PI * 180));
                IJ.log("checkDirDis: predistance " + String.valueOf(predistance));
                IJ.log("checkDirDis: 2nd trial angle cos " + String.valueOf(deltaanglecos));
                IJ.log("checkDirDis: deltadistanceratio " + String.valueOf(deltadistanceratio));

                if (deltaanglecos < minanglecos || deltadistanceratio > mindistancechange){
                    // try onemore time again. this time initail target and 2nd next roi as
                    // direction
                    int adjacentroi3 = 0;
                    for (int j = 0; j < roiorder.length; j++) {
                        // here is 2nd next closser roi.
                        if ((int) (roiorder[j][0]) == 2){
                            adjacentroi3 = j;
                        }
                    }
                    theta = Math.atan2(roiorder[adjacentroi3][2], roiorder[adjacentroi3][3]);
                    deltaanglecos = Math.cos(pretheta - theta);
                    deltadistanceratio = Math.abs(predistance - roiorder[adjacentroi3][1]) / predistance;
                    IJ.log("checkDirDis: theta degree " + String.valueOf(theta / Math.PI * 180));
                    IJ.log("checkDirDis: 3rd trial angle cos " + String.valueOf(deltaanglecos));
                    IJ.log("checkDirDis: deltadistanceratio " + String.valueOf(deltadistanceratio));

                    // 3rd if still strange..
                    // give up this slice
                    if (deltaanglecos < minanglecos || deltadistanceratio > mindistancechange){
                        IJ.log("checkDirDis: third slice check has failed, giving up");
                        trackstatus = false;
                        roiorder = new double[][] { { -1 } };// return negative value because failed
                    } else {
                        IJ.log("checkDirDis: third slice check has passed");
                        predistance = roiorder[adjacentroi][1];
                        pretheta = theta;
                        trackstatus = true;
                    }
                } else {
                    IJ.log("checkDirDis: second slice check has passed");
                    predistance = roiorder2[adjacentroi2][1];
                    pretheta = theta;
                    roiorder = roiorder2;
                    trackstatus = true;
                }
            } else {
                predistance = roiorder[adjacentroi][1];
                pretheta = theta;
                trackstatus = true;
            }
        } else if (pretheta == 10.0) {
            // if this is the first slice having 2 or more rois.{
            predistance = roiorder[adjacentroi][1];
            pretheta = theta;
        }
        returnvalue = roiorder;
        return returnvalue;
    }

    // not entirely sure what this does, but I suspect that this is related to drawing
    // an overlay over the images
    // in particular, I think it draws boxes representing regions of intrest in a specific order
    void drawRoiOrder(int slice, double[][] roiOrder, double[][] measures_, boolean trackStatus) {
        ImageProcessor drawip = binaryimgstack.getProcessor(slice);

        for (int j = 0; j < roiOrder.length; j++) {
            String order = String.valueOf((int) roiOrder[j][0]);
            IJ.log(order + " slice " + String.valueOf(slice));

            drawip.moveTo((int) measures_[j][2], (int) measures_[j][3]);
            
            if (trackStatus == true) {
                drawip.setValue(200);
            } else {
                drawip.setValue(100);
            }
            
            drawip.drawString(order);
        }
    }

    // Save x and y position to .txt file.
    void saveXYPositionsToTextFile(double[] xPositionsToSave, double[] yPositionsToSave) {
        String saveString;
        String header = "x,y";
        String lineSeperator = System.getProperty("line.separator");
        saveString = header + lineSeperator;
        // actual data

        String currPositionData = "";
        for (int i = 0; i < xPositionsToSave.length; i++) {
            currPositionData = String.valueOf(xPositionsToSave[i]) + "," + String.valueOf(yPositionsToSave[i]);
            saveString = saveString + currPositionData + lineSeperator;
        }
        // show dialog
        IJ.saveString(saveString, tpf.imageSaveDirectory);
        IJ.log("saveXYPositionsToTextFile: Output is saved");
    }


    // for non-thresholding method
    // this returns x y of centor of mass backgournd subtracted
    static double[] getCenterofMass(ImagePlus imp, ImageProcessor ip, Roi roi, int x, int y) {
        ImagePlus imp_ = imp;
        ImageProcessor ip_ = ip;
        Roi roi_ = roi;

        // get the mean pixel value for the background
        ImageStatistics imstat_ = imp_.getStatistics(ij.measure.Measurements.MEAN); // 2 -> ij.measure.Measurements.MEAN
        int bgValue = (int) imstat_.mean;

        // duplicate imageprocessor and subtract the mean pixel value for each pixel
        ImageProcessor ip2 = ip_.duplicate();
        ip2.add(-bgValue * 1.5);
        ImagePlus imp2 = new ImagePlus("subtracted", ip2);

        roi_.setLocation(x, y);
        imp2.setRoi(roi_);

        // eliminate noise that interferes with thresholding
        RankFilters rf = new RankFilters();
        rf.rank(ip2, 0.0, ij.plugin.filter.RankFilters.MEDIAN);


        // get center of mass
        ImageStatistics imstat2 = imp2.getStatistics(ij.measure.Measurements.CENTER_OF_MASS + ij.measure.Measurements.CENTROID);
        double[] centerOfMass = { imstat2.xCenterOfMass, imstat2.yCenterOfMass };

        // increment global variable
        countslice++;

        return centerOfMass;
    }

    void saveImageToTiff(String imageName, ImagePlus imgToSave, double xPos, double yPos, double zPos){
        String fileName = imageSaveDirectory + "/" + imageName + ".tif";
        String positionInfo = "xpos=" + String.valueOf(xPos) 
                + ",ypos=" + String.valueOf(yPos)
                + ",zpos=" + String.valueOf(zPos);

        try {
            File outfile = new File(fileName);
            outfile.createNewFile();

            OutputStream outstream = new FileOutputStream(outfile);
            FileInfo fi = imgToSave.getFileInfo();

            fi.info = positionInfo;

            TiffEncoder tiffencoder = new TiffEncoder(fi);
            tiffencoder.write(outstream);
            outstream.close();

        } catch (java.lang.Exception e){
            IJ.log("startAcq: could not save image to file");
            IJ.log(e.getMessage());
        }
    }

    // called in startAcq when there is an existing image stack to process
    void processExistingImageStack(ImagePlus imgPls){
        int width = imgPls.getWidth();
        int height = imgPls.getHeight();
        ImageProcessor ip = imgPls.getProcessor();
        Roi roi = new Roi(0, 0, width / 2, height);

        Date d1 = new java.util.Date();
        IJ.log("startAcq: processing image stack at start time" + d1.getTime());
        ImageStack imgstack = imgPls.getStack();
        int slicenumber = imgPls.getNSlices();
        binaryimgstack = new ImageStack(width / 4, height / 2, slicenumber);// out put to check how look like
        // if width is not multiple of 4, cause error
        measurespre = new double[][] { { 0 }, { 0 } };
        // measures;
        double[][] mindist;
        targethistory = new double[slicenumber][3];
        double[] shift = new double[2];
        double roiorder[][] = null;
            for (int i = 0; i < slicenumber; i++) {
                imgPls.setSlice(i + 1);
                imgPls.setRoi(roi);
                ImageProcessor ipleft = ip.crop();
                ImagePlus impleft = new ImagePlus("l", ipleft);
                String thresholdmethod = tpf.thresholdMethodSelector.getSelectedItem();
                measures = getObjmeasures(impleft, ipleft, true, thresholdmethod);

                // if lost any cells
                if (measures.length == 0){
                    IJ.log("startAcq: target lost while processing image stack");
                    // test to continue imaging
                    measures = measurespre;
                }
                if (i != 0) {
                    mindist = getMinDist(measurespre, measures);
                    int j;
                    int previoustarget = (int) targethistory[i - 1][0];
                    int newtarget = (int) mindist[previoustarget][0];
                    targethistory[i][0] = newtarget;
                    targethistory[i][1] = measures[newtarget][2];
                    targethistory[i][2] = measures[newtarget][3];
                    shift[0] = mindist[previoustarget][2];
                    shift[1] = mindist[previoustarget][3];
                    IJ.log(shift[0] + "," + shift[1]);
                    // here put stage control code.
                } else {
                    // mock meaures to detect most centorized roi for resized scan, divide 4
                    double[][] mock = { { 0, 0, ipleft.getWidth() / 4, ipleft.getHeight() / 4 } };
                    double[][] initialtarget = getMinDist(mock, measures);
                    int target = (int) initialtarget[0][0];
                    IJ.log("startAcq: target #" + String.valueOf(target) + " roi at " + String.valueOf(measures[target][2]) + ","
                            + String.valueOf(measures[target][3]));
                    targethistory[0][0] = target;
                    targethistory[0][1] = measures[target][2];
                    targethistory[0][2] = measures[target][3];
                }

                // return [roi#][order by distance from target, distance, dx, dy]
                // static double[][] getRoiOrder(int targetroinum, double[][] measures)
                IJ.log("startAqc: calling getRoiOrder with arg " + String.valueOf(targethistory[i][0]));
                roiorder = getRoiOrder((int) targethistory[i][0], measures);
                // check target is collect or not by direcion/distance towards next roi. if
                // there are more than 2 rois.
                if (measures.length >= 2) {
                    // roiorder [roi#][order by distance from target, distance, dx, dy]
                    // returen same format.
                    // static double[][] checkDirDis(int slice, double[][] roiorder,double[][]
                    // measures)
                    double[][] checkedroiorder = checkDirDis(i - 1, roiorder, measures);
                    boolean trackstatus = false;
                    double[][] finalroiorder = new double[][] { { 0 } };
                    if ((int) checkedroiorder[0][0] == -1)// negative means failed
                    {
                        trackstatus = false;
                        finalroiorder = roiorder;
                    } else {
                        trackstatus = true;
                        finalroiorder = checkedroiorder;
                        int newtarget = 0;
                        for (int j = 0; j < finalroiorder.length; j++) {
                            if ((int) (finalroiorder[j][0]) == 0)// here is target
                            {
                                newtarget = j;
                            }
                        }
                        // targethistory[slicenumber][roi index, x, y]
                        targethistory[i][0] = newtarget;
                        targethistory[i][1] = measures[newtarget][2];
                        targethistory[i][2] = measures[newtarget][3];

                    }
                    IJ.log("startAcq: targethistory after calling getRoiOrder " + String.valueOf(targethistory[i][0]));
                    // void drawRoiOrder(int slice, double[][] roiorder, double[][] measures,
                    // boolean trackstatus)
                    drawRoiOrder(i - 1, finalroiorder, measures, trackstatus);
                } // if(measures.length>2) end
                  // use targethistory[i][0] to calculate distance from centor.
                measurespre = measures;
            } // for(int i=1;i<=slicenumber;i++) end
            Date d2 = new java.util.Date();
            IJ.log("startAcq: finished processing image stack at" + d2.getTime());
            IJ.log(String.valueOf((d2.getTime() - d1.getTime()) / 1000.0) + " sec");

            ImagePlus imp3 = new ImagePlus("binarystack", binaryimgstack);
            imp3.show();

    }

    /*---------------------------------------  start process-------------------------------------------------*/

    //////////////////////////////////////////////////////////////////////////////////
    public void startAcq(String arg) {
        boolean xyStageInitialized = xyStageController.initialize();

        if( !xyStageInitialized ){
            IJ.log("startAcq: XY stage port is not found, can't start image acquisition");
            return;
        }

        // static values are last even after process. need to clear onece have done.
        countslice = 0;
        binaryimgstack = null;
        preroiarraylist = null;
        predistance = 0.0;
        pretheta = 10.0;// normal radian must beteween =-pi. so this value could use to check if there
                        // is pretheta.
        threshsum = 0;
        threahaverage = 0;

        ImageProcessor ip;
        // get info. from the live window
        ip = imp.getProcessor();
        Roi roi = imp.getRoi();
        int width = imp.getWidth();
        int height = imp.getHeight();
        int roiwidth = width / 2;
        int roiheight = height;
        Roi leftroi;
        leftroi = new Roi(0, 0, width / 2, height);
        Roi rightroi;
        rightroi = new Roi(width / 2, 0, width / 2, height);

        if (roi != null && !tpf.trackRightSideScreen.getState()) {
            Rectangle r = roi.getBounds();
            roiwidth = r.width;
            roiheight = r.height;
        } else {
            if (!tpf.trackRightSideScreen.getState()) {
                // set roi at left half.
                IJ.log("startAcq: no existing roi, set roi to the left side of the screen");
                roi = (Roi) leftroi.clone();
            } else {
                // set roi at right half.
                IJ.log("startAcq: no existing roi, set roi to the right side of the screen");
                roi = (Roi) rightroi.clone();

            }
        }

        // If there is stack, process it without stage control.
        if (imp.getImageStackSize() > 1) {
            processExistingImageStack(imp);
            return;
        } 

        // start acquisition

        double[] xposarray = new double[numFrames];
        double[] yposarray = new double[numFrames];
        double zpos = 0.0;

        // 50msec wait doesn't work?
        try {
            mmc_.startSequenceAcquisition(numFrames, 0, false);
        } catch (java.lang.Exception e) {
            IJ.log("startAcq: error calling MMCore startSequenceAcquisition");
            IJ.log(e.getMessage());
        }

        ImageStatistics imstat = imp.getStatistics(16);
        imp.setDisplayRange(imstat.min, imstat.max);
        imp.show();

        Date d1 = new java.util.Date();
        IJ.log("startAcq: starting image acquisition at time " + d1.getTime());
        measurespre = new double[][] { { 0 }, { 0 } };
        // measures;
        double[][] mindist;
        // targethistory[slicenumber][roi index, x, y]
        targethistory = new double[numFrames][3];
        double[] shift = new double[2];
        double[] stagepos = new double[2];
        double roiorder[][] = null;
        double[] distancefromcenter = new double[2];
        java.lang.Object img = new short[width * height];

        int curImgIndex = 0;
        int skip = Integer.parseInt(tpf.numSkipFramesText.getText());// if it 2, keep 1 out of 2 image
        int skipcount = 0;
        double[] centorofmass = new double[2];
        // while imaging...
        long nanotimecurrent = System.nanoTime();
        long nanotimepre = 0;

        while (mmc_.isSequenceRunning()) {
            if (mmc_.getRemainingImageCount() > 0) {
                try {
                    img = mmc_.popNextImage();// img is byte array,[B, or Short array [S
                } catch (java.lang.Exception e) {
                    IJ.log("startAcq: error calling MMCore popNextImage");
                    IJ.log(e.getMessage());
                }
                skipcount = (skipcount + 1) % skip;
                nanotimecurrent = System.nanoTime();
                // throw the data
                if (skipcount == 0)// else throw the data
                {
                    nanotimepre = nanotimecurrent;
                    ip.setPixels(img);
                    imp.setProcessor(imp.getTitle(), ip);
                    imp.updateImage();
        
                    double[] currPosition = xyStageController.getXYZPosition();
                    xposarray[curImgIndex] = currPosition[0];
                    yposarray[curImgIndex] = currPosition[1];
                    zpos = currPosition[2];

                    // if this is runnning as real imaging process, save images.
                    if (!ready) {
                        saveImageToTiff(String.valueOf(curImgIndex), imp, xposarray[curImgIndex], yposarray[curImgIndex], zpos);
                    }

                    if (!tpf.useManualTracking.getState()) {
                        if (!tpf.useCenterOfMassTracking.getState() && !tpf.useFullFieldImaging.getState())// for normal thresholding method.
                        {

                            imp.setRoi(roi);
                            ImageProcessor ip_current = imp.getProcessor();
                            ImageProcessor ipleft = ip_current.crop();
                            ImagePlus impleft = new ImagePlus("l", ipleft);
                            String thresholdmethod = tpf.thresholdMethodSelector.getSelectedItem();
                            measures = getObjmeasures(impleft, ipleft, false, thresholdmethod);
                            if (measures.length == 0)// if lost any cells
                            {
                                IJ.log("startAcq: target lost when not manual tracking");
                                // test to continue imaging
                                measures = measurespre;
                            }
                            if (curImgIndex != 0)// after second image
                            {
                                mindist = getMinDist(measurespre, measures);
                                int j;
                                int previoustarget = (int) targethistory[curImgIndex - 1][0];
                                int newtarget = (int) mindist[previoustarget][0];
                                targethistory[curImgIndex][0] = newtarget;
                                targethistory[curImgIndex][1] = measures[newtarget][2];
                                targethistory[curImgIndex][2] = measures[newtarget][3];
                                shift[0] = mindist[previoustarget][2];
                                shift[1] = mindist[previoustarget][3];
                                IJ.log(shift[0] + "," + shift[1]);
                                // multiply 2 because resized 1/2
                                distancefromcenter[0] = width / 4 - measures[newtarget][2] * 2;
                                distancefromcenter[1] = height / 2 - measures[newtarget][3] * 2;
                                // for non resised version:
                                // distancefromcenter[0]=width/4-measures[newtarget][2];
                                // distancefromcenter[1]=height/2-measures[newtarget][3];

                            } // after second image end
                            else// first image
                            {
                                // mock meaures to detect most centorized roi for resized scan, divide 4
                                double[][] mock = { { 0, 0, ipleft.getWidth() / 4, ipleft.getHeight() / 4 } };
                                double[][] initialtarget = getMinDist(mock, measures);
                                // for non resised version
                                int target = (int) initialtarget[0][0];
                                IJ.log("startAcq: not manual tracking - target #" + String.valueOf(target) + " roi at "
                                        + String.valueOf(measures[target][2]) + ","
                                        + String.valueOf(measures[target][3]));
                                targethistory[0][0] = target;
                                targethistory[0][1] = measures[target][2];
                                targethistory[0][2] = measures[target][3];
                                // multiply 2 because resized 1/2
                                distancefromcenter[0] = width / 4 - measures[target][2] * 2;
                                distancefromcenter[1] = height / 2 - measures[target][3] * 2;
                            } // first image end

                            // return [roi#][order by distance from target, distance, dx, dy]
                            // static double[][] getRoiOrder(int targetroinum, double[][] measures)
                            if (!tpf.useClosest.getState()) {
                                IJ.log("startAcq: before getRoiOrder " + String.valueOf(targethistory[curImgIndex][0]));
                                roiorder = getRoiOrder((int) targethistory[curImgIndex][0], measures);
                                // check target is collect or not by direcion/distance towards next roi. if
                                // there are more than 2 rois.
                                if (measures.length >= 2) {
                                    // roiorder [roi#][order by distance from target, distance, dx, dy]
                                    // returen same format.
                                    // static double[][] checkDirDis(int slice, double[][] roiorder,double[][]
                                    // measures)
                                    double[][] checkedroiorder = checkDirDis(curImgIndex + 1, roiorder, measures);
                                    boolean trackstatus = false;
                                    double[][] finalroiorder = new double[][] { { 0 } };
                                    if ((int) checkedroiorder[0][0] == -1)// negative means failed
                                    {
                                        trackstatus = false;
                                        finalroiorder = roiorder;
                                    } else {
                                        trackstatus = true;
                                        finalroiorder = checkedroiorder;
                                        int newtarget = 0;
                                        for (int j = 0; j < finalroiorder.length; j++) {
                                            if ((int) (finalroiorder[j][0]) == 0)// here is target
                                            {
                                                newtarget = j;
                                            }
                                        }
                                        // targethistory[slicenumber][roi index, x, y]
                                        targethistory[curImgIndex][0] = newtarget;
                                        targethistory[curImgIndex][1] = measures[newtarget][2];
                                        targethistory[curImgIndex][2] = measures[newtarget][3];

                                    }
                                    IJ.log("startAcq: after getRoiOrder " + String.valueOf(targethistory[curImgIndex][0]));
                                    // void drawRoiOrder(int slice, double[][] roiorder, double[][] measures,
                                    // boolean trackstatus)
                                    drawRoiOrder(curImgIndex + 1, finalroiorder, measures, trackstatus);
                                } // if(!useClosest.getState()) end

                                // use targethistory[i][0] to calculate distance from centor.
                                // multiply 2 because resized 1/2
                                // print("target #"+newtarget+" roi at
                                // "+measures[newtarget][2]+","+measures[newtarget][3]);
                                distancefromcenter[0] = width / 4 - measures[(int) targethistory[curImgIndex][0]][2] * 2;
                                distancefromcenter[1] = height / 2 - measures[(int) targethistory[curImgIndex][0]][3] * 2;
                            } // if(measures.length>2) end
                                //
                                // here put stage control code.
                        } else if (tpf.useFullFieldImaging.getState())// full field
                        {
                            roi = new Roi(4, 4, width - 8, height - 8);// trim edge of image since it may have dark
                                                                        // reagion
                            imp.setRoi(roi);
                            ImagePlus inverted = imp.duplicate();
                            ImageProcessor ip_current = inverted.getProcessor();
                            if (tpf.useBrightFieldImaging.getState())// for brightfield
                            {
                                ip_current.invert(); // turn this off for now because it causes flashing 
                            }
                            ImagePlus impinv = new ImagePlus("l", ip_current);
                            // get data and put it into double[] distancefromcenter =new double[2];
                            if (curImgIndex != 0)// after second image
                            {
                                centorofmass = getCenterofMass(impinv, ip_current, roi, 4, 4);// trim 4 pix?
                                distancefromcenter[0] = width / 2 - centorofmass[0];
                                distancefromcenter[1] = height / 2 - centorofmass[1];
                                // targethistory[slicenumber][roi index, x, y]
                                targethistory[curImgIndex][0] = -1;// for center of mass method, the roi index use -1,
                                targethistory[curImgIndex][1] = centorofmass[0];
                                targethistory[curImgIndex][2] = centorofmass[1];
                            } else// first image
                            {
                                centorofmass = getCenterofMass(impinv, ip_current, roi, 0, 0);
                                distancefromcenter[0] = width / 2 - centorofmass[0];
                                distancefromcenter[1] = height / 2 - centorofmass[1];
                                // targethistory[slicenumber][roi index, x, y]
                                targethistory[curImgIndex][0] = -1;// for center of mass method, the roi index use -1,
                                targethistory[curImgIndex][1] = centorofmass[0];
                                targethistory[curImgIndex][2] = centorofmass[1];
                            }
                            imp.setRoi(roi);// just for visible.
                            IJ.log("startAcq: center of mass values " + String.valueOf(centorofmass[0]) + " "
                                    + String.valueOf(centorofmass[1]));
                            IJ.log("startAcq: distance from center " + String.valueOf(distancefromcenter[0]) + " "
                                    + String.valueOf(distancefromcenter[1]));

                        }
                        // if(!tpf.useCenterOfMassTracking.getState()) center of mass end
                        else // for center of mass method
                        {

                            imp.setRoi(leftroi);
                            ImageProcessor ip_current = imp.getProcessor();
                            ImageProcessor ipleft = ip_current.crop();
                            ImagePlus impleft = new ImagePlus("l", ipleft);
                            // get data and put it into double[] distancefromcenter =new double[2];
                            if (curImgIndex != 0)// after second image
                            {
                                if (roiwidth == width / 2 && roiheight == height)// usr didn't drow a roi
                                {
                                    centorofmass = getCenterofMass(impleft, ipleft, roi, 0, 0);// the roi should be
                                                                                                // left roi
                                } else {
                                    int roishiftx = (int) (targethistory[curImgIndex - 1][1] - roiwidth / 2.0);
                                    int roishifty = (int) (targethistory[curImgIndex - 1][2] - roiheight / 2.0);
                                    centorofmass = getCenterofMass(impleft, ipleft, roi, roishiftx, roishifty);// use
                                                                                                                // the
                                                                                                                // previous
                                                                                                                // roi
                                                                                                                // pos
                                }
                                distancefromcenter[0] = width / 4 - centorofmass[0];
                                distancefromcenter[1] = height / 2 - centorofmass[1];
                                // targethistory[slicenumber][roi index, x, y]
                                targethistory[curImgIndex][0] = -1;// for center of mass method, the roi index use -1,
                                targethistory[curImgIndex][1] = centorofmass[0];
                                targethistory[curImgIndex][2] = centorofmass[1];
                            } else// first image
                            {
                                centorofmass = getCenterofMass(impleft, ipleft, leftroi, 0, 0);// this roi is left
                                                                                                // roi or usr
                                                                                                // defined?
                                distancefromcenter[0] = width / 4 - centorofmass[0];
                                distancefromcenter[1] = height / 2 - centorofmass[1];
                                // targethistory[slicenumber][roi index, x, y]
                                targethistory[curImgIndex][0] = -1;// for center of mass method, the roi index use -1,
                                targethistory[curImgIndex][1] = centorofmass[0];
                                targethistory[curImgIndex][2] = centorofmass[1];
                            }
                            imp.setRoi(roi);// just for visible.
                            IJ.log("startAcq: center of mass is " + String.valueOf(centorofmass[0]) + " "
                                    + String.valueOf(centorofmass[1]));
                            IJ.log("startAcq: distance from center is " + String.valueOf(distancefromcenter[0]) + " "
                                    + String.valueOf(distancefromcenter[1]));
                        }
                        double distancescalar = Math.sqrt((distancefromcenter[0]) * (distancefromcenter[0])
                                + (distancefromcenter[1]) * (distancefromcenter[1]));
                        if (distancescalar > LIMIT) {
                            // 100 msec 0.0018?
                            double xv = Math.round(-distancefromcenter[0] * 0.0018 * 1000.0) / 1000.0;
                            double yv = Math.round(distancefromcenter[1] * 0.0018 * 1000.0) / 1000.0;
                            // 10x obj, need to be increased...may be x4?
                            int accelint = 1;
                            if (tpf.stageAccelerationSelector.getSelectedItem() == "1x") {
                                accelint = 1;
                            } else if (tpf.stageAccelerationSelector.getSelectedItem() == "2x") {
                                accelint = 2;
                            } else if (tpf.stageAccelerationSelector.getSelectedItem() == "4x") {
                                accelint = 4;
                            } else if (tpf.stageAccelerationSelector.getSelectedItem() == "5x") {
                                accelint = 5;
                            } else if (tpf.stageAccelerationSelector.getSelectedItem() == "6x") {
                                accelint = 6;
                            }
                            xv = xv * accelint;
                            yv = yv * accelint;
                            IJ.log("startAcq: xv is " + String.valueOf(xv));
                            IJ.log("startAcq: yv is " + String.valueOf(yv));
                            xyStageController.setXYStageVelocity(xv, yv);
                        } else {
                            xyStageController.setXYStageVelocity(0.0, 0.0);
                        } // if(distancescalar>LIMIT) else end
                    } // if manual tracking
                    measurespre = measures;

                    curImgIndex++;
                } // if(skipcount==0)end
            } // if (mmc.getRemainingImageCount() > 0) end
        } // while (mmc.isSequenceRunning()) end

        // after image acquisition finished, set the xy velocity to 0
        xyStageController.setXYStageVelocity(0.0, 0.0);

        Date d2 = new java.util.Date();
        IJ.log("startAcq: finished image acquisition at" + d2.getTime());
        IJ.log(String.valueOf((d2.getTime() - d1.getTime()) / 1000.0) + " sec");

        // output saved data if user selected the option to
        if (!ready && tpf.saveXYPositionsAsTextFile.getState()) {
            saveXYPositionsToTextFile(xposarray, yposarray);
        }

        // toggle live mode because the user may want to perform more than one acquisition
        tpf.app.enableLiveMode(false);
        tpf.app.enableLiveMode(true);
        
    }
}
