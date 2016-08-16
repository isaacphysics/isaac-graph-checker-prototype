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
import java.util.Arrays;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;


/**
 * Check correctness of user's graph against the correct graph, and gives error found if there is one.
 */
public final class Checker {

    static final double NORM_DEGREE = 1.0;
    static final double ERR_TOLERANCE_POSITION = 0.5;
    static final double ERR_TOLERANCE_SHAPE = 0.05;
    static final double ERR_TOLERANCE_GRAD = 1;
    static final double ORIGIN_RADIUS = 0.025;
    static final double MAXIMUM_ERROR_TOLERANCE = 2.45;

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

//    /**
//     * estimate the difference (error) between two curves.
//     *
//     * @param pts1 points of one of the curve
//     * @param pts2 points of the other curve
//     * @return the estimated error
//     * @throws CheckerException thrown if the two curves have different number of points
//     */
//    private static double findError(final Point[] pts1, final Point[] pts2) throws CheckerException {
//        if (pts1.length != pts2.length) {
//            throw new CheckerException("Trusted curve and untrusted curve have different number of points");
//        }
//
//        int n = pts1.length;
//
//        double err1 = 0;
//        for (int i = 0; i < n; i++) {
//            err1 += Math.pow(Point.getDist(pts1[i], pts2[i]), NORM_DEGREE);
//        }
//        err1 = Math.pow(err1, 1.0 / NORM_DEGREE) / n;
//
//        double err2 = 0;
//        double max_error = 0;
//        for (int i = 0; i < n; i++) {
//            err2 += Math.pow(Point.getDist(pts1[(n - 1) - i], pts2[i]), NORM_DEGREE);
//            max_error = Math.max(Point.getDist(pts1[(n - 1) - i], pts2[i]), max_error);
//        }
//        err2 = Math.pow(err2, 1.0 / NORM_DEGREE) / n;
//
//        return Math.min(err1, err2);
//    }
//
//    /**
//     * estimate the maximum (error) between two curves.
//     *
//     * @param pts1 points of one of the curve
//     * @param pts2 points of the other curve
//     * @return the maximumestimated error
//     * @throws CheckerException thrown if the two curves have different number of points
//     */
//    private static double findMaxError(final Point[] pts1, final Point[] pts2) throws CheckerException {
//        if (pts1.length != pts2.length) {
//            throw new CheckerException("Trusted curve and untrusted curve have different number of points");
//        }
//        int n = pts1.length;
//        double max_error = 0;
//        for (int i = 0; i < n; i++) {
//            max_error = Math.max(Point.getDist(pts1[i], pts2[i]), max_error);
//        }
//        return max_error;
//    }


    private static double[] findGradient(final Point[] pts) {
        double[] grad = new double[pts.length - 1];
        for (int i = 0; i < grad.length; i++) {
            double dx = pts[i+1].x - pts[i].x;
            double dy = pts[i+1].y - pts[i].y;
            if (dy / dx > 10) {
                grad[i] = 10;
            } else if (dy / dx < -10) {
                grad[i] = -10;
            } else {
                grad[i] = dy / dx;
            }
        }
        return grad;
    }

//
//    private static double findGradError(final double[] trusted, final double[] untrusted) {
//        int n = trusted.length;
//
//        double err1 = 0;
//        for (int i = 0; i < n; i++) {
//            err1 += Math.pow(Math.abs(trusted[i] - untrusted[i]), NORM_DEGREE);
//        }
//        err1 = Math.pow(err1, 1.0 / NORM_DEGREE) / n;
//
//        double err2 = 0;
//        for (int i = 0; i < n; i++) {
//            err2 += Math.pow(Math.abs(trusted[n - i - 1] - untrusted[i]), NORM_DEGREE);
//        }
//        err2 = Math.pow(err2, 1.0 / NORM_DEGREE) / n;
//
//        return Math.min(err1, err2);
//    }

    private static double findError2(final Point[] trusted, final Point[] untrusted) throws CheckerException {
        int n = trusted.length;
        int m = untrusted.length;

        double[][] dtw = new double[n+1][m+1];
        for (int i = 1; i <= n; i++) {
            dtw[i][0] = 10000;
        }
        for (int j = 1; j <= m; j++) {
            dtw[0][j] = 10000;
        }
        dtw[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.pow(Point.getDist(trusted[i-1], untrusted[j-1]), NORM_DEGREE);
                dtw[i][j] = cost + Math.min(Math.min(dtw[i-1][j], dtw[i][j-1]), dtw[i-1][j-1]);
            }
        }
        double err1 = Math.pow(dtw[n][m], 1.0 / NORM_DEGREE) / n;

