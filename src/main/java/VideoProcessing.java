
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import org.openimaj.image.*;
import org.openimaj.image.colour.*;
import org.openimaj.video.*;
import org.openimaj.video.xuggle.*;

public class VideoProcessing
{	
	public static void main(String[] args)
	{
		/**
		 * Get the Video File and Display It
		 */
		Video<MBFImage> video = new XuggleVideo("objectSearch.mov");

		int div = 2;
		XuggleVideoWriter writer = new XuggleVideoWriter("videoOut.mp4", video.getWidth(), video.getHeight(), video.getFPS() / div);
		writer.initialise();

		DisplayUtilities.createNamedWindow("feed");
		DisplayUtilities.displayName(new MBFImage(video.getWidth(), video.getHeight()), "feed");
		int i = 0;
		for (MBFImage orig : video)
		{
			if (i++ % div != 0)
				continue;
			MBFImage frame = orig.clone();
			Processor.replaceAll(frame, 0f, .095);
			int[][] edges = Processor.edgeDetect(frame, .17, 3); //.17, 3 triathlon, .048, 2 water
			Processor.thinEdges(edges);
			//orig = Processor.binImage(edges).toRGB();
			ArrayList<TreeSet<int[]>[]> blobs = Processor.genWhiteBlobsWithBorder(edges);
			HashSet<Integer> points = new HashSet<>();
			for (TreeSet<int[]>[] blob : blobs)
				if (blob[0].size() < 7000 && blob[0].size() > 17)
				{
					//for (TreeSet<int[]> set : blob)
						for (int[] pt : blob[1])
							if (!points.remove(pt[0] * edges[0].length + pt[1]))
								points.add(pt[0] * edges[0].length + pt[1]);
				}
			for (Integer pt : points)
			{
				int x = pt % edges[0].length, y = pt / edges[0].length;
				orig.setPixel(x, y, RGBColour.RED);
				orig.setPixel(x + 1, y, RGBColour.RED);
				orig.setPixel(x, y + 1, RGBColour.RED);
			}
			DisplayUtilities.updateNamed("feed", orig, "feed");
			writer.addFrame(orig);
			System.out.println(i + " " + video.countFrames());







			//with openIMAJ
			/*
			 * MBFImage frame = orig.clone();
			int[][] edges = Processor.edgeDetect(frame, .17, 3);
			FImage bw = Processor.binImage(edges);
			bw.processInplace(new FFastGaussianConvolve(.4f, 100));
			edges = Processor.binarize(bw.toRGB(), 0f, .5f, .51f);
			ArrayList<TreeSet<int[]>> blobs = Processor.genWhiteBlobs(edges);
			//DisplayUtilities.updateNamed("feed", Processor.binImage(edges), "feed");
			orig = Processor.binImage(edges).toRGB();
			for (TreeSet<int[]> blob : blobs)
				if (blob.size() < 7000 && blob.size() > 10)
				{
					int[] first = blob.first(), last = blob.last();
					int y = first[0], x = first[1], w = last[1] - x, h = last[0] - y;
					//orig.drawShape(new Rectangle(x, y, w, h), 2, RGBColour.RED);
					for (int[] pt : blob)
						orig.setPixel(pt[1], pt[0], RGBColour.RED);
				}
			DisplayUtilities.updateNamed("feed", orig, "feed");
			 */

			/*MBFImage frame = orig.clone();
			Processor.replaceAll(frame, .6f, .095);

			ArrayList<Rectangle> boxes = Processor.genRelevantBoxesAndGrayscale(frame, 80, 80);

			Processor.replaceWhite(frame, .6f, .8);
			int[][] binary = Processor.binarize(frame, .0f, .45f, .7f);

			ArrayList<TreeSet<int[]>> blobs = Processor.genRelevantBlobs(binary, boxes);
			frame.fill(new Float[] {1f, 1f, 1f});
			int minSize = 0, maxSize = 500;

			//for (Rectangle r : boxes) orig.drawShape(r, RGBColour.RED);

			double minRadius = 3.7;
			for (TreeSet<int[]> blob : blobs)
			{
				double[] center = isCircularEdge(blob, 3.5);
				if (center[0] != -1 && center[2] >= minRadius)
					orig.drawShape(new Circle((float) center[1], (float) center[0], (float) center[2]), 2, RGBColour.RED);
				// brown .05416 green .0123 box .0580
			}

			DisplayUtilities.updateNamed("feed", orig, "feed");
			writer.addFrame(orig);
			System.out.println(i + " " + video.countFrames());*/
		}
		writer.processingComplete();
		//writer.close();
		System.out.println("DONE");

		/**
		 * Iterate through the frames of the file
		 * "mbfImage" is the iterator which acts as each individual frame through the run through
		 */
		/*VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
		display.addVideoListener(new VideoDisplayListener<MBFImage>()
		{
			public void beforeUpdate(MBFImage orig)
			{		
				ImageProcessing processor = new ImageProcessing(orig);
				processor.reduceNoise(orig, 80, 80);
				processor.performSegmentation(orig, 80, 80, processor.values);


				MBFImage frameOut = new MBFImage(orig.getWidth() * 2, orig.getHeight() * 2);
				MBFImage frame = orig.clone(), origClone = orig.clone();
				Processor.replaceAll(frame, .6f, .095);
				//DisplayUtilities.display(frame);
				//pause(200000);
				//orig.drawImage(frame, 0, 0);
				MBFImage scaledClone = frame.clone();
				frameOut.drawImage(scaledClone, scaledClone.getWidth(), 0);
				scaledClone = ResizeProcessor.halfSize(scaledClone);
				orig.drawImage(scaledClone, scaledClone.getWidth(), 0);

				ArrayList<Rectangle> boxes = Processor.genRelevantBoxesAndGrayscale(frame, 80, 80);

				scaledClone = frame.clone();
				for (Rectangle r : boxes) scaledClone.drawShape(r, RGBColour.RED);
				frameOut.drawImage(scaledClone, 0, scaledClone.getHeight());
				scaledClone = ResizeProcessor.halfSize(scaledClone);
				orig.drawImage(scaledClone, 0, scaledClone.getHeight());

				Processor.replaceWhite(frame, .6f, .8);
				int[][] binary = Processor.binarize(frame, .0f, .45f, .7f);
				scaledClone = frame.clone();
				frameOut.drawImage(scaledClone, scaledClone.getWidth(), scaledClone.getHeight());
				scaledClone = ResizeProcessor.halfSize(scaledClone);
				orig.drawImage(scaledClone, scaledClone.getWidth(), scaledClone.getHeight());

				ArrayList<TreeSet<int[]>> blobs = Processor.genRelevantBlobs(binary, boxes);
				frame.fill(new Float[] {1f, 1f, 1f});
				int minSize = 0, maxSize = 500;

				//for (Rectangle r : boxes) orig.drawShape(r, RGBColour.RED);

				double minRadius = 3.7;
				for (TreeSet<int[]> blob : blobs)
				{
					double[] center = isCircularEdge(blob, 3.5);
					if (center[0] != -1 && center[2] >= minRadius)
						origClone.drawShape(new Circle((float) center[1], (float) center[0], (float) center[2]), RGBColour.RED);
					for (int[] i : blob)
					{
						orig.getBand(0).pixels[i[0]][i[1]] = 1f;
						orig.getBand(1).pixels[i[0]][i[1]] = 0f;
						orig.getBand(2).pixels[i[0]][i[1]] = 0f;
					}
				}
				scaledClone = origClone;
				frameOut.drawImage(scaledClone, 0, 0);
				scaledClone = ResizeProcessor.halfSize(scaledClone);
				orig.drawImage(scaledClone, 0, 0);

				writer.addFrame(frameOut);
				//pause(100);
			}

			public void afterUpdate(VideoDisplay<MBFImage> display)
			{}
		});*/


	}

