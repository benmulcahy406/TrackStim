    
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
if (i != 0)// after second image
{
    centorofmass = getCenterofMass(impinv, ip_current, roi, 4, 4);// trim 4 pix?
    distancefromcenter[0] = width / 2 - centorofmass[0];
    distancefromcenter[1] = height / 2 - centorofmass[1];
    // targethistory[slicenumber][roi index, x, y]
    targethistory[i][0] = -1;// for center of mass method, the roi index use -1,
    targethistory[i][1] = centorofmass[0];
    targethistory[i][2] = centorofmass[1];
} else// first image
{
    centorofmass = getCenterofMass(impinv, ip_current, roi, 0, 0);
    distancefromcenter[0] = width / 2 - centorofmass[0];
    distancefromcenter[1] = height / 2 - centorofmass[1];
    // targethistory[slicenumber][roi index, x, y]
    targethistory[i][0] = -1;// for center of mass method, the roi index use -1,
    targethistory[i][1] = centorofmass[0];
    targethistory[i][2] = centorofmass[1];
}
imp.setRoi(roi);// just for visible.
IJ.log("startAcq: center of mass values " + String.valueOf(centorofmass[0]) + " "
        + String.valueOf(centorofmass[1]));
IJ.log("startAcq: distance from center " + String.valueOf(distancefromcenter[0]) + " "
        + String.valueOf(distancefromcenter[1]));
