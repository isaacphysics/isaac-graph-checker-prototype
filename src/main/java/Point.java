public class Point {
	final double x;
	final double y;


	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public static double getDist(Point pt1, Point pt2) {
		return Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
	}

}
