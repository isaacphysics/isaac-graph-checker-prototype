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


import java.io.IOException;
import java.util.*;

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
            throw new CheckerException("Trusted curve and untrusted curve have different number of points");

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

    public static boolean testPosition(Point[] trustedPts, Point[] untrustedPts) throws CheckerException {
        double errTolerancePosition = 0.02;
        double errPosition = findError(normalisePosition(trustedPts), normalisePosition(untrustedPts));
        if (errPosition < errTolerancePosition) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean testShape(Point[] trustedPts, Point[] untrustedPts) throws CheckerException {
        double errToleranceShape = 0.01;
        double errShape = findError(normaliseShape(trustedPts), normaliseShape(untrustedPts));
        if (errShape < errToleranceShape) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean testKnotsSymbols(Knot[] trustedKnots, Knot[] untrustedKnots) {
        if (trustedKnots.length != untrustedKnots.length) return false;

        List<Knot> knots1 = Arrays.asList(trustedKnots);
        List<Knot> knots2 = Arrays.asList(untrustedKnots);
        boolean correct;

        correct = true;
        for (int i = 0; i < knots1.size(); i++) {
            Knot knot1 = knots1.get(i);
            Knot knot2 = knots2.get(i);

            if (knot1.symbol != null && knot2.symbol == null) correct = false;
            else if (knot1.symbol == null && knot2.symbol != null) correct = false;
            else if (knot1.symbol != null && knot2.symbol != null) {
                if (!knot1.symbol.text.equals(knot2.symbol.text)) correct = false;
            }
            if (!correct) break;

            if (knot1.xSymbol != null && knot2.xSymbol == null) correct = false;
            else if (knot1.xSymbol == null && knot2.xSymbol != null) correct = false;
            else if (knot1.xSymbol != null && knot2.xSymbol != null) {
                if (!knot1.xSymbol.text.equals(knot2.xSymbol.text)) correct = false;
            }
            if (!correct) break;

            if (knot1.ySymbol != null && knot2.ySymbol == null) correct = false;
            else if (knot1.ySymbol == null && knot2.ySymbol != null) correct = false;
            else if (knot1.ySymbol != null && knot2.ySymbol != null) {
                if (!knot1.ySymbol.text.equals(knot2.ySymbol.text)) correct = false;
            }
            if (!correct) break;
        }

        if (correct) return true;

        Collections.reverse(knots2);

        correct = true;
        for (int i = 0; i < knots1.size(); i++) {
            Knot knot1 = knots1.get(i);
            Knot knot2 = knots2.get(i);

            if (knot1.symbol != null && knot2.symbol == null) correct = false;
            else if (knot1.symbol == null && knot2.symbol != null) correct = false;
            else if (knot1.symbol != null && knot2.symbol != null) {
                if (!knot1.symbol.text.equals(knot2.symbol.text)) correct = false;
            }
            if (!correct) break;

            if (knot1.xSymbol != null && knot2.xSymbol == null) correct = false;
            else if (knot1.xSymbol == null && knot2.xSymbol != null) correct = false;
            else if (knot1.xSymbol != null && knot2.xSymbol != null) {
                if (!knot1.xSymbol.text.equals(knot2.xSymbol.text)) correct = false;
            }
            if (!correct) break;

            if (knot1.ySymbol != null && knot2.ySymbol == null) correct = false;
            else if (knot1.ySymbol == null && knot2.ySymbol != null) correct = false;
            else if (knot1.ySymbol != null && knot2.ySymbol != null) {
                if (!knot1.ySymbol.text.equals(knot2.ySymbol.text)) correct = false;
            }
            if (!correct) break;
        }

        return correct;
    }



    private static boolean testKnotsPosition(Knot[] trustedKnots, Knot[] untrustedKnots) {

        if (trustedKnots.length != untrustedKnots.length) return false;

        List<Knot> knots1 = Arrays.asList(trustedKnots);
        List<Knot> knots2 = Arrays.asList(untrustedKnots);
        boolean correct;

        correct = true;
        for (int i = 0; i < trustedKnots.length; i++) {
            Knot knot1 = knots1.get(i);
            Knot knot2 = knots2.get(i);


            if (Math.abs(knot1.x) < 0.025 && Math.abs(knot2.x) < 0.025) {
                if ((Math.abs(knot1.y) < 0.025 && Math.abs(knot2.y) < 0.025)) {
                    continue;
                } else if (knot1.y * knot2.y < 0) {
                    correct = false;
                }
            } else if (Math.abs(knot1.y) < 0.025 && Math.abs(knot2.y) < 0.025) {
                if (knot1.x * knot2.x < 0) {
                    correct = false;
                }
            } else if ((knot1.x * knot2.x < 0) || (knot1.y * knot2.y < 0)) {
                correct = false;
            }

            if (!correct) break;
        }

        if (correct) return true;
        Collections.reverse(knots2);

        correct = true;
        for (int i = 0; i < trustedKnots.length; i++) {
            Knot knot1 = knots1.get(i);
            Knot knot2 = knots2.get(i);

            if (Math.abs(knot1.x) < 0.025 && Math.abs(knot2.x) < 0.025) {
                if (Math.abs(knot1.y) < 0.025 && Math.abs(knot2.y) < 0.025) {
                    continue;
                } else if (knot1.y * knot2.y < 0) {
                    correct = false;
                }
            } else if (Math.abs(knot1.y) < 0.025 && Math.abs(knot2.y) < 0.025) {
                if (knot1.x * knot2.x < 0) {
                    correct = false;
                }
            } else if ((knot1.x * knot2.x < 0) || (knot1.y * knot2.y < 0)) {
                correct = false;
            }

            if (!correct) break;
        }

        return correct;
    }


	public static String test(String trustedJSONString, String untrustedJSONString) throws CheckerException, ParseException {

        HashMap<String, Object> trustedData = Parser.parseInputJSONString(trustedJSONString);
        Curve[] trustedCurves = (Curve[]) trustedData.get("curves");

        HashMap<String, Object> untrustedData = Parser.parseInputJSONString(untrustedJSONString);
        Curve[] untrustedCurves = (Curve[]) untrustedData.get("curves");

        JSONObject jsonResult = new JSONObject();

	    if (trustedCurves.length != untrustedCurves.length) {
            jsonResult.put("errCause", "wrongNumOfCurves");
            jsonResult.put("isCorrect", false);
            return jsonResult.toJSONString();
        }

        for (int i = 0; i < trustedCurves.length; i++) {
            if (!testShape(trustedCurves[i].getPts(), untrustedCurves[i].getPts())) {
                jsonResult.put("errCause", "wrongShape");
                jsonResult.put("isCorrect", false);
                return jsonResult.toJSONString();
            }
        }

        for (int i = 0; i < trustedCurves.length; i++) {
            boolean correct = true;
            if (!testPosition(trustedCurves[i].getPts(), untrustedCurves[i].getPts())) {
                correct = false;
            } else if (!testKnotsPosition(trustedCurves[i].getInterX(), untrustedCurves[i].getInterX())) {
                correct = false;
            } else if (!testKnotsPosition(trustedCurves[i].getInterY(), untrustedCurves[i].getInterY())) {
                correct = false;
            } else if (!testKnotsPosition(trustedCurves[i].getMaxima(), untrustedCurves[i].getMaxima())) {
                correct = false;
            } else if (!testKnotsPosition(trustedCurves[i].getMinima(), untrustedCurves[i].getMinima())) {
                correct = false;
            }

            if (!correct) {
                jsonResult.put("errCause", "wrongPosition");
                jsonResult.put("isCorrect", false);
                return jsonResult.toJSONString();
            }
        }

        for (int i = 0; i < trustedCurves.length; i++) {
            boolean correct = true;
            if (!testKnotsSymbols(trustedCurves[i].getInterX(), untrustedCurves[i].getInterX())) {
                correct = false;
            } else if (!testKnotsSymbols(trustedCurves[i].getInterY(), untrustedCurves[i].getInterY())) {
                correct = false;
            } else if (!testKnotsSymbols(trustedCurves[i].getMaxima(), untrustedCurves[i].getMaxima())) {
                correct = false;
            } else if (!testKnotsSymbols(trustedCurves[i].getMinima(), untrustedCurves[i].getMinima())) {
                correct = false;
            }

            if (!correct) {
                jsonResult.put("errCause", "wrongLabels");
                jsonResult.put("isCorrect", false);
                return jsonResult.toJSONString();
            }
        }

        jsonResult.put("errCause", "null");
        jsonResult.put("isCorrect", true);
        return jsonResult.toJSONString();
	}


	public static void main(String[] args) throws CheckerException, ParseException, IOException {

	    String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/test.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/drawn.json");
        System.out.println(test(trustedJSONString, untrustedJSONString));

    }
	
}