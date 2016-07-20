import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Checker {

    private static Point[] normaliseShape(Point[] pts) {
		double minX = pts[0].x;
		double maxX = pts[0].x;
		double minY = pts[0].y;
		double maxY = pts[0].y;
		for (int i = 0; i < pts.length; i++) {
			minX = Math.min(minX, pts[i].x);
			maxX = Math.max(maxX, pts[i].x);
			minY = Math.min(minY, pts[i].y);
			maxY = Math.max(maxY, pts[i].y);
		}
		double rangeX = maxX - minX;
		double rangeY = maxY - minY;
		
		Point[] normalised = new Point[pts.length];
		for (int i = 0; i < pts.length; i++) {
			normalised[i] = new Point((pts[i].x - minX) / rangeX, (pts[i].y - minY) / rangeY);
		}
		
		return normalised;
	}
	
	private static Point[] normalisePosition(Point[] pts) {
		double maxX = 0;
		double maxY = 0;
		for (int i = 0; i < pts.length; i++) {
            maxX = Math.max(maxX, Math.abs(pts[i].x));
            maxY = Math.max(maxY, Math.abs(pts[i].y));
		}

		Point[] normalised = new Point[pts.length];
		for (int i = 0; i < pts.length; i++) {
		    normalised[i] = new Point(pts[i].x / maxX, pts[i].y / maxY);
        }

        return normalised;
	}


    private static double findError(Point[] pts1, Point[] pts2) throws CheckerException {
        if (pts1.length != pts2.length)
            throw new CheckerException("Test segment and drawn segment have different number of points");

        int n = pts1.length;
        double normDegree = 3;

        double err1 = 0;
        for (int i = 0; i < n; i++) {
            err1 += Math.pow(Point.getDist(pts1[i], pts2[i]), normDegree);
        }
        err1 = Math.pow(err1, 1.0 / normDegree) / n;

        double err2 = 0;
        for (int i = 0; i < n; i++) {
            err2 += Math.pow(Point.getDist(pts1[(n-1) - i], pts2[i]), normDegree);
        }
        err2 = Math.pow(err2, 1.0 /normDegree) / n;

        return Math.min(err1, err2);
    }

    private static boolean testSymbols(Symbol[] testSyms, Symbol[] drawnSyms) throws CheckerException {
        if (testSyms.length != drawnSyms.length)
            throw new CheckerException("Test symbols have different number of symbols as drawn symbols");

        int n = testSyms.length;
        boolean isCorrect = true;
        for (int i = 0; i < n; i++) {
            if (!testSyms[i].text.equals(drawnSyms[i].text)) isCorrect = false;
            else if (testSyms[i].bindCurveIdx != drawnSyms[i].bindCurveIdx) isCorrect = false;
            else if (!testSyms[i].category.equals(drawnSyms[i].category)) isCorrect = false;
            else if (testSyms[i].catIndex != drawnSyms[i].catIndex) isCorrect = false;
            else if (testSyms[i].x * drawnSyms[i].x < 0 || testSyms[i].y * drawnSyms[i].y < 0) isCorrect = false;
            if (!isCorrect) break;
        }

        return isCorrect;
    }

    private static Point[] findInterceptX(Point[] pts) {
        LinkedList<Point> interX = new LinkedList<Point>();
        if (pts[0].y == 0)
            interX.add(pts[0]);
        for (int i = 1; i < pts.length; i++) {
            if (pts[i].y == 0) {
                interX.add(pts[i]);
            } else if (pts[i - 1].y * pts[i].y < 0) {
                double dx = pts[i].x - pts[i - 1].x;
                double dy = pts[i].y - pts[i - 1].y;
                double esti = pts[i - 1].x + (dx / dy) * (0 - pts[i - 1].y);
                interX.add(new Point(esti, 0));
            }
        }
        return interX.toArray(new Point[0]);
    }

    private static Point[] findInterceptY(Point[] pts) {
        LinkedList<Point> interY = new LinkedList<Point>();
        if (pts[0].y == 0)
            interY.add(pts[0]);
        for (int i = 1; i < pts.length; i++) {
            if (pts[i].x == 0) {
                interY.add(pts[i]);
            } else if (pts[i - 1].x * pts[i].x < 0) {
                double dx = pts[i].x - pts[i - 1].x;
                double dy = pts[i].y - pts[i - 1].y;
                double esti = pts[i - 1].y + (dy / dx) * (0 - pts[i - 1].x);
                interY.add(new Point(0, esti));
            }
        }
        return interY.toArray(new Point[0]);
    }

    private static Point[] findMaxima(Point[] pts) {
        LinkedList<Point> maxima = new LinkedList<Point>();

        double[] grad = new double[pts.length - 1];
        for (int i = 0; i < pts.length - 1; i++) {
            grad[i] = (pts[i+1].y - pts[i].y) / (pts[i+1].x - pts[i].x);
        }

        for (int i = 1; i < grad.length; i++) {
            if (grad[i-1] > 0 && grad[i] < 0) {
                if ((pts[i].x - pts[i - 1].x) * (pts[i + 1].x - pts[i].x) > 0) {
                    int j = i - 1;
                    while (j >= 0 && Point.getDist(pts[i], pts[j]) < 0.025) j--;
                    if (j < 0) continue;
                    double grad1 = (pts[i].y - pts[j].y) / (pts[i].x - pts[j].x);

                    int k = i + 1;
                    while (k < pts.length && Point.getDist(pts[i], pts[k]) < 0.025) k++;
                    if (k >= pts.length) continue;
                    double grad2 = (pts[k].y - pts[i].y) / (pts[k].x - pts[k].x);

                    if (Math.abs(grad1) > 0.03 && Math.abs(grad2) > 0.03)
                        maxima.add(pts[i]);

                }
            }
        }

        return maxima.toArray(new Point[0]);
    }

    private static Point[] findMinima(Point[] pts) {
        LinkedList<Point> minima = new LinkedList<Point>();

        double[] grad = new double[pts.length - 1];
        for (int i = 0; i < pts.length - 1; i++) {
            grad[i] = (pts[i+1].y - pts[i].y) / (pts[i+1].x - pts[i].x);
        }

        for (int i = 1; i < grad.length; i++) {
            if (grad[i-1] < 0 && grad[i] > 0) {
                if ((pts[i].x - pts[i - 1].x) * (pts[i + 1].x - pts[i].x) > 0) {
                    int j = i - 1;
                    while (j >= 0 && Point.getDist(pts[i], pts[j]) < 0.025) j--;
                    if (j < 0) continue;
                    double grad1 = (pts[i].y - pts[j].y) / (pts[i].x - pts[j].x);

                    int k = i + 1;
                    while (k < pts.length && Point.getDist(pts[i], pts[k]) < 0.025) k++;
                    if (k >= pts.length) continue;
                    double grad2 = (pts[k].y - pts[i].y) / (pts[k].x - pts[k].x);

                    if (Math.abs(grad1) > 0.03 && Math.abs(grad2) > 0.03)
                        minima.add(pts[i]);

                }
            }
        }

        return minima.toArray(new Point[0]);
    }

    private static boolean testKnots(Point[] kts1, Point[] kts2) {

        if (kts1.length != kts2.length)
            return false;

        int n = kts1.length;
        for (int i = 0; i < n; i++) {
            if (kts1[i].x * kts2[i].x < 0) return false;
            if (kts1[i].y * kts2[i].y < 0) return false;
        }

        return true;
    }



	public static String test(String testJSONString, String drawnJSONString) throws CheckerException, ParseException {

        HashMap<String, Object> testData = Parser.parseInputJSONString(testJSONString);
        Point[][] testPtss = (Point[][]) testData.get("ptss");
        Symbol[] testSyms = (Symbol[]) testData.get("symbols");

        HashMap<String, Object> drawnData = Parser.parseInputJSONString(drawnJSONString);
        Point[][] drawnPtss = (Point[][]) drawnData.get("ptss");
        Symbol[] drawnSyms = (Symbol[]) drawnData.get("symbols");

	    boolean isCorrect = true;
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("descriptor", drawnData.get("descriptor"));

	    if (testPtss.length != drawnPtss.length) {
	        isCorrect = false;
//            System.out.println("Fail 'number of segments' test");
            jsonResult.put("test_number of segments", false);
        } else {
//            System.out.println("Pass 'number of segments' test");
            jsonResult.put("test_number of segments", true);

            JSONArray jsonSegmentsResults = new JSONArray();

            int n = testPtss.length;
            double errToleranceShape = 0.01;
            double errTolerancePosition = 0.02;

            for (int i = 0; i < n; i++) {
                JSONObject jsonSegmentResult = new JSONObject();

                double errPosition = findError(normalisePosition(testPtss[i]), normalisePosition(drawnPtss[i]));
                jsonSegmentResult.put("errPosition", errPosition);
                if (errPosition < errTolerancePosition) {
//                    System.out.println("Segment " + (i+1) + " pass 'position' test, error is " + errPosition);
                    jsonSegmentResult.put("test_position", true);
                } else {
//                    System.out.println("Segment " + (i+1) + " fail 'position' test, error is " + errPosition);
                    jsonSegmentResult.put("test_position", false);
                    isCorrect = false;
                }

                double errShape = findError(normaliseShape(testPtss[i]), normaliseShape(drawnPtss[i]));
                jsonSegmentResult.put("errShape", errShape);
                if (errShape < errToleranceShape) {
//                    System.out.println("Segment " + (i+1) + " pass 'shape' test, error is " + errShape);
                    jsonSegmentResult.put("test_shape", true);
                } else {
//                    System.out.println("Segment " + (i+1) + " fail 'shape' test, error is " + errShape);
                    jsonSegmentResult.put("test_shape", false);
                    isCorrect = false;
                }

                if (testKnots(findMaxima(testPtss[i]), findMaxima(drawnPtss[i]))) {
//                    System.out.println("Segment " + (i+1) + " pass 'maxima' test");
                    jsonSegmentResult.put("test_maxima", true);
                } else {
//                    System.out.println("Segment " + (i+1) + " fail 'maxima' test");
                    jsonSegmentResult.put("test_maxima", false);
                    isCorrect = false;
                }

                if (testKnots(findMinima(testPtss[i]), findMinima(drawnPtss[i]))) {
//                    System.out.println("Segment " + (i+1) + " pass 'minima' test");
                    jsonSegmentResult.put("test_minima", true);
                } else {
//                    System.out.println("Segment " + (i+1) + " fail 'minima' test");
                    jsonSegmentResult.put("test_minima", false);
                    isCorrect = false;
                }

                jsonSegmentsResults.add(jsonSegmentResult);
            }

            jsonResult.put("segmentsResults", jsonSegmentsResults);
        }

        if (testSymbols(testSyms, drawnSyms)) {
//            System.out.println("Pass 'symbol' test");
            jsonResult.put("test_symbols", true);
        } else {
//            System.out.println("Fail 'symbol' test");
            jsonResult.put("test_symbols", false);
            isCorrect = false;
        }

        jsonResult.put("isCorrect", isCorrect);

        return jsonResult.toJSONString();
	}


	public static void main(String[] args) throws CheckerException, ParseException, IOException {

	    String testJSONString = FileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/test.json");
        String drawnJSONString = FileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/drawn.json");
        System.out.println(test(testJSONString, drawnJSONString));

    }
	
}