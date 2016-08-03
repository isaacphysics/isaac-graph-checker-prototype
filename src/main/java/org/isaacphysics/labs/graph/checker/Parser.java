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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;


/**
 * parse JSON string, either input or output.
 */
public final class Parser {

    static final double MAX_CANVAS_DIMENTION = 5000;

    /**
     * utility classes should not have a public or default constructor.
     */
    private Parser() {
        //
    }

    /**
     * parse information of a knot from the corresponding JSON object.
     * @param jsonKnots the JSON object that contains information of a set of knots
     * @return an instance Knot
     * @throws CheckerException thrown if some information is missing in the JSON object.
     */
    private static Knot[] parseJSONKnots(final JSONArray jsonKnots) throws CheckerException {

        Knot[] knots = new Knot[jsonKnots.size()];
        for (int j = 0; j < jsonKnots.size(); j++) {
            JSONObject jsonKnot = (JSONObject) jsonKnots.get(j);

            Double x = ((Number) jsonKnot.get("x")).doubleValue();
            Double y = ((Number) jsonKnot.get("y")).doubleValue();

            Symbol symbol = null;
            if (jsonKnot.get("symbol") != null) {
                JSONObject jsonSymbol = (JSONObject) jsonKnot.get("symbol");
                Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                String text = (String) jsonSymbol.get("text");
                if (text == null) {
                    throw new CheckerException("Invalid JSON: key information missing");
                }
                symbol = new Symbol(sx, sy, text);
            }

            Symbol xSymbol = null;
            if (jsonKnot.get("xSymbol") != null) {
                JSONObject jsonSymbol = (JSONObject) jsonKnot.get("xSymbol");
                Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                String text = (String) jsonSymbol.get("text");
                if (text == null) {
                    throw new CheckerException("Invalid JSON: key information missing");
                }
                xSymbol = new Symbol(sx, sy, text);
            }

            Symbol ySymbol = null;
            if (jsonKnot.get("ySymbol") != null) {
                JSONObject jsonSymbol = (JSONObject) jsonKnot.get("ySymbol");
                Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                String text = (String) jsonSymbol.get("text");
                if (text == null) {
                    throw new CheckerException("Invalid JSON: key information missing");
                }
                ySymbol = new Symbol(sx, sy, text);
            }

            Knot knot = new Knot(x, y, symbol, xSymbol, ySymbol);
            knots[j] = knot;
        }

        return knots;
    }

    /**
     * parse the input JSON string into a HashMap of reasonable classes.
     *
     * @param jsonString the input json string
     * @return a hash map contains exactly the same information as in the JSON string
     * @throws CheckerException it is thrown when information are missing in the JSON String, or the JSON string is not
     *      in the correct format. Also, it will be thrown if the information in JSON string is not valid.
     * @throws ParseException if the JSON string can not be parsed by json.simple.JSONParser
     */
    public static HashMap<String, Object> parseInputJSONString(final String jsonString)
                                                    throws CheckerException, ParseException {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonData = (JSONObject) obj;

            HashMap<String, Object> data = new HashMap<String, Object>();

            double canvasWidth = ((Number) jsonData.get("canvasWidth")).doubleValue();
            if (canvasWidth < 0 || canvasWidth > MAX_CANVAS_DIMENTION) {
                throw new CheckerException("Invalid canvasWidth");
            }
            data.put("canvasWidth", canvasWidth);

            double canvasHeight = ((Number) jsonData.get("canvasHeight")).doubleValue();
            if (canvasHeight < 0 || canvasHeight > MAX_CANVAS_DIMENTION) {
                throw new CheckerException("Invalid canvasHeight");
            }
            data.put("canvasHeight", canvasHeight);

            String descriptor = "";
            if (jsonData.get("descriptor") != null) {
                descriptor = (String) (jsonData.get("descriptor"));
            }
            data.put("descriptor", descriptor);


            JSONArray jsonCurves = (JSONArray) jsonData.get("curves");
            Curve[] curves = new Curve[jsonCurves.size()];
            for (int i = 0; i < jsonCurves.size(); i++) {
                JSONObject jsonCurve = (JSONObject) jsonCurves.get(i);
                Curve curve = new Curve();

                JSONArray jsonPts = (JSONArray) jsonCurve.get("pts");
                Point[] pts = new Point[jsonPts.size()];
                for (int j = 0; j < jsonPts.size(); j++) {
                    JSONObject jsonPoint = (JSONObject) jsonPts.get(j);
                    Double x = ((Number) jsonPoint.get("x")).doubleValue();
                    Double y = ((Number) jsonPoint.get("y")).doubleValue();
                    pts[j] = new Point(x, y);
                }
                curve.setPts(pts);

                Knot[] interX = parseJSONKnots((JSONArray) jsonCurve.get("interX"));
                curve.setInterX(interX);

                Knot[] interY = parseJSONKnots((JSONArray) jsonCurve.get("interY"));
                curve.setInterY(interY);

                Knot[] maxima = parseJSONKnots((JSONArray) jsonCurve.get("maxima"));
                curve.setMaxima(maxima);

                Knot[] minima = parseJSONKnots((JSONArray) jsonCurve.get("minima"));
                curve.setMinima(minima);

                curves[i] = curve;
            }
            data.put("curves", curves);

            return data;

        } catch (NullPointerException|ClassCastException npExn) {
            throw new CheckerException("Invalid JSON: key information missing");
        }
    }

    /**
     * retrieves the test result from result string.
     *
     * @param jsonString the result json string
     * @return test result (either true or false)
     * @throws ParseException if json.simple.JSONParser can not parse the string
     */
    public static boolean getIsCorrect(final String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(jsonString);
        JSONObject jsonResult = (JSONObject) obj;
        boolean correct = (Boolean) jsonResult.get("isCorrect");
        return correct;
    }


    /**
     * retrieves the error found from result string.
     *
     * @param jsonString the result json string
     * @return error found (either true or false)
     * @throws ParseException if json.simple.JSONParser can not parse the string
     */
    public static String getErrCause(final String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(jsonString);
        JSONObject jsonResult = (JSONObject) obj;
        String errCause = (String) jsonResult.get("errCause");
        return errCause;
    }



}
