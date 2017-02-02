import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class OCR implements Comparator<int[]>
{
	public static void main(String[] args) throws Exception
	{
		BufferedImage img = ImageIO.read(new File("llama2.jpg"));
		grayscale(img);
		ImageIO.write(img, "png", new File("gray.png"));
		int[][] binary = binarize(img, .5);
		binary = edgeDetect(img, 50);
		BufferedImage bw = binImage(binary);
		
		ImageIO.write(bw, "png", new File("binarized.png"));
		ArrayList<TreeSet<int[]>> blobs = genBlobs(binary);
		
		Color[] colors = {Color.BLACK, Color.BLUE, Color.GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.RED, Color.WHITE, Color.YELLOW, Color.CYAN, Color.PINK};
		BufferedImage blobImage = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		for (int i = 0; i < blobs.size(); i++)
			for (int[] pt : blobs.get(i))
				blobImage.setRGB(pt[1], pt[0], colors[i % colors.length].getRGB());
		ImageIO.write(blobImage, "png", new File("blobs.png"));
	}
	
	/**
	 * Finds contiguous sets of 0s or 1s. The sets are ordered top-left to bottom-right
	 * @param binary
	 * @return
	 */
	private static ArrayList<TreeSet<int[]>> genBlobs (int[][] binary)
	{
		ArrayList<TreeSet<int[]>> blobs = new ArrayList<>();
		boolean[][] visited = new boolean[binary.length][binary[0].length];
		for (int i = 0; i < visited.length; i++)
			for (int j = 0; j < visited[i].length; j++)
				if (!visited[i][j])
					blobs.add(genBlob(i, j, visited, binary));
		return blobs;
	}
	
	/**
	 * Helper method for genBlobs
	 * @param i
	 * @param j
	 * @param visited
	 * @param binary
	 * @return
	 */
	private static TreeSet<int[]> genBlob (int i, int j, boolean[][] visited, int[][] binary)
	{
		TreeSet<int[]> blob = new TreeSet<>(new OCR());
		ArrayList<int[]> points = new ArrayList<>();
		points.add(new int[] {i, j});
		visited[i][j] = true;
		int[][] moves = {{0,1}, {0,-1}, {1,0}, {-1,0}};
		while (!points.isEmpty())
		{
			int[] point = points.remove(points.size() - 1);
			int x = point[0], y = point[1];
			blob.add(point);
			for (int[] move : moves)
			{
				int newX = x + move[0], newY = y + move[1];
				if (newX < 0 || newX >= binary.length || newY < 0 || newY >= binary[newX].length)
					continue;
				if (visited[newX][newY])
					continue;
				if (binary[newX][newY] != binary[x][y])
					continue;
				points.add(new int[] {newX, newY});
				visited[newX][newY] = true;
			}
		}
		return blob;
	}

	/**
	 * Converts to grayscale
	 * @param img
	 */
	private static void grayscale (BufferedImage img)
	{
		for (int i = 0; i < img.getHeight(); i++)
			for (int j = 0; j < img.getWidth(); j++)
			{
				Color c = new Color(img.getRGB(j, i));
				int avg = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
				img.setRGB(j, i, new Color(avg, avg, avg).getRGB());
			}
	}

	/**
	 * Converts to black and white based on threshold percentage (.5 means anyhting 50% black is black)
	 * @param img
	 * @param threshold
	 * @return
	 */
	private static int[][] binarize(BufferedImage img, double threshold)
	{
		double maxBlack = threshold * 256;
		int[][] bin = new int[img.getHeight()][img.getWidth()];
		for (int i = 0; i < bin.length; i++)
			for (int j = 0; j < bin[i].length; j++)
				if ((img.getRGB(j, i) & 0xff) < maxBlack)
					bin[i][j] = 1;
				else
					bin[i][j] = 0;
		return bin;
	}

	/**
	 * Convert 0/1 array to image
	 * @param bin
	 * @return
	 */
	private static BufferedImage binImage (int[][] bin)
	{
		BufferedImage img = new BufferedImage(bin[0].length, bin.length, BufferedImage.TYPE_BYTE_BINARY);
		for (int i = 0; i < bin.length; i++)
			for (int j = 0; j < bin[i].length; j++)
				img.setRGB(j, i, 0xffffffff - bin[i][j] * 0xffffffff);
		return img;
	}
	
	private static int[][] edgeDetect (BufferedImage img, double threshold)
	{
		int[][] bin = new int[img.getHeight()][img.getWidth()];
		int[][] moves = {{0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}};
		for (int i = 0; i < img.getHeight(); i++)
			for (int j = 0; j < img.getWidth(); j++)
			{
				boolean edge = false;
				for (int[] move : moves)
				{
					int x = j + move[0], y = i + move[1];
					if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight())
						continue;
					if (dist(img.getRGB(x, y), img.getRGB(j, i)) > threshold)
					{
						edge = true;
						break;
					}
				}
				if (edge)
					bin[i][j] = 1;
				else
					bin[i][j] = 0;
			}
		return bin;
	}
	
	private static double dist (int rgb1, int rgb2)
	{
		Color c1 = new Color(rgb1), c2 = new Color(rgb2);
		double res = Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2) + Math.pow(c1.getGreen() - c2.getGreen(), 2);
		res = Math.sqrt(res);
		return res;
	}
	
	public int compare (int[] a, int[] b)
	{
		if (a[0] < b[0])
			return -1;
		if (a[0] > b[0])
			return 1;
		if (a[1] < b[1])
			return -1;
		if (a[1] > b[1])
			return 1;
		return 0;
	}
}
