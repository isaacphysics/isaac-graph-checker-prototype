/**
 * Copyright 2016 Junwei Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class CheckerBackup {

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

    public static JSONObject testPosition(Point[] trustedPts, Point[] untrustedPts) throws CheckerException {
        JSONObject jsonResult = new JSONObject();

        double errTolerancePosition = 0.02;
        double errPosition = findError(normalisePosition(trustedPts), normalisePosition(untrustedPts));
        jsonResult.put("errPosition", errPosition);
        if (errPosition < errTolerancePosition) {
            jsonResult.put("correct", true);
        } else {
            jsonResult.put("correct", false);
        }

        return jsonResult;
    }

    public static JSONObject testShape(Point[] trustedPts, Point[] untrustedPts) throws CheckerException {
        JSONObject jsonResult = new JSONObject();

        double errToleranceShape = 0.01;
        double errShape = findError(normaliseShape(trustedPts), normaliseShape(untrustedPts));
        jsonResult.put("errShape", errShape);
        if (errShape < errToleranceShape) {
            jsonResult.put("correct", true);
        } else {
            jsonResult.put("correct", false);
        }

        return jsonResult;
    }



    private static JSONObject testKnots(Knot[] knots1, Knot[] knots2) {

        JSONObject jsonResults = new JSONObject();

        if (knots1.length != knots2.length) {
            jsonResults.put("sameNumberOfKnots", false);
            jsonResults.put("correct", false);
            return jsonResults;
        } else {
            jsonResults.put("sameNumberOfKnots", true);
        }

        boolean correct = true;
        JSONArray jsonKnotResults = new JSONArray();
        int n = knots1.length;
        for (int i = 0; i < n; i++) {
            JSONObject jsonKnotResult = new JSONObject();

            if ((knots1[i].x * knots2[i].x < 0) || (knots1[i].y * knots2[i].y < 0)) {
                jsonKnotResult.put("inSameQuadrant", false);
            } else {
                jsonKnotResult.put("inSameQuadrant", true);
            }

            if (knots1[i].symbol == null && knots2[i].symbol == null) {
                jsonKnotResult.put("sameSymbol", true);
            } else if (knots1[i].symbol != null && knots2[i].symbol == null) {
                jsonKnotResult.put("sameSymbol", false);
                correct = false;
            } else if (knots1[i].symbol == null && knots2[i].symbol != null) {
                jsonKnotResult.put("sameSymbol", false);
                correct = false;
            } else if (knots1[i].symbol != null && knots2[i].symbol != null) {
                if (!knots1[i].symbol.text.equals(knots2[i].symbol.text)) {
                    jsonKnotResult.put("sameSymbol", false);
                    correct = false;
                } else {
                    jsonKnotResult.put("sameSymbol", true);
                }
            }

            if (knots1[i].xSymbol == null && knots2[i].xSymbol == null) {
                jsonKnotResult.put("sameXSymbol", true);
            } else if (knots1[i].xSymbol != null && knots2[i].xSymbol == null) {
                jsonKnotResult.put("sameXSymbol", false);
                correct = false;
            } else if (knots1[i].xSymbol == null && knots2[i].xSymbol != null) {
                jsonKnotResult.put("sameXSymbol", false);
                correct = false;
            } else if (knots1[i].xSymbol != null && knots2[i].xSymbol != null) {
                if (!knots1[i].xSymbol.text.equals(knots2[i].xSymbol.text)) {
                    jsonKnotResult.put("sameXSymbol", false);
                    correct = false;
                } else {
                    jsonKnotResult.put("sameXSymbol", true);
                }
            }

            if (knots1[i].ySymbol == null && knots2[i].ySymbol == null) {
                jsonKnotResult.put("sameYSymbol", true);
            } else if (knots1[i].ySymbol != null && knots2[i].ySymbol == null) {
                jsonKnotResult.put("sameYSymbol", false);
                correct = false;
            } else if (knots1[i].ySymbol == null && knots2[i].ySymbol != null) {
                jsonKnotResult.put("sameYSymbol", false);
                correct = false;
            } else if (knots1[i].ySymbol != null && knots2[i].ySymbol != null) {
                if (!knots1[i].ySymbol.text.equals(knots2[i].ySymbol.text)) {
                    jsonKnotResult.put("sameYSymbol", false);
                    correct = false;
                } else {
                    jsonKnotResult.put("sameYSymbol", true);
                }
            }

            jsonKnotResults.add(jsonKnotResult);
        }

        jsonResults.put("knotResults", jsonKnotResults);
        jsonResults.put("correct", correct);
        return jsonResults;
    }



	public static String test(String trustedJSONString, String untrustedJSONString) throws CheckerException, ParseException {

        HashMap<String, Object> trustedData = Parser.parseInputJSONString(trustedJSONString);
        Curve[] trustedCurves = (Curve[]) trustedData.get("curves");

        HashMap<String, Object> untrustedData = Parser.parseInputJSONString(untrustedJSONString);
        Curve[] untrustedCurves = (Curve[]) untrustedData.get("curves");

        String descriptor = (String) untrustedData.get("descriptor");
//        System.out.println("Descriptor: " + descriptor);

        JSONObject jsonResult = new JSONObject();

	    if (trustedCurves.length != untrustedCurves.length) {
//            System.out.println("sameNumOfCurves: false");
//            System.out.println("false");
            jsonResult.put("sameNumOfCurves", false);
            jsonResult.put("correct", false);
            return jsonResult.toJSONString();
        } else {
//            System.out.println("sameNumOfCurves: true");
            jsonResult.put("sameNumOfCurves", true);

            int n = trustedCurves.length;
            JSONArray jsonCurveResults = new JSONArray();
            boolean correct = true;

            for (int i = 0; i < n; i++) {
                JSONObject jsonCurveResult = new JSONObject();

                JSONObject json;

                json = testShape(trustedCurves[i].getPts(), untrustedCurves[i].getPts());
                jsonCurveResult.put("shapeTest", json);
                if ((Boolean) json.get("correct")) {
//                    System.out.println((i+1) + " shapeTest: true");
                } else {
//                    System.out.println((i+1) + " shapeTest: false");
                    correct = false;
                }

                json = testPosition(trustedCurves[i].getPts(), untrustedCurves[i].getPts());
                jsonCurveResult.put("positionTest", json);
                if ((Boolean) json.get("correct")) {
//                    System.out.println((i+1) + " positionTest: true");
                } else {
//                    System.out.println((i+1) + " positionTest: false");
                    correct = false;
                }

                json = testKnots(trustedCurves[i].getInterX(), untrustedCurves[i].getInterX());
                jsonCurveResult.put("interXTest", json);
                if ((Boolean) json.get("correct")) {
//                    System.out.println((i+1) + " interXTest: true");
                } else {
//                    System.out.println((i+1) + " interXTest: false");
                    correct = false;
                }

                json = testKnots(trustedCurves[i].getInterY(), untrustedCurves[i].getInterY());
                jsonCurveResult.put("interYTest", json);
                if ((Boolean) json.get("correct")) {
//                    System.out.println((i+1) + " interYTest: true");
                } else {
//                    System.out.println((i+1) + " interYTest: false");
                    correct = false;
                }

                json = testKnots(trustedCurves[i].getMaxima(), untrustedCurves[i].getMaxima());
                jsonCurveResult.put("maximaTest", json);
                if ((Boolean) json.get("correct")) {
//                    System.out.println((i+1) + " maximaTest: true");
                } else {
//                    System.out.println((i+1) + " maximaTest: false");
                    correct = false;
                }

                json = testKnots(trustedCurves[i].getMinima(), untrustedCurves[i].getMinima());
                jsonCurveResult.put("minimaTest", json);
                if ((Boolean) json.get("correct")) {
//                    System.out.println((i+1) + " minimaTest: true");
                } else {
//                    System.out.println((i+1) + " minimaTest: false");
                    correct = false;
                }

                jsonCurveResults.add(jsonCurveResult);
            }

            jsonResult.put("curveResults", jsonCurveResults);
            jsonResult.put("correct", correct);
//            System.out.println(correct);
            System.out.println(jsonResult.toJSONString());
            return jsonResult.toJSONString();
        }

	}


	public static void main(String[] args) throws CheckerException, ParseException, IOException {

	    String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/test.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/drawn.json");
        test(trustedJSONString, untrustedJSONString);

    }
	
}