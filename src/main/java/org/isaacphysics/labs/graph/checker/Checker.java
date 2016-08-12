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
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;


/**
 * Check correctness of user's graph against the correct graph, and gives error found if there is one.
 */
public final class Checker {

    static final double NORM_DEGREE = 3;
    static final double ERR_TOLERANCE_POSITION = 0.02;
    static final double ERR_TOLERANCE_SHAPE = 0.01;
    static final double ORIGIN_RADIUS = 0.025;

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
     * estimate the difference (error) between two curves.
     *
     * @param pts1 points of one of the curve
     * @param pts2 points of the other curve
     * @return the estimated error
     * @throws CheckerException thrown if the two curves have different number of points
     */
    private static double findError(final Point[] pts1, final Point[] pts2) throws CheckerException {
        if (pts1.length != pts2.length) {
            throw new CheckerException("Trusted curve and untrusted curve have different number of points");
        }

        int n = pts1.length;


        double err1 = 0;
        for (int i = 0; i < n; i++) {
            err1 += Math.pow(Point.getDist(pts1[i], pts2[i]), NORM_DEGREE);
        }
        err1 = Math.pow(err1, 1.0 / NORM_DEGREE) / n;

        double err2 = 0;
        for (int i = 0; i < n; i++) {
            err2 += Math.pow(Point.getDist(pts1[(n - 1) - i], pts2[i]), NORM_DEGREE);
        }
        err2 = Math.pow(err2, 1.0 / NORM_DEGREE) / n;

        return Math.min(err1, err2);
    }

    /**
     * Test the position of user's curve against the corresponding curve in the answer.
     *
     * @param trustedPts points of curve in the answer
     * @param untrustedPts points of corresponding curve of user
     * @return true if two curves are at similar position relative to origin, false otherwise
     * @throws CheckerException thrown when two curves have different number of points
     */
    public static boolean testPosition(final Point[] trustedPts, final Point[] untrustedPts) throws CheckerException {
        double errPosition = findError(normalisePosition(trustedPts), normalisePosition(untrustedPts));
        return errPosition < ERR_TOLERANCE_POSITION;
    }

    /**
     * Test the shape of user's curve against the corresponding curve in the answer.
     *
     * @param trustedPts points of curve in the answer
     * @param untrustedPts points of corresponding curve of user
     * @return true if two curves are at similar shape, false otherwise
     * @throws CheckerException thrown when two curves have different number of points
     */
    public static boolean testShape(final Point[] trustedPts, final Point[] untrustedPts) throws CheckerException {
        double errShape = findError(normaliseShape(trustedPts), normaliseShape(untrustedPts));
        return errShape < ERR_TOLERANCE_SHAPE;
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
        if (trustedKnots.length != untrustedKnots.length) {
            return false;
        }

        boolean correct = true;
        for (int i = 0; i < trustedKnots.length && correct; i++) {
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
        }

        if (correct) {
            return true;
        }

        // if incorrect, do the same test with reversed trustedKnots
        correct = true;
        for (int i = 0; i < trustedKnots.length && correct; i++) {
            Knot knot1 = trustedKnots[i];
            Knot knot2 = untrustedKnots[trustedKnots.length - i - 1];

            correct = ((knot1.symbol == null && knot2.symbol == null)
                    || (knot1.symbol != null && knot2.symbol != null && knot1.symbol.text.equals(knot2.symbol.text)))

                    && ((knot1.xSymbol == null && knot2.xSymbol == null)
                    || (knot1.xSymbol != null && knot2.xSymbol != null && knot1.xSymbol.text.equals(knot2.xSymbol.text)))

                    && ((knot1.ySymbol == null && knot2.ySymbol == null)
                    || (knot1.ySymbol != null && knot2.ySymbol != null && knot1.ySymbol.text.equals(knot2.ySymbol.text)));
        }

        return correct;
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
        if (trustedKnots.length != untrustedKnots.length) {
            return false;
        }

        boolean correct = true;
        for (int i = 0; i < trustedKnots.length && correct; i++) {
            Knot knot1 = trustedKnots[i];
            Knot knot2 = untrustedKnots[i];

            // correct when
            // 1. two knots are in the same quadrant
            // 2. two knots are near x axis, and they are at the same side of y axis
            // 3. two knots are near y axis, and they are at the same side of x axis
            // 4. two knots are around the origin
            correct = (knot1.x * knot2.x >= 0 && knot1.y * knot2.y >= 0)

                    || (Math.abs(knot1.y) < ORIGIN_RADIUS && Math.abs(knot2.y) < ORIGIN_RADIUS
                    && knot1.x * knot2.x >= 0)

                    || (Math.abs(knot1.x) < ORIGIN_RADIUS && Math.abs(knot2.x) < ORIGIN_RADIUS
                    && knot1.y * knot2.y >= 0)

                    || (Math.abs(knot1.y) < ORIGIN_RADIUS && Math.abs(knot2.y) < ORIGIN_RADIUS
                    && Math.abs(knot1.x) < ORIGIN_RADIUS && Math.abs(knot2.x) < ORIGIN_RADIUS);
        }

        if (correct) {
            return true;
        }

        // if incorrect, do the same test with reversed untrustedKnots
        correct = true;
        for (int i = 0; i < trustedKnots.length && correct; i++) {
            Knot knot1 = trustedKnots[i];
            Knot knot2 = untrustedKnots[trustedKnots.length - i - 1];
            correct = (knot1.x * knot2.x >= 0 && knot1.y * knot2.y >= 0)

                    || (Math.abs(knot1.y) < ORIGIN_RADIUS && Math.abs(knot2.y) < ORIGIN_RADIUS
                    && knot1.x * knot2.x >= 0)

                    || (Math.abs(knot1.x) < ORIGIN_RADIUS && Math.abs(knot2.x) < ORIGIN_RADIUS
                    && knot1.y * knot2.y >= 0)

                    || (Math.abs(knot1.y) < ORIGIN_RADIUS && Math.abs(knot2.y) < ORIGIN_RADIUS
                    && Math.abs(knot1.x) < ORIGIN_RADIUS && Math.abs(knot2.x) < ORIGIN_RADIUS);
        }
        return correct;
    }

