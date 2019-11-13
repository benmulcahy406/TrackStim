from ij import IJ, ImagePlus  

from ij.process import ImageProcessor, ImageStatistics

from ij.plugin.filter import RankFilters, EDM

from ij.gui import Roi, PolygonRoi, Wand, ImageCanvas, PointRoi

from ij.measure.Measurements import MEAN, CENTER_OF_MASS, CENTROID

import math

# core idea behind the full field and optionally the bright field option
# if it is clicked
# get image and duplicate it
original = IJ.getImage()
width = original.getWidth()
height = original.getHeight()
original.setRoi(Roi(4, 4, width - 8, height - 8))
inverted = original.duplicate()
inverted_roi = inverted.getRoi()
inverted_processor = inverted.getProcessor()

# invert the image (related to checking the use bright field imaging option)
# this is not run if the bright field option is not clicked
inverted_processor.invert()
#inverted.show()


# get the mean pixel value forom the inverted image
mean_pixel_value = inverted.getStatistics(MEAN).mean # interpret the mean as the background value
# compute the value to subtract constant
# this is independent of the actual image
# meaning different images may end up fully black after this calculation
value_to_subtract = -1 * mean_pixel_value * 1.5
inverted_processor.add(value_to_subtract)


# filter image by the median value
rf = RankFilters()
rf.rank(inverted_processor, 0.0, RankFilters.MEDIAN)

# compute the center of mass of the image
stats = inverted.getStatistics(CENTER_OF_MASS + CENTROID)

center_of_mass = PointRoi(stats.xCenterOfMass, stats.yCenterOfMass)

inverted.setRoi(center_of_mass)

# compute the distance from the center, and the distance scalar
distance_from_center = (width / 2 - stats.xCenterOfMass, height / 2 - stats.yCenterOfMass)


# this value is checked to see if the stage should move to a new xy position
distance_from_center_scalar = math.sqrt((distance_from_center[0]) * (distance_from_center[0])
                                    + (distance_from_center[1]) * (distance_from_center[1]))

print distance_from_center
print distance_from_center_scalar
                                   
inverted.show()


