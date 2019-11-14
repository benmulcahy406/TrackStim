/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trackstim;

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


class Tracker extends Thread {
    // vaiables recieve from GUI
    TrackStimGUI tpf;
}