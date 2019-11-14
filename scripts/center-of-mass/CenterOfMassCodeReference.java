    
// the code relevant to when full field tracking is enabled
// else if (tpf.useFullFieldImaging.getState())// full field


// for non-thresholding method
// this returns x y of centor of mass backgournd subtracted
static double[] getCenterofMass(ImagePlus imp, ImageProcessor ip, Roi roi, int x, int y) {
    ImagePlus imp_ = imp;
    ImageProcessor ip_ = ip;
    Roi roi_ = roi;
    ImageStatistics imstat_ = imp_.getStatistics(2);
    int backgroundvalue = (int) imstat_.mean;
    ImageProcessor ip2 = ip_.duplicate();
    ip2.add(-backgroundvalue * 1.5);
    ImagePlus imp2 = new ImagePlus("subtracted", ip2);
    roi_.setLocation(x, y);
    imp2.setRoi(roi_);

    // median filter ver 7 test
    RankFilters rf = new RankFilters();
    rf.rank(ip2, 0.0, 4);// median 4 periodic black white noize cause miss thresholding, so eliminate
                            // those noize
    ImageStatistics imstat2 = imp2.getStatistics(64 + 32);
    double[] returnval = { imstat2.xCenterOfMass, imstat2.yCenterOfMass };
    countslice++;
    return (returnval);
}

imp.setRoi(leftroi);
ImageProcessor ip_current = imp.getProcessor();
ImageProcessor ipleft = ip_current.crop();
ImagePlus impleft = new ImagePlus("l", ipleft);
// get data and put it into double[] distancefromcenter =new double[2];
if (i != 0)// after second image
{
    if (roiwidth == width / 2 && roiheight == height)// usr didn't drow a roi
    {
        centorofmass = getCenterofMass(impleft, ipleft, roi, 0, 0);// the roi should be
                                                                    // left roi
    } else {
        int roishiftx = (int) (targethistory[i - 1][1] - roiwidth / 2.0);
        int roishifty = (int) (targethistory[i - 1][2] - roiheight / 2.0);
        centorofmass = getCenterofMass(impleft, ipleft, roi, roishiftx, roishifty);// use
                                                                                    // the
                                                                                    // previous
                                                                                    // roi
                                                                                    // pos
    }
    distancefromcenter[0] = width / 4 - centorofmass[0];
    distancefromcenter[1] = height / 2 - centorofmass[1];
    // targethistory[slicenumber][roi index, x, y]
    targethistory[i][0] = -1;// for center of mass method, the roi index use -1,
    targethistory[i][1] = centorofmass[0];
    targethistory[i][2] = centorofmass[1];
} else// first image
{
    centorofmass = getCenterofMass(impleft, ipleft, leftroi, 0, 0);// this roi is left
                                                                    // roi or usr
                                                                    // defined?
    distancefromcenter[0] = width / 4 - centorofmass[0];
    distancefromcenter[1] = height / 2 - centorofmass[1];
    // targethistory[slicenumber][roi index, x, y]
    targethistory[i][0] = -1;// for center of mass method, the roi index use -1,
    targethistory[i][1] = centorofmass[0];
    targethistory[i][2] = centorofmass[1];
}
imp.setRoi(roi);// just for visible.
IJ.log("startAcq: center of mass is " + String.valueOf(centorofmass[0]) + " "
        + String.valueOf(centorofmass[1]));
IJ.log("startAcq: distance from center is " + String.valueOf(distancefromcenter[0]) + " "
        + String.valueOf(distancefromcenter[1]));