        for (int i = 1; i <= n; i++) {
            dtw[i][0] = 10000;
        }
        for (int j = 1; j <= m; j++) {
            dtw[0][j] = 10000;
        }
        dtw[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.pow(Point.getDist(trusted[i-1], untrusted[m-j]), NORM_DEGREE);
                dtw[i][j] = cost + Math.min(Math.min(dtw[i-1][j], dtw[i][j-1]), dtw[i-1][j-1]);
            }
        }
        double err2 = Math.pow(dtw[n][m], 1.0 / NORM_DEGREE) / n;

        return Math.min(err1, err2);
    }

    private static double findGradError2(final double[] trusted, final double[] untrusted) {

        int n = trusted.length;
        int m = untrusted.length;

        double[][] dtw = new double[n+1][m+1];
        for (int i = 1; i <= n; i++) {
            dtw[i][0] = 10000;
        }
        for (int j = 1; j <= m; j++) {
            dtw[0][j] = 10000;
        }
        dtw[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.pow(Math.abs(trusted[i-1] - untrusted[j-1]), NORM_DEGREE);
                dtw[i][j] = cost + Math.min(Math.min(dtw[i-1][j], dtw[i][j-1]), dtw[i-1][j-1]);
            }
        }
        double err1 = Math.pow(dtw[n][m], 1.0 / NORM_DEGREE) / n;

        for (int i = 1; i <= n; i++) {
            dtw[i][0] = 10000;
        }
        for (int j = 1; j <= m; j++) {
            dtw[0][j] = 10000;
        }
        dtw[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.pow(Math.abs(trusted[i-1] - untrusted[m-j]), NORM_DEGREE);
                dtw[i][j] = cost + Math.min(Math.min(dtw[i-1][j], dtw[i][j-1]), dtw[i-1][j-1]);
            }
        }
        double err2 = Math.pow(dtw[n][m], 1.0 / NORM_DEGREE) / n;

        return Math.min(err1, err2);
    }

    //    private static int findNumInflextions(Point[] pts) {
