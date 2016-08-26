package org.isaacphysics.labs.graph.checker;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;


/**
 * Check correctness of user's graph against the correct graph, and gives error found if there is one.
 */
public final class Checker {

    static final private double ORIGIN_RADIUS = 0.010;
    static final private int NUM_COLOR = 3;

    /**
     * Utility class should not have public or default constructor.
     */
    private Checker() {
        //
    }

    /**
     * normalise points that make up a curve, the resultant points will be within a unit square from (0,0) to (1,1).
     * this normalisation focus on purely the shape of curve
     *
     * @param pts points that make up a curve
     * @return normalised points
     */
    private static Point[] normaliseShape(final Point[] pts) {
        double minX = pts[0].x;
        double maxX = pts[0].x;
        double minY = pts[0].y;
        double maxY = pts[0].y;
        for (int i = 1; i < pts.length; i++) {
            minX = Math.min(minX, pts[i].x);
            maxX = Math.max(maxX, pts[i].x);
            minY = Math.min(minY, pts[i].y);
            maxY = Math.max(maxY, pts[i].y);
        }
        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        Point[] normalised = new Point[pts.length];
        for (int i = 0; i < pts.length; i++) {
            double nx;
            if (rangeX == 0) {
                nx = 0;
            } else {
                nx = (pts[i].x - minX) / rangeX;
            }

            double ny;
            if (rangeY == 0) {
                ny = 0;
            } else {
                ny = (pts[i].y - minY) / rangeY;
            }

            normalised[i] = new Point(nx, ny);
        }

        return normalised;
    }

    /**
     * normalise points that make up a curve, the resultant points will be within a unit square
     * from (-0.5,-0.5) to (0.5,0.5).
     *
     * this normalisation focus on the position of the curve relative to the origin.
     *
     * @param pts points that make up a curve
     * @return normalised points
     */
    private static Point[] normalisePosition(final Point[] pts) {
        double maxX = pts[0].x;
        double maxY = pts[0].y;
        for (int i = 1; i < pts.length; i++) {
            maxX = Math.max(maxX, Math.abs(pts[i].x));
            maxY = Math.max(maxY, Math.abs(pts[i].y));
        }

        Point[] normalised = new Point[pts.length];
        for (int i = 0; i < pts.length; i++) {
            double nx;
            if (maxX == 0) {
                nx = 0;
            } else {
                nx = pts[i].x / maxX;
            }

            double ny;
            if (maxY == 0) {
                ny = 0;
            } else {
                ny = pts[i].y / maxY;
            }

            normalised[i] = new Point(nx, ny);
        }

        return normalised;
    }


    /**
     * Calculate the error between two curves' points using "dynamic time wrapping". The algorithm is on wikipedia.
     *
     * Note the cost is square of distance between two matching points. This is inspired by method in
     * "least error optimisation".
     *
     * @param trusted points of curve of answer
     * @param untrusted points of curve of user
     * @return the measured error.
     */
    private static double findDtwError(final Point[] trusted, final Point[] untrusted) {
        int n = trusted.length;
        int m = untrusted.length;

        double[][] dtw = new double[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            dtw[i][0] = 10000;
        }
        for (int j = 1; j <= m; j++) {
            dtw[0][j] = 10000;
        }
        dtw[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.pow(Point.getDist(trusted[i - 1], untrusted[j - 1]), 2.0);
                dtw[i][j] = cost + Math.min(Math.min(dtw[i - 1][j], dtw[i][j - 1]), dtw[i - 1][j - 1]);
            }
        }

        double err1 = dtw[n][m];