	private static void pause(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static boolean isCircularCombo (TreeSet<int[]> blob)
	{
		boolean dev = isCircular(blob, 3.5)[0] != -1;
		boolean dist = isCircularAvgDist(blob, .3);
		if (!dev && !dist)
			return false;
		if (dev && dist)
			return true;
		boolean area = isCircularArea(blob, .5);
		if (dev && area || dist && area)
			return true;
		return false;
	}

	private static double[] isCircular(TreeSet<int[]> blob, double maxDev)
	{
		// Finds "center" by averaging x's and y's
		double x = 0, y = 0;
		for (int[] i : blob)
		{
			x += i[0];
			y += i[1];
		}
		x /= blob.size();
		y /= blob.size();

		double avgDist = 0, maxRadius = 0;
		ArrayList<Double> dists = new ArrayList<>();
		for (int[] i : blob)
		{
			double dist = Math.sqrt(Math.pow(i[0] - x, 2) + Math.pow(i[1] - y, 2));
			if (dist > maxRadius)
				maxRadius = dist;
			avgDist += dist;
			dists.add(dist);
		}
		avgDist /= blob.size();
		double dev = 0;
		for (double dist : dists)
			dev += Math.pow(dist - avgDist, 2);
		dev /= dists.size();
		dev = Math.sqrt(dev);
		if (dev < maxDev)
			return new double[] {x, y, maxRadius};
		return new double[] {-1, -1, -1};
	}

	private static double[] isCircularEdge(TreeSet<int[]> blob, double maxDev)
	{
		// Finds "center" by averaging x's and y's
		double x = 0, y = 0;
		TreeMap<Integer, ArrayList<Integer>> rows = new TreeMap<>();
		for (int[] i : blob)
		{
			if (!rows.containsKey(i[1]))
				rows.put(i[1], new ArrayList<Integer>());
			rows.get(i[1]).add(i[0]);
		}

		ArrayList<int[]> consideredPoints = new ArrayList<>();
		for (int row : rows.keySet())
			if (row == rows.firstKey() || row == rows.lastKey())
				for (int col : rows.get(row))
				{
					x += col;
					y += row;
					consideredPoints.add(new int[] {col, row});
				}
			else
			{
				x += rows.get(row).get(0);
				y += row;
				consideredPoints.add(new int[] {rows.get(row).get(0), row});
				if (rows.get(row).size() > 1)
				{
					x += rows.get(row).get(rows.get(row).size() - 1);
					y += row;
					consideredPoints.add(new int[] {rows.get(row).get(rows.get(row).size() - 1), row});
				}
			}

		x /= consideredPoints.size();
		y /= consideredPoints.size();

		double avgDist = 0, maxRadius = 0;
		ArrayList<Double> dists = new ArrayList<>();
		for (int[] i : consideredPoints)
		{
			double dist = Math.sqrt(Math.pow(i[0] - x, 2) + Math.pow(i[1] - y, 2));
			if (dist > maxRadius)
				maxRadius = dist;
			avgDist += dist;
			dists.add(dist);
		}
		avgDist /= dists.size();
		double dev = 0;
		for (double dist : dists)
			dev += Math.pow(dist - avgDist, 2);
		dev /= dists.size();
		dev = Math.sqrt(dev);
		if (dev < maxDev)
			return new double[] {x, y, maxRadius};
		return new double[] {-1, -1, -1};
	}

	private static boolean isCircularAvgDist (TreeSet<int[]> blob, double maxError)
	{
		// Finds "center" by averaging x's and y's
		double x = 0, y = 0;
		for (int[] i : blob)
		{
			x += i[0];
			y += i[1];
		}
		x /= blob.size();
		y /= blob.size();

		double avgDist = 0, maxRadius = 0;
		ArrayList<Double> dists = new ArrayList<>();
		for (int[] i : blob)
		{
			double dist = Math.sqrt(Math.pow(i[0] - x, 2) + Math.pow(i[1] - y, 2));
			if (dist > maxRadius)
				maxRadius = dist;
			avgDist += dist;
			dists.add(dist);
		}
		avgDist /= blob.size();
		double trueAvgDist = Math.PI * maxRadius / 4.0;
		double error = Math.abs(avgDist - trueAvgDist) / trueAvgDist;
		if (error > maxError)
			return false;
		return true;
	}

	private static boolean isCircularArea (TreeSet<int[]> blob, double threshold)
	{
		// Finds "center" by averaging x's and y's
		double x = 0, y = 0;
		for (int[] i : blob)
		{
			x += i[0];
			y += i[1];
		}
		x /= blob.size();
		y /= blob.size();

		double maxRadius = 0;
		for (int[] i : blob)
		{
			double dist = Math.sqrt(Math.pow(i[0] - x, 2) + Math.pow(i[1] - y, 2));
			if (dist > maxRadius)
				maxRadius = dist;
		}
		double area = Math.PI * maxRadius * maxRadius;
		double percentageFilled = blob.size() / area;
		if (percentageFilled > threshold)
			return true;
		return false;
	}

	/**
	 * go until has thresh% of points
	 * @param blob
	 */
	private static boolean isCircularArea2 (TreeSet<int[]> blob)
	{
		// Finds "center" by averaging x's and y's
		double x = 0, y = 0;
		for (int[] i : blob)
		{
			x += i[0];
			y += i[1];
		}
		x /= blob.size();
		y /= blob.size();

		ArrayList<Double> dists = new ArrayList<>();
		ArrayList<int[]> points = new ArrayList<>();
		for (int[] i : blob)
		{
			double dist = Math.sqrt(Math.pow(i[0] - x, 2) + Math.pow(i[1] - y, 2));
			int insert = 0;
			while (insert < dists.size() && dists.get(insert) > dist)
				insert++;
			dists.add(insert, dist);
		}
		System.out.println(dists);
		return false;
	}
}