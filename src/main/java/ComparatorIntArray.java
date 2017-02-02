import java.util.Comparator;

public class ComparatorIntArray implements Comparator<int[]>
{
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
