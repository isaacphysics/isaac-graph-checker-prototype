public class Curve {

    private Point[] pts;
    private Knot[] interX;
    private Knot[] interY;
    private Knot[] maxima;
    private Knot[] minima;

    public Point[] getPts() {
        return pts;
    }

    public Knot[] getInterX() {
        return interX;
    }

    public Knot[] getInterY() {
        return interY;
    }

    public Knot[] getMaxima() {
        return maxima;
    }

    public Knot[] getMinima() {
        return minima;
    }

    public void setPts(Point[] pts) {
        this.pts = pts;
    }

    public void setInterX(Knot[] interX) {
        this.interX = interX;
    }

    public void setInterY(Knot[] interY) {
        this.interY = interY;
    }

    public void setMaxima(Knot[] maxima) {
        this.maxima = maxima;
    }

    public void setMinima(Knot[] minima) {
        this.minima = minima;
    }
}
