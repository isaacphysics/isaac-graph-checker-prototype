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

import java.util.ArrayList;
import java.util.HashMap;

public class Parser {


    public static HashMap<String, Object> parseInputJSONString(String JSONString) throws CheckerException, ParseException {
        JSONParser parser = new JSONParser();

//        try {
            Object obj = parser.parse(JSONString);
            JSONObject jsonData = (JSONObject) obj;

            HashMap<String, Object> data = new HashMap<String, Object>();

            double canvasWidth = ((Number) jsonData.get("canvasWidth")).doubleValue();
            if (canvasWidth < 0 || canvasWidth > 5000)
                throw new CheckerException("Invalid canvasWidth");
            data.put("canvasWidth", canvasWidth);

            double canvasHeight = ((Number) jsonData.get("canvasHeight")).doubleValue();
            if (canvasHeight < 0 || canvasHeight > 5000)
                throw new CheckerException("Invalid canvasHeight");
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

                JSONArray jsonInterX = (JSONArray) jsonCurve.get("interX");
                Knot[] interX = new Knot[jsonInterX.size()];
                for (int j = 0; j < jsonInterX.size(); j++) {
                    JSONObject jsonKnot = (JSONObject) jsonInterX.get(j);
                    Double x = ((Number) jsonKnot.get("x")).doubleValue();
                    Double y = ((Number) jsonKnot.get("y")).doubleValue();

                    Symbol symbol = null;
                    if (jsonKnot.get("symbol") != null) {
                        JSONObject jsonSymbol = (JSONObject) jsonKnot.get("symbol");
                        Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                        Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                        String text = (String) jsonSymbol.get("text");
                        symbol = new Symbol(sx, sy, text);
                    }

                    Knot knot = new Knot(x,y,symbol);
                    interX[j] = knot;
                }
                curve.setInterX(interX);

                JSONArray jsonInterY = (JSONArray) jsonCurve.get("interY");
                Knot[] interY = new Knot[jsonInterY.size()];
                for (int j = 0; j < jsonInterY.size(); j++) {
                    JSONObject jsonKnot = (JSONObject) jsonInterY.get(j);
                    Double x = ((Number) jsonKnot.get("x")).doubleValue();
                    Double y = ((Number) jsonKnot.get("y")).doubleValue();

                    Symbol symbol = null;
                    if (jsonKnot.get("symbol") != null) {
                        JSONObject jsonSymbol = (JSONObject) jsonKnot.get("symbol");
                        Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                        Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                        String text = (String) jsonSymbol.get("text");
                        symbol = new Symbol(sx, sy, text);
                    }

                    Knot knot = new Knot(x, y, symbol);
                    interY[j] = knot;
                }
                curve.setInterY(interY);

                JSONArray jsonMaxima = (JSONArray) jsonCurve.get("maxima");
                Knot[] maxima = new Knot[jsonMaxima.size()];
                for (int j = 0; j < jsonMaxima.size(); j++) {
                    JSONObject jsonKnot = (JSONObject) jsonMaxima.get(j);
                    Double x = ((Number) jsonKnot.get("x")).doubleValue();
                    Double y = ((Number) jsonKnot.get("y")).doubleValue();

                    Symbol symbol = null;
                    if (jsonKnot.get("symbol") != null) {
                        JSONObject jsonSymbol = (JSONObject) jsonKnot.get("symbol");
                        Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                        Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                        String text = (String) jsonSymbol.get("text");
                        symbol = new Symbol(sx, sy, text);
                    }

                    Knot knot = new Knot(x, y, symbol);
                    maxima[j] = knot;
                }
                curve.setMaxima(maxima);

                JSONArray jsonMinima = (JSONArray) jsonCurve.get("minima");
                Knot[] minima = new Knot[jsonMinima.size()];
                for (int j = 0; j < jsonMinima.size(); j++) {
                    JSONObject jsonKnot = (JSONObject) jsonMinima.get(j);
                    Double x = ((Number) jsonKnot.get("x")).doubleValue();
                    Double y = ((Number) jsonKnot.get("y")).doubleValue();

                    Symbol symbol = null;
                    if (jsonKnot.get("symbol") != null) {
                        JSONObject jsonSymbol = (JSONObject) jsonKnot.get("symbol");
                        Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                        Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                        String text = (String) jsonSymbol.get("text");
                        symbol = new Symbol(sx, sy, text);
                    }

                    Knot knot = new Knot(x, y, symbol);
                    minima[j] = knot;
                }
                curve.setMinima(minima);

                curves[i] = curve;
            }
            data.put("curves", curves);

            return data;

//        } catch (NullPointerException npExn) {
//            throw new CheckerException("Invalid JSON: key information missing");
//        }
    }

    public static HashMap<String, Object> parseResultJSONString(String JSONString) throws ParseException {

        JSONParser parser = new JSONParser();
        HashMap<String, Object> result = new HashMap<String, Object>();

        Object obj = parser.parse(JSONString);
        JSONObject jsonResult = (JSONObject) obj;

        result.put("descriptor", jsonResult.get("descriptor"));
        result.put("isCorrect", jsonResult.get("isCorrect"));
        result.put("test_symbols", jsonResult.get("test_symbols"));
        result.put("test_number of segments", jsonResult.get("test_number of segments"));

        JSONArray jsonSegmentsResults = (JSONArray) jsonResult.get("segmentsResults");
        Object[] segmentsResults = new Object[jsonSegmentsResults.size()];
        for (int i = 0; i < jsonSegmentsResults.size(); i++) {
            JSONObject jsonSegmentResult = (JSONObject) jsonSegmentsResults.get(i);
            HashMap<String, Object> segmentResult = new HashMap<String, Object>();

            segmentResult.put("test_position", jsonSegmentResult.get("test_position"));
            segmentResult.put("test_shape", jsonSegmentResult.get("test_shape"));
            segmentResult.put("test_maxima", jsonSegmentResult.get("test_maxima"));
            segmentResult.put("test_minima", jsonSegmentResult.get("test_minima"));
            segmentResult.put("errPosition", jsonSegmentResult.get("errPosition"));
            segmentResult.put("errShape", jsonSegmentResult.get("errShape"));

            segmentsResults[i] = jsonSegmentResult;
        }

        result.put("segmentsResults", segmentsResults);

        return result;
    }

    public static boolean getIsCorrect(String JSONString) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(JSONString);
        JSONObject jsonResult = (JSONObject) obj;
        boolean isCorrect = (Boolean) jsonResult.get("isCorrect");
        return isCorrect;
    }


}