//        Point[] grad = new Point[pts.length - 1];
//        for (int i = 0; i < grad.length; i++) {
//            double dx = pts[i+1].x - pts[i].x;
//            double dy = pts[i+1].y - pts[i].y;
//            grad[i] = new Point(pts[i].x, dy/dx);
//        }
//
//        Point[] diff = new Point[grad.length - 1];
//        for (int i = 0; i < diff.length; i++) {
//            double dx = grad[i+1].x - grad[i].x;
//            double dy = grad[i+1].y - grad[i].y;
//            diff[i] = new Point(grad[i].x, dy/dx);
//        }
//
//        int count = 0;
//        for (int i = 1; i < diff.length; i++) {
//            if (diff[i-1].y * diff[i].y < 0) {
//                System.out.println(diff[i].x);
//                count++;
//            }
//        }
//
//        System.out.println();
//        return count;
//    }

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
//        if (trustedKnots.length != untrustedKnots.length) {
//            return false;
//        }

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
//        if (trustedKnots.length != untrustedKnots.length) {
//            return false;
//        }

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
     * Test the shape of user's curve against the corresponding curve in the answer.
     *
     * @param trustedCurves curves in the answer
     * @param untrustedCurves corresponding curves of user
     * @return true if two curves are at similar shape, false otherwise
     * @throws CheckerException thrown when two curves have different number of points
     */
    public static boolean testShape(final Curve[] trustedCurves, final Curve[] untrustedCurves) throws CheckerException {
        for (int i = 0; i < trustedCurves.length; i++) {
            int degree = trustedCurves[i].getMaxima().length + untrustedCurves[i].getMinima().length;
            double shapeTolerance, gradTolerance;

            if (degree <= 1) {
                shapeTolerance = 0.05;
                gradTolerance = 1;
            } else {
                shapeTolerance = 0.1;
                gradTolerance = 1.5;
            }

            Point[] trustedPts = trustedCurves[i].getPts();
            Point[] untrustedPts = untrustedCurves[i].getPts();
            Point[] tmp;

            double err = findError2(normaliseShape(trustedPts), normaliseShape(untrustedPts));
            double errGrad = findGradError2(findGradient(trustedPts), findGradient(untrustedPts));
            System.out.println("err: " + err);
            System.out.println(errGrad);
            if (err < shapeTolerance && errGrad < gradTolerance) {
                continue;
            }

            tmp = Arrays.copyOfRange(untrustedPts, 10, untrustedPts.length);
            double err1 = findError2(normaliseShape(trustedPts), normaliseShape(tmp));
            double errGrad1 = findGradError2(findGradient(trustedPts), findGradient(tmp));
            System.out.println("err1: " + err1);
            System.out.println(errGrad1);
            if (err1 < shapeTolerance && errGrad1 < gradTolerance) {
                continue;
            }

            tmp = Arrays.copyOfRange(untrustedPts, 0, untrustedPts.length - 10);
            double err2 = findError2(normaliseShape(trustedPts), normaliseShape(tmp));
            double errGrad2 = findGradError2(findGradient(trustedPts), findGradient(tmp));
            System.out.println("err2: " + err2);
            System.out.println(errGrad2);
            if (err2 < shapeTolerance && errGrad2 < gradTolerance) {
                continue;
            }

            tmp = Arrays.copyOfRange(untrustedPts, 5, untrustedPts.length - 5);
            double err3 = findError2(normaliseShape(trustedPts), normaliseShape(tmp));
            double errGrad3 = findGradError2(findGradient(trustedPts), findGradient(tmp));
            System.out.println("err3: " + err3);
            System.out.println(errGrad3);
            if (err3 < shapeTolerance && errGrad3 < gradTolerance) {
                continue;
            }

            // if we make it here
            return false;
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
    public static boolean testPosition(final Curve[] trustedCurves, final Curve[] untrustedCurves) throws CheckerException {
        for (int i = 0; i < trustedCurves.length; i++) {
            double errPositionDtw = findError2(normalisePosition(trustedCurves[i].getPts()), normalisePosition(untrustedCurves[i].getPts()));
            System.out.println("errPositionDtw: " + errPositionDtw);
//            double maxErrPosition = findMaxError(normalisePosition(trustedPts), normalisePosition(untrustedPts));

            boolean correct = (errPositionDtw < ERR_TOLERANCE_POSITION)
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
    private static boolean testSymbols(Curve[] trustedCurves, Curve[] untrustedCurves) {
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

//    private static boolean getTestStrictness(Curve[] curves) {
//
//    }


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


        /*
         Test: Number of curves
         Test: Number of intercepts
         Test: Number of turning Pts
         Test: Shape of curve
                - total error
                - max error (maybe)
         Test: Position
                - total error
                - individual error (maybe)
                - test positions of intercepts
         Test: Labels
        */


        // make sure two graphs have same number of curves
        if (trustedCurves.length != untrustedCurves.length) {
            jsonResult.put("errCause", "You've drawn the wrong number of curves!");
            jsonResult.put("equal", false);
            return jsonResult.toJSONString();
        }

        // make sure each curve has right number of x,y intercepts.
        for (int i = 0; i < trustedCurves.length; i++) {
            boolean correct = (trustedCurves[i].getInterX().length == untrustedCurves[i].getInterX().length)
                    && (trustedCurves[i].getInterX().length == untrustedCurves[i].getInterX().length);
            if (!correct) {
                jsonResult.put("errCause", "One of the curve contains wrong number of intercepts!");
                jsonResult.put("equal", false);
                return jsonResult.toJSONString();
            }
        }

        // make sure each curve has right number of turning pts
        for (int i = 0; i < trustedCurves.length; i++) {
            boolean correct = (trustedCurves[i].getMaxima().length == untrustedCurves[i].getMaxima().length)
                    && (trustedCurves[i].getMinima().length == untrustedCurves[i].getMinima().length);
            if (!correct) {
                jsonResult.put("errCause", "One of the curve contains wrong number of turning points.");
                jsonResult.put("equal", false);
                return jsonResult.toJSONString();
            }
        }

        // Test the shape of the curve
        if (!testShape(trustedCurves, untrustedCurves)) {
            jsonResult.put("errCause", "Your curve is the wrong shape!");
            jsonResult.put("equal", false);
            return jsonResult.toJSONString();
        }

        // Test the position of knots
        if (!testPosition(trustedCurves, untrustedCurves)) {
            jsonResult.put("errCause", "Your curve is positioned incorrectly!");
            jsonResult.put("equal", false);
            return jsonResult.toJSONString();
        }

        // Check that the labels are correctly positioned
        if (!testSymbols(trustedCurves, untrustedCurves)) {
            jsonResult.put("errCause", "Your labels are incorrectly placed!");
            jsonResult.put("equal", false);
            return jsonResult.toJSONString();
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