        for (int i = 1; i <= n; i++) {
            dtw[i][0] = 10000;
        }
        for (int j = 1; j <= m; j++) {
            dtw[0][j] = 10000;
        }
        dtw[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.pow(Point.getDist(trusted[i - 1], untrusted[m - j]), 2.0);
                dtw[i][j] = cost + Math.min(Math.min(dtw[i - 1][j], dtw[i][j - 1]), dtw[i - 1][j - 1]);
            }
        }
        double err2 = dtw[n][m];

        return Math.min(err1, err2);
    }

    /**
     * test the position of a set of special points (called knots) of user's curve
     * against the corresponding curve in the answer
     *
     * for example like maximum, minimum, and x,y intercepts.
     *
     * @param trustedKnots the special points of the curve in answer
     * @param untrustedKnots the special points of the user's curve
     * @return true if they match, false otherwise
     */
    private static boolean testKnotsPosition(final Knot[] trustedKnots, final Knot[] untrustedKnots) {
        boolean correct = true;
        for (int i = 0; i < trustedKnots.length; i++) {
            Knot knot1 = trustedKnots[i];
            Knot knot2 = untrustedKnots[i];

            correct = (Math.abs(knot1.y) < ORIGIN_RADIUS && Math.abs(knot2.y) < ORIGIN_RADIUS
                    && Math.abs(knot1.x) < ORIGIN_RADIUS && Math.abs(knot2.x) < ORIGIN_RADIUS)

                    || ((knot1.x * knot2.x >= 0 && knot1.y * knot2.y >= 0)
                    && (Math.abs(knot1.x) - ORIGIN_RADIUS) * (Math.abs(knot2.x) - ORIGIN_RADIUS) >= 0
                    && (Math.abs(knot1.y) - ORIGIN_RADIUS) * (Math.abs(knot2.y) - ORIGIN_RADIUS) >= 0);

            if (!correct) {
                break;
            }
        }

        if (correct) {
            return true;
        }

        // if incorrect, do the same test with reversed untrustedKnot;
        for (int i = 0; i < trustedKnots.length; i++) {
            Knot knot1 = trustedKnots[i];
            Knot knot2 = untrustedKnots[trustedKnots.length - i - 1];

            correct = (Math.abs(knot1.y) < ORIGIN_RADIUS && Math.abs(knot2.y) < ORIGIN_RADIUS
                    && Math.abs(knot1.x) < ORIGIN_RADIUS && Math.abs(knot2.x) < ORIGIN_RADIUS)

                    || ((knot1.x * knot2.x >= 0 && knot1.y * knot2.y >= 0)
                    && (Math.abs(knot1.x) - ORIGIN_RADIUS) * (Math.abs(knot2.x) - ORIGIN_RADIUS) >= 0
                    && (Math.abs(knot1.y) - ORIGIN_RADIUS) * (Math.abs(knot2.y) - ORIGIN_RADIUS) >= 0);

            if (!correct) {
                break;
            }
        }

        return correct;
    }


    /**
     * test the labels of a set of special points (called knots) of user's curve
     * against the corresponding curve in the answer
     *
     * for example like maximum, minimum, and x,y intercepts.
     *
     * @param trustedKnots the special points of the curve in answer
     * @param untrustedKnots the special points of the user's curve
     * @return true if they match, false otherwise
     */
    private static boolean testKnotsSymbols(final Knot[] trustedKnots, final Knot[] untrustedKnots) {
        boolean correct = true;
        for (int i = 0; i < trustedKnots.length; i++) {
            Knot knot1 = trustedKnots[i];
            Knot knot2 = untrustedKnots[i];

            // correct when
            // 1. both knot1 and knot2 has null symbol;
            // 2. both knot has non-null symbol, and two symbols has the same text.
            // the same test is applied also on xSymbol and ySymbol, and for result to be correct, all three tests need
            // to be correct.
            correct = ((knot1.symbol == null && knot2.symbol == null)
                    || (knot1.symbol != null && knot2.symbol != null && knot1.symbol.text.equals(knot2.symbol.text)))

                    && ((knot1.xSymbol == null && knot2.xSymbol == null)
                    || (knot1.xSymbol != null && knot2.xSymbol != null && knot1.xSymbol.text.equals(knot2.xSymbol.text)))

                    && ((knot1.ySymbol == null && knot2.ySymbol == null)
                    || (knot1.ySymbol != null && knot2.ySymbol != null && knot1.ySymbol.text.equals(knot2.ySymbol.text)));

            if (!correct) {
                break;
            }
        }

        if (correct) {
            return true;
        }

        // if incorrect, do the same test with reversed trustedKnots
        correct = true;
        for (int i = 0; i < trustedKnots.length; i++) {
            Knot knot1 = trustedKnots[i];
            Knot knot2 = untrustedKnots[trustedKnots.length - i - 1];

            correct = ((knot1.symbol == null && knot2.symbol == null)
                    || (knot1.symbol != null && knot2.symbol != null && knot1.symbol.text.equals(knot2.symbol.text)))

                    && ((knot1.xSymbol == null && knot2.xSymbol == null)
                    || (knot1.xSymbol != null && knot2.xSymbol != null && knot1.xSymbol.text.equals(knot2.xSymbol.text)))

                    && ((knot1.ySymbol == null && knot2.ySymbol == null)
                    || (knot1.ySymbol != null && knot2.ySymbol != null && knot1.ySymbol.text.equals(knot2.ySymbol.text)));

            if (!correct) {
                break;
            }
        }

        return correct;
    }

    /**
     * split a curve into an array of sections. The split points are turning points.
     * @param curve the input curve
     * @return an array of sections
     */
    private static LinkedList<Point[]> splitCurve(final Curve curve) {
        LinkedList<Knot> knots = new LinkedList<>();
        knots.addAll(Arrays.asList(curve.getMaxima()));
        knots.addAll(Arrays.asList(curve.getMinima()));

        int prev = 0;
        Point[] pts = curve.getPts();
        LinkedList<Point[]> sections = new LinkedList<>();
        for (int i = 0; i < pts.length; i++) {
            for (int j = 0; j < knots.size(); j++) {
                if (pts[i].x == knots.get(j).x && pts[i].y == knots.get(j).y) {
                    knots.remove(j);
                    Point[] tmp = Arrays.copyOfRange(pts, prev, i);
                    sections.push(tmp);
                    prev = i;
                }
            }

            if (knots.size() == 0) {
                break;
            }
        }

        Point[] tmp = Arrays.copyOfRange(pts, prev, pts.length);
        sections.push(tmp);
        return sections;
    }


    /**
     * Test the shape of user's curve against the corresponding curve in the answer.
     * @param trustedCurves curves in the answer
     * @param untrustedCurves corresponding curves of user
     * @return true if two curves are at similar shape, false otherwise
     * @throws CheckerException thrown when one curve is split into wrong number of sections. (this should not happen,
     * if happens, then it is a problem of the splitting algorithm.)
     */
    private static boolean testShape(final Curve[] trustedCurves, final Curve[] untrustedCurves) throws CheckerException {
        double strict = 0.1;
        double loose = 0.5;

        for (int i = 0; i < trustedCurves.length; i++) {

            System.out.println("    Curve " + i);

            LinkedList<Point[]> sec1 = splitCurve(trustedCurves[i]);
            LinkedList<Point[]> sec2 = splitCurve(untrustedCurves[i]);

            if (sec1.size() != sec2.size()) {
                throw new CheckerException("wrong number of sections.");
            }

            boolean equal = true;
            for (int j = 0; j < sec1.size(); j++) {
                Point[] pts1 = normaliseShape(sec1.get(j));
                Point[] pts2 = normaliseShape(sec2.get(j));
                double err = findDtwError(pts1, pts2);
                System.out.println("        sec " + j + ": " + err);

                double tlr;
                if (j == 0 || j == sec1.size() - 1) {
                    tlr = loose;
                } else {
                    tlr = strict;
                }

                if (err > tlr) {
                    equal = false;
                    break;
                }
            }

            if (equal) {
                continue;
            }

            System.out.println("        reverse");

            for (int j = 0; j < sec1.size(); j++) {
                Point[] pts1 = normaliseShape(sec1.get(j));
                Point[] pts2 = normaliseShape(sec2.get(sec1.size() - j - 1));
                double err = findDtwError(pts1, pts2);
                System.out.println("        sec " + j + ": " + err);

                double tlr;
                if (j == 0 || j == sec1.size() - 1) {
                    tlr = loose;
                } else {
                    tlr = strict;
                }

                if (err > tlr) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Test the position of user's curve against the corresponding curve in the answer.
     *
     * @param trustedCurves curves in the answer
     * @param untrustedCurves corresponding curves of user
     * @return true if two curves are at similar position relative to origin, false otherwise
     * @throws CheckerException thrown when two curves have different number of points
     */
    private static boolean testPosition(final Curve[] trustedCurves, final Curve[] untrustedCurves) throws CheckerException {
        for (int i = 0; i < trustedCurves.length; i++) {
            double errPositionDtw = findDtwError(normalisePosition(trustedCurves[i].getPts()), normalisePosition(untrustedCurves[i].getPts()));

            boolean correct = (errPositionDtw < 50)
                    && testKnotsPosition(trustedCurves[i].getInterX(), untrustedCurves[i].getInterX())
                    && testKnotsPosition(trustedCurves[i].getInterY(), untrustedCurves[i].getInterY())
                    && testKnotsPosition(trustedCurves[i].getMaxima(), untrustedCurves[i].getMaxima())
                    && testKnotsPosition(trustedCurves[i].getMinima(), untrustedCurves[i].getMinima());

            if (!correct) {
                return false;
            }
        }

        return true;
    }


    /**
     * Test the position of labels.
     * @param trustedCurves curves in the answer
     * @param untrustedCurves corresponding curves from user
     * @return true if the labels are correctly placed in user's curves
     */
    private static boolean testSymbols(final Curve[] trustedCurves, final Curve[] untrustedCurves) {
        for (int i = 0; i < trustedCurves.length; i++) {
            boolean correct = testKnotsSymbols(trustedCurves[i].getInterX(), untrustedCurves[i].getInterX())
                    && testKnotsSymbols(trustedCurves[i].getInterY(), untrustedCurves[i].getInterY())
                    && testKnotsSymbols(trustedCurves[i].getMaxima(), untrustedCurves[i].getMaxima())
                    && testKnotsSymbols(trustedCurves[i].getMinima(), untrustedCurves[i].getMinima());
            if (!correct) {
                return false;
            }
        }

        return true;
    }


    /**
     * separate curves according to their colors.
     * @param curves the input curves
     * @return an array of array of curves, each array of curve corresponds to curves drawn in one color.
     */
    private static Curve[][] classify(final Curve[] curves) {
        int n = NUM_COLOR;

        ArrayList<ArrayList<Curve>> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            result.add(new ArrayList<>());
        }

        for (Curve c : curves) {
            int idx = c.getColorIdx();
            ArrayList<Curve> list = result.get(idx);
            list.add(c);
        }

        for (ArrayList<Curve> list : result) {
            Collections.sort(list);
        }

        Curve[][] export = new Curve[n][];
        for (int i = 0; i < n; i++) {
            ArrayList<Curve> tmp = result.get(i);
            export[i] = new Curve[tmp.size()];

            for (int j = 0; j < tmp.size(); j++) {
                export[i][j] = tmp.get(j);
            }
        }

        return export;
    }

    /**
     * return a string (name of color) corresponding to a color index.
     * @param idx color index of curve
     * @return a string that includes color name
     */
    private static String getColor(final int idx) {
        String[] colors = {
            "Blue",
            "Orange",
            "Green"
        };
        return colors[idx];
    }

    /**
     * check the correctness of user-plotted graphs against a pre-defined answer.
     *
     * @param targetJSONString a JSON String which contains the correct answer
     * @param testJSONString a JSON String which contains user's answer
     * @return a JSON string containing two field. 1. the test result; 2. the error if there is one.
     *      test result can be true of false
     *      error includes: wrongNumOfCurves, wrongShape, wrongPosition, wrongLabels.
     * @throws CheckerException it is thrown when information are missing in the JSON String, or the JSON string is not
     *      in the correct format. Also, it will be thrown if the information in JSON string is not valid.
     * @throws ParseException it is thrown when input JSON string cannot be parsed. It is thrown by the external library
     *      json.simple.
     */

    static String test(final String targetJSONString, final String testJSONString)
                                                    throws CheckerException, ParseException {
        // parse JSON string
        HashMap<String, Object> trustedData = Parser.parseInputJSONString(targetJSONString);
        Curve[] rawTargetCurves = (Curve[]) trustedData.get("curves");

        HashMap<String, Object> untrustedData = Parser.parseInputJSONString(testJSONString);
        Curve[] rawTestCurves = (Curve[]) untrustedData.get("curves");

        // separate curves according to their colors
        Curve[][] targetClasses = classify(rawTargetCurves);
        Curve[][] testClasses = classify(rawTestCurves);

        // start testing
        JSONObject jsonResult = new JSONObject();

        for (int j = 0; j < NUM_COLOR; j++) {

            /*
             Test: Number of curves
             Test: Size of curves
             Test: Number of intercepts
             Test: Number of turning Pts
             Test: Shape of curve
                    - total error
                    - max error (NOT DONE)
             Test: Position
                    - total error
                    - individual error (NOT DONE)
                    - test positions of intercepts and turning points
             Test: Labels
            */

            String color = getColor(j);
            Curve[] targetCurves = targetClasses[j];
            Curve[] testCurves = testClasses[j];

            if (targetCurves.length == 0 && testCurves.length == 0) {
                continue;
            }

            System.out.println("class " + j + " start test");

            // make sure two graphs have same number of curves
            if (targetCurves.length != testCurves.length) {
                jsonResult.put("errCause", "Color " + color + ": You've drawn the wrong number of curves!");
                jsonResult.put("equal", false);
                return jsonResult.toJSONString();
            }

            // make sure the curve from user is large enough.
            for (int i = 0; i < testCurves.length; i++) {
                Curve c = testCurves[i];
                double rx = c.getMaxX() - c.getMinX();
                double ry = c.getMaxY() - c.getMinY();
                if (rx < 0.2 && ry < 0.2) {
                    jsonResult.put("errCause", "Color " + color + ": One of the curve is too small!");
                    jsonResult.put("equal", false);
                    return jsonResult.toJSONString();
                }
            }

            // make sure each curve has right number of x,y intercepts.
            for (int i = 0; i < targetCurves.length; i++) {
                boolean correct = (targetCurves[i].getInterX().length == testCurves[i].getInterX().length)
                        && (targetCurves[i].getInterY().length == testCurves[i].getInterY().length);
                if (!correct) {
                    jsonResult.put("errCause", "Color " + color + ": One of the curve contains wrong number of intercepts!");
                    jsonResult.put("equal", false);
                    return jsonResult.toJSONString();
                }
            }

            // make sure each curve has right number of turning pts
            for (int i = 0; i < targetCurves.length; i++) {
                boolean correct = (targetCurves[i].getMaxima().length == testCurves[i].getMaxima().length)
                        && (targetCurves[i].getMinima().length == testCurves[i].getMinima().length);
                if (!correct) {
                    jsonResult.put("errCause", "Color " + color + ":One of the curve contains wrong number of turning points.");
                    jsonResult.put("equal", false);
                    return jsonResult.toJSONString();
                }
            }

            // Test the shape of the curve
            if (!testShape(targetCurves, testCurves)) {
                jsonResult.put("errCause", "Color " + color + ": curve is the wrong shape!");
                jsonResult.put("equal", false);
                return jsonResult.toJSONString();
            }

            // Test the position of knots
            if (!testPosition(targetCurves, testCurves)) {
                jsonResult.put("errCause", "Color " + color + ": curve is positioned incorrectly!");
                jsonResult.put("equal", false);
                return jsonResult.toJSONString();
            }

            // Check that the labels are correctly positioned
            if (!testSymbols(targetCurves, testCurves)) {
                jsonResult.put("errCause", "Color " + color + ": labels are incorrectly placed!");
                jsonResult.put("equal", false);
                return jsonResult.toJSONString();
            }

            System.out.println();
        }

        // If we make it to here, we have an exact match with the correct answer.
        jsonResult.put("errCause", "null");
        jsonResult.put("equal", true);
        return jsonResult.toJSONString();
    }


    public static void main(final String[] args) throws CheckerException, ParseException, IOException {

        String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/target.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/test.json");
        System.out.println(test(trustedJSONString, untrustedJSONString));

    }

}