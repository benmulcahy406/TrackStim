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

void drawRoiOrder(int slice, double[][] roiorder, double[][] measures_, boolean trackstatus) {
    ImageProcessor drawip = binaryimgstack.getProcessor(slice);
    for (int j = 0; j < roiorder.length; j++) {
        String order = String.valueOf((int) roiorder[j][0]);
        IJ.log(order + " slice " + String.valueOf(slice));
        drawip.moveTo((int) measures_[j][2], (int) measures_[j][3]);
        if (trackstatus == true) {
            drawip.setValue(200);
        } else {
            drawip.setValue(100);
        }
        drawip.drawString(order);
    }
}


if (!tpf.useManualTracking.getState() && !tpf.useCenterOfMassTracking.getState() && !tpf.useFullFieldImaging.getState())// for normal thresholding method.
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
    if (i != 0)// after second image
    {
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
        IJ.log("startAcq: before getRoiOrder " + String.valueOf(targethistory[i][0]));
        roiorder = getRoiOrder((int) targethistory[i][0], measures);
        // check target is collect or not by direcion/distance towards next roi. if
        // there are more than 2 rois.
        if (measures.length >= 2) {
            // roiorder [roi#][order by distance from target, distance, dx, dy]
            // returen same format.
            // static double[][] checkDirDis(int slice, double[][] roiorder,double[][]
            // measures)
            double[][] checkedroiorder = checkDirDis(i + 1, roiorder, measures);
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
            IJ.log("startAcq: after getRoiOrder " + String.valueOf(targethistory[i][0]));
            // void drawRoiOrder(int slice, double[][] roiorder, double[][] measures,
            // boolean trackstatus)
            drawRoiOrder(i + 1, finalroiorder, measures, trackstatus);
        } // if(!useClosest.getState()) end

        // use targethistory[i][0] to calculate distance from centor.
        // multiply 2 because resized 1/2
        // print("target #"+newtarget+" roi at
        // "+measures[newtarget][2]+","+measures[newtarget][3]);
        distancefromcenter[0] = width / 4 - measures[(int) targethistory[i][0]][2] * 2;
        distancefromcenter[1] = height / 2 - measures[(int) targethistory[i][0]][3] * 2;
    } // if(measures.length>2) end
        //
        // here put stage control code.
}