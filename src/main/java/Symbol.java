public class Symbol extends Point {

    String text;
    int bindCurveIdx;
    String category;
    int catIndex;

    public Symbol(double x, double y, String text, int bindCurveIdx, String category, int catIndex) {
        super(x, y);
        this.text = text;
        this.bindCurveIdx = bindCurveIdx;
        this.category = category;
        this.catIndex = catIndex;
    }
}
