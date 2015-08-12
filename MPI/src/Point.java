import java.util.*;

/**
 * Point data type
 * 
 * @author Pan
 */
public class Point implements Data {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8006614303365080893L;
	public int x;
	public int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int distance(Data d) throws IllegalArgumentException {
		if (!d.getClass().equals(Point.class)) {
			throw new IllegalArgumentException();
		}
		Point p = (Point) d;
		return ((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
	}

	public Data average(List<Data> list)
			throws IllegalArgumentException {
		if (list.size() == 0)
			throw new IllegalArgumentException();
		if (!list.get(0).getClass().equals(Point.class)) {
			throw new IllegalArgumentException();
		}
		long xsum = 0;
		long ysum = 0;
		int num = list.size();
		Iterator<Data> iter = list.iterator();
		while (iter.hasNext()) {
			Point p = (Point) iter.next();
			xsum += p.x;
			ysum += p.y;
		}
		int xavg = (int) (xsum / num);
		int yavg = (int) (ysum / num);
		return new Point(xavg, yavg);
	}

	public String toString() {
		return ("(" + x + ", " + y + ")");
	}
}
