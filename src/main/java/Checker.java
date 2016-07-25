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


    private static boolean testKnots(Knot[] knots1, Knot[] knots2) {

        if (knots1.length != knots2.length)
            return false;

        int n = knots1.length;
        for (int i = 0; i < n; i++) {
            if (knots1[i].x * knots2[i].x < 0) return false;
            if (knots1[i].y * knots2[i].y < 0) return false;

            if (knots1[i].symbol != null && knots2[i].symbol == null) return false;
            else if (knots1[i].symbol == null && knots2[i].symbol != null) return false;
            else if (knots1[i].symbol != null && knots2[i].symbol != null) {
                if (knots1[i].symbol.text != knots2[i].symbol.text) return false;
            }
        }

        return true;
    }



	public static String test(String trustedJSONString, String untrustedJSONString) throws CheckerException, ParseException {

        HashMap<String, Object> trustedData = Parser.parseInputJSONString(trustedJSONString);
        Curve[] trustedCurves = (Curve[]) trustedData.get("Curves");

        HashMap<String, Object> untrustedData = Parser.parseInputJSONString(untrustedJSONString);
        Curve[] untrustedCurves = (Curve[]) untrustedData.get("Curves");

	    boolean isCorrect = true;
        JSONObject jsonResult = new JSONObject();

        String descriptor = (String) untrustedData.get("descriptor");
        jsonResult.put("descriptor", descriptor);
        System.out.println("Descriptor: " + descriptor);

	    if (trustedCurves.length != untrustedCurves.length) {
	        isCorrect = false;
            System.out.println("Fail 'number of curves' test");
            jsonResult.put("'number of curves' test", false);
        } else {
            System.out.println("Pass 'number of segments' test");
            jsonResult.put("'number of curves' test", true);


            int n = trustedCurves.length;
            JSONArray jsonCurveResults = new JSONArray();

            for (int i = 0; i < n; i++) {
                JSONObject jsonCurveResult = new JSONObject();

                double errTolerancePosition = 0.02;
                double errPosition = findError(normalisePosition(trustedCurves[i].getPts()), normalisePosition(untrustedCurves[i].getPts()));
                jsonCurveResult.put("errPosition", errPosition);
                if (errPosition < errTolerancePosition) {
                    System.out.println("Curve " + (i+1) + " pass 'position' test, error is " + errPosition);
                    jsonCurveResult.put("position test", true);
                } else {
                    System.out.println("Curve " + (i+1) + " fail 'position' test, error is " + errPosition);
                    jsonCurveResult.put("position test", false);
                    isCorrect = false;
                }

                double errToleranceShape = 0.01;
                double errShape = findError(normaliseShape(trustedCurves[i].getPts()), normaliseShape(untrustedCurves[i].getPts()));
                jsonCurveResult.put("errShape", errShape);
                if (errShape < errToleranceShape) {
                    System.out.println("Curve " + (i+1) + " pass 'shape' test, error is " + errShape);
                    jsonCurveResult.put("shape test", true);
                } else {
                    System.out.println("Curve " + (i+1) + " fail 'shape' test, error is " + errShape);
                    jsonCurveResult.put("shape test", false);
                    isCorrect = false;
                }

                if (testKnots(trustedCurves[i].getInterX(), untrustedCurves[i].getInterX())) {
                    System.out.println("Curve " + (i+1) + " pass 'interX' test");
                    jsonCurveResult.put("interX test", true);
                } else {
                    System.out.println("Curve " + (i+1) + " fail 'interX' test");
                    jsonCurveResult.put("interX test", false);
                    isCorrect = false;
                }

                if (testKnots(trustedCurves[i].getInterY(), untrustedCurves[i].getInterY())) {
                    System.out.println("Curve " + (i+1) + " pass 'interY' test");
                    jsonCurveResult.put("interY test", true);
                } else {
                    System.out.println("Curve " + (i+1) + " fail 'interY' test");
                    jsonCurveResult.put("interY test", false);
                    isCorrect = false;
                }

                if (testKnots(trustedCurves[i].getMaxima(), untrustedCurves[i].getMaxima())) {
                    System.out.println("Curve " + (i+1) + " pass 'maxima' test");
                    jsonCurveResult.put("maxima test", true);
                } else {
                    System.out.println("Curve " + (i+1) + " fail 'maxima' test");
                    jsonCurveResult.put("maxima test", false);
                    isCorrect = false;
                }

                if (testKnots(trustedCurves[i].getMinima(), untrustedCurves[i].getMinima())) {
                    System.out.println("Curve " + (i+1) + " pass 'minima' test");
                    jsonCurveResult.put("minima test", true);
                } else {
                    System.out.println("Curve " + (i+1) + " fail 'minima' test");
                    jsonCurveResult.put("minima test", false);
                    isCorrect = false;
                }

                jsonCurveResults.add(jsonCurveResult);
            }

            jsonResult.put("details", jsonCurveResults);
        }

        jsonResult.put("isCorrect", isCorrect);
        return jsonResult.toJSONString();
	}


	public static void main(String[] args) throws CheckerException, ParseException, IOException {

	    String trustedJSONString = FileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/test.json");
        String untrustedJSONString = FileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/drawn.json");
        System.out.println(test(trustedJSONString, untrustedJSONString));

    }
	
}