    /**
     * check the correctness of user-plotted graphs against a pre-defined answer.
     *
     * @param trustedJSONString a JSON String which contains the correct answer
     * @param untrustedJSONString a JSON String which contains user's answer
     * @return a JSON string containing two field. 1. the test result; 2. the error if there is one.
     *      test result can be true of false
     *      error includes: wrongNumOfCurves, wrongShape, wrongPosition, wrongLabels.
     * @throws CheckerException it is thrown when information are missing in the JSON String, or the JSON string is not
     *      in the correct format. Also, it will be thrown if the information in JSON string is not valid.
     * @throws ParseException it is thrown when input JSON string cannot be parsed. It is thrown by the external library
     *      json.simple.
     */
    public static String test(final String trustedJSONString, final String untrustedJSONString)
                                                    throws CheckerException, ParseException {

        HashMap<String, Object> trustedData = Parser.parseInputJSONString(trustedJSONString);
        Curve[] trustedCurves = (Curve[]) trustedData.get("curves");

        HashMap<String, Object> untrustedData = Parser.parseInputJSONString(untrustedJSONString);
        Curve[] untrustedCurves = (Curve[]) untrustedData.get("curves");

        JSONObject jsonResult = new JSONObject();

        if (trustedCurves.length != untrustedCurves.length) {
            jsonResult.put("errCause", "You've drawn the wrong number of curves!");
            jsonResult.put("equal", false);
            return jsonResult.toJSONString();
        }

        // Test the shape of the curve
        for (int i = 0; i < trustedCurves.length; i++) {
            if (!testShape(trustedCurves[i].getPts(), untrustedCurves[i].getPts())) {
                jsonResult.put("errCause", "Your curve is the wrong shape!");
                jsonResult.put("equal", false);
                return jsonResult.toJSONString();
            }
        }

        // Test the position of knots
        for (int i = 0; i < trustedCurves.length; i++) {
            boolean correct = testPosition(trustedCurves[i].getPts(), untrustedCurves[i].getPts())
                    && testKnotsPosition(trustedCurves[i].getInterX(), untrustedCurves[i].getInterX())
                    && testKnotsPosition(trustedCurves[i].getInterY(), untrustedCurves[i].getInterY())
                    && testKnotsPosition(trustedCurves[i].getMaxima(), untrustedCurves[i].getMaxima())
                    && testKnotsPosition(trustedCurves[i].getMinima(), untrustedCurves[i].getMinima());
            if (!correct) {
                jsonResult.put("errCause", "Your curve is positioned incorrectly!");
                jsonResult.put("equal", false);
                return jsonResult.toJSONString();
            }
        }

        // Check that the labels are correctly positioned
        for (int i = 0; i < trustedCurves.length; i++) {
            boolean correct = testKnotsSymbols(trustedCurves[i].getInterX(), untrustedCurves[i].getInterX())
                    && testKnotsSymbols(trustedCurves[i].getInterY(), untrustedCurves[i].getInterY())
                    && testKnotsSymbols(trustedCurves[i].getMaxima(), untrustedCurves[i].getMaxima())
                    && testKnotsSymbols(trustedCurves[i].getMinima(), untrustedCurves[i].getMinima());
            if (!correct) {
                jsonResult.put("errCause", "Your labels are incorrectly placed!");
                jsonResult.put("equal", false);
                return jsonResult.toJSONString();
            }  
        }

        // If we make it to here, we have an exact match with the correct answer.
        jsonResult.put("errCause", "null");
        jsonResult.put("equal", true);
        return jsonResult.toJSONString();
    }


    public static void main(final String[] args) throws CheckerException, ParseException, IOException {

        String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/test.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/drawn.json");
        System.out.println(test(trustedJSONString, untrustedJSONString));

    }

}