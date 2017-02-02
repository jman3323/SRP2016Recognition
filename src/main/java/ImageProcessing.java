

import java.io.File;
import java.io.IOException;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Rectangle;

public class ImageProcessing {

	//---------------------------------------Variables--------------------------------------------------------//
	
	/**
	 * Segments - An array of images that are filled by splitting up the original
	 * image into the defined set of parts
	 */
	private MBFImage[] segments = new MBFImage[1000000];
	
	/**
	 * comparators - An array of booleans to determine whether or not the object is noise
	 */
	private boolean comparators[] = new boolean[1000000];
	
	/**
	 * grayScalePixels - Represents the pixels that contain a threshold. Based on the user's
	 * threshold -- these pixels will appear white (1) on the screen, or they will show up as 
	 * the gray-scale representation of the pixel
	 */
	private float[][] grayScalePixels = null;
	
	/**
	 * whiteOut - This is the array of pixels that is used after determining whether or not the 
	 * segment contains an object or just has random noise. If the image segment deems to only
	 * have noise, instead of an actual object, the image segment will have all of its pixels 
	 * match the color values of whiteOut, which all equal '1'
	 */
	private float[][] whiteOut = null;
	
	public int[] values = new int[2];
	
	int previousX = 0;
	int previousY = 0;
	
	//---------------------------------------Constructor--------------------------------------------------------//
	
	/**
	 * Input an image to process. Once you put an image, it automatically fills the 
	 * 'pixels', 'grayScalePixels', and 'whiteOut' arrays to the image's width and height
	 * @param image
	 */
	public ImageProcessing(MBFImage image) {
		grayScalePixels = new float[image.getHeight()][image.getWidth()];
		whiteOut = new float[image.getHeight()][image.getWidth()];
		
		//All whiteOut pixels have a value of 1 -- this function does this for you
		performWhiteOut(image, whiteOut);
	}
	
	//---------------------------------------Main Method--------------------------------------------------------//

	public static void main(String[] args) throws IOException {
		 //Step 1
		 //Read in the image using 'OpenImaj'
		MBFImage image = ImageUtilities.readMBF(new File("/Users/Anthony/Desktop/running.png"));
		
		 //Step 2
		 //Create a clone
		MBFImage clone = image.clone();
		
		 //Step 3
		 //Fill in all arrays - you are now able to process clone
		ImageProcessing processor = new ImageProcessing(clone);

		 //Step 4
		 //Main logic of the processor
		//processor.analyzeWhiteSegments(clone);
		//DisplayUtilities.display(clone);
		processor.reduceNoise(clone, 100, 110); //Makes the boxes
		processor.performSegmentation(clone, 100, 110, processor.values); //Gets rid of unnecessary boxes
		
		 //Step 5
		 //Display the image
		DisplayUtilities.display(clone);

	}
	
	//-----------------------------------------Methods----------------------------------------------------------//
	
	public void reduceNoise(MBFImage clone, int tileWidth, int tileHeight) {
		int x = 0;
		int y = 0;
		int width = tileWidth;
		int height = tileHeight;
		int counter = 0;
		for (int yPos = 0; yPos <= (clone.getHeight() / height); yPos++) {
			width = tileWidth;
			height = tileHeight;
			for (int xPos = 0; xPos <= (clone.getWidth() / width); xPos++) {
				Rectangle bounds = new Rectangle(x, y, getWidth(width, clone, x), getHeight(height, clone, y));
				MBFImage segment = clone.extractROI(bounds);
				//clone.drawShape(bounds, 1, RGBColour.ORANGE);
				segments[counter] = segment;
				if (analyzeWhiteSegments(segments[counter]) > 1) {
					comparators[counter] = true;
				}
				counter++;
				x += width;
			}
			y += height;
			x = 0;
		}
	}
	
	public void performSegmentation(MBFImage clone, int tileWidth, int tileHeight, int[] values) {
		values[0] = 0;
		values[1] = 0;
		int personCounter = 0;
		int x = 0;
		int y = 0;
		int width = tileWidth;
		int height = tileHeight;
		int counter = 0;
		//double doubleWidth = (double) tileWidth;
		//double totalTiles = (double) (clone.getWidth() / doubleWidth);
		//int totalTilesDone = ((int) Math.ceil(totalTiles));
		for (int yPos = 0; yPos <= (clone.getHeight() / height); yPos++) {
			width = tileWidth;
			height = tileHeight;
			for (int xPos = 0; xPos <= (clone.getWidth() / width); xPos++) {
				Rectangle bounds = new Rectangle(x, y, getWidth(width, clone, x), getHeight(height, clone, y));
				MBFImage segment = clone.extractROI(bounds);
				segments[counter] = segment;
				if (analyzeWhiteSegments(segments[counter]) > 100 && analyzeBlackSegments(segments[counter]) > 20) {
					if (counter > 0) {
						if (determineIfSamePerson(values[0], values[1], xPos, yPos) == false) {
							personCounter++;
						} else {
							//continue -- same person
							System.out.println("Same Person");
						}
					} else {
						//values[0] = xPos;
						//values[1] = yPos;
						//determineIfSamePerson(values[0], values[1], xPos, yPos);
						//personCounter++;
					}
					if (personCounter == 1) {
						clone.drawShape(bounds, 1, RGBColour.RED);
					} else if (personCounter == 2) {
						clone.drawShape(bounds, 1, RGBColour.GREEN);
					} else if (personCounter == 3) {
						clone.drawShape(bounds, 1, RGBColour.BLUE);
					} else if (personCounter == 4) {
						clone.drawShape(bounds, 1, RGBColour.ORANGE);
					} else if (personCounter == 5) {
						clone.drawShape(bounds, 1, RGBColour.PINK);
					} else if (personCounter == 6) {
						clone.drawShape(bounds, 1, RGBColour.GRAY);
					} else if (personCounter == 7) {
						clone.drawShape(bounds, 1, RGBColour.CYAN);
					} else {
						clone.drawShape(bounds, 1, RGBColour.MAGENTA);
					}
					System.out.println("Person " + personCounter + " positioned at (" + xPos + ", " + yPos + ")");
					values[0] = xPos;
					values[1] = yPos;
					/*
					//if (comparators[counter + 1] == false //Next
							//&& comparators[counter - 1] == false) //Before
								//&& comparators[counter - totalTilesDone] == false //Above
									//&& comparators[counter + totalTilesDone] == false) //Below
					{
						//continue
					} else {
						clone.drawShape(bounds, 1, RGBColour.RED);
					}
					*/
				}
				counter++;
				x += width;
			}
			y += height;
			x = 0;
		}
	}
	
	public boolean determineIfSamePerson(int x1, int y1, int x2, int y2) {
		boolean isSamePerson = true;
		int xDifference = Math.abs(x2 - x1);
		int yDifference = Math.abs(y2 - y1);
		int totalDifference = xDifference - yDifference;
		//System.out.println(x1);
		//System.out.println(y1);
		//System.out.println("X DIFF: " + xDifference + " || Y DIFF: " + yDifference);
		//System.out.println("XDifference: " + xDifference);
		//System.out.println("YDifference: " + yDifference);
		//System.out.println("TOTALDifference: " + totalDifference);
		if (xDifference > 1 || yDifference > 1 || totalDifference > 1) {
			isSamePerson = false;
		} else {
			isSamePerson = true;
		}
		return isSamePerson;
	}
	
	public void performWhiteOut(MBFImage image, float[][] whiteOut) {
		for (int y=1; y<image.getHeight() - 1; y++) {
		    for(int x=1; x<image.getWidth() - 1; x++) {
		    	whiteOut[y][x] = 1;
		    } 
		}
	}

	public int analyzeWhiteSegments(MBFImage image) {
		int counter = 0;
		int y = 1;
		int x = 1;
		for (y=1; y<image.getHeight() - 1; y++) {
		    for(x=1; x<image.getWidth() - 1; x++) {
		    	float sumFloat = 0;
		        float redColor = image.getBand(0).pixels[y][x];
		        float greenColor = image.getBand(1).pixels[y][x];
		        float blueColor = image.getBand(2).pixels[y][x];
		        sumFloat = (((redColor*256) + (greenColor*256) + (blueColor*256)) / 3);
		        if (sumFloat/256 < 0.98)
		        {
		        	sumFloat = 1;
		        }
		        else
		        {
		        	sumFloat = sumFloat / 256;
		        	counter++;
		        }
		        grayScalePixels[y][x] = sumFloat;
		    } 
		}
		
		if (counter > 10) {
			colorInImage(image, grayScalePixels);
		} else {
			colorInImage(image, whiteOut);
		}
		
		//if (counter != 0)
			//System.out.println("White Pixels Found: " + counter);
		return counter;
	}
	
	public int analyzeBlackSegments(MBFImage image) {
		int counter = 0;
		int y = 1;
		int x = 1;
		for (y=1; y<image.getHeight() - 1; y++) {
		    for(x=1; x<image.getWidth() - 1; x++) {
		    	float sumFloat = 0;
		        float redColor = image.getBand(0).pixels[y][x];
		        float greenColor = image.getBand(1).pixels[y][x];
		        float blueColor = image.getBand(2).pixels[y][x];
		        sumFloat = (((redColor*256) + (greenColor*256) + (blueColor*256)) / 3);
		        if (sumFloat/256 > 0.99) {
		        	sumFloat = 1;
		        } else {
		        	sumFloat = sumFloat / 256;
		        	counter++;
		        }
		        grayScalePixels[y][x] = sumFloat;
		    } 
		}
		
		if (counter > 10) {
			colorInImage(image, grayScalePixels);
		} else {
			colorInImage(image, whiteOut);
		}
		
		//System.out.println("Black Pixels Found: " + counter);
		return counter;
	}
	
	public static void colorInImage(MBFImage image, float[][] pixels) {
		for (int y=1; y<image.getHeight() - 1; y++) {
		    for(int x=1; x<image.getWidth() - 1; x++) {
		        image.getBand(0).setPixel(x, y, pixels[y][x]);
		        image.getBand(1).setPixel(x, y, pixels[y][x]);
		        image.getBand(2).setPixel(x, y, pixels[y][x]);
		    } 
		}
	}

	public static int getWidth(int currentWidth, MBFImage image, int currentX) {
		if (currentWidth < (image.getWidth() - currentX)) {
			// continue
		} else {
			if ((image.getWidth() - currentX - 1) < 0) {
				currentWidth = (image.getWidth() - currentX);
			} else {
				currentWidth = (image.getWidth() - currentX - 1);
			}
		}
		return currentWidth;
	}

	public static int getHeight(int currentHeight, MBFImage image, int currentY) {
		if ((image.getHeight() - currentHeight) > currentY) { //(200-100) < (50) || (100) < (50) 
			// continue
		} else {
			if ((image.getHeight() - currentY - 1) < 0) {
				currentHeight = (image.getHeight() - currentY);
			} else {
				currentHeight = (image.getHeight() - currentY - 1);
			}
		}
		return currentHeight;
	}
}
