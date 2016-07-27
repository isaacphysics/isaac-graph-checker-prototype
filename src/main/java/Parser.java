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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Parser {


    public static HashMap<String, Object> parseInputJSONString(String JSONString) throws CheckerException, ParseException {
        JSONParser parser = new JSONParser();

        try {
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
                        if (text == null) throw new CheckerException("Invalid JSON: key information missing");
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
                        if (text == null) throw new CheckerException("Invalid JSON: key information missing");
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
                        if (text == null) throw new CheckerException("Invalid JSON: key information missing");
                        symbol = new Symbol(sx, sy, text);
                    }

                    Symbol xSymbol = null;
                    if (jsonKnot.get("xSymbol") != null) {
                        JSONObject jsonSymbol = (JSONObject) jsonKnot.get("xSymbol");
                        Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                        Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                        String text = (String) jsonSymbol.get("text");
                        if (text == null) throw new CheckerException("Invalid JSON: key information missing");
                        xSymbol = new Symbol(sx, sy, text);
                    }

                    Symbol ySymbol = null;
                    if (jsonKnot.get("ySymbol") != null) {
                        JSONObject jsonSymbol = (JSONObject) jsonKnot.get("ySymbol");
                        Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                        Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                        String text = (String) jsonSymbol.get("text");
                        if (text == null) throw new CheckerException("Invalid JSON: key information missing");
                        ySymbol = new Symbol(sx, sy, text);
                    }

                    Knot knot = new Knot(x, y, symbol, xSymbol, ySymbol);
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
                        if (text == null) throw new CheckerException("Invalid JSON: key information missing");
                        symbol = new Symbol(sx, sy, text);
                    }

                    Symbol xSymbol = null;
                    if (jsonKnot.get("xSymbol") != null) {
                        JSONObject jsonSymbol = (JSONObject) jsonKnot.get("xSymbol");
                        Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                        Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                        String text = (String) jsonSymbol.get("text");
                        if (text == null) throw new CheckerException("Invalid JSON: key information missing");
                        xSymbol = new Symbol(sx, sy, text);
                    }

                    Symbol ySymbol = null;
                    if (jsonKnot.get("ySymbol") != null) {
                        JSONObject jsonSymbol = (JSONObject) jsonKnot.get("ySymbol");
                        Double sx = ((Number) jsonSymbol.get("x")).doubleValue();
                        Double sy = ((Number) jsonSymbol.get("y")).doubleValue();
                        String text = (String) jsonSymbol.get("text");
                        if (text == null) throw new CheckerException("Invalid JSON: key information missing");
                        ySymbol = new Symbol(sx, sy, text);
                    }


                    Knot knot = new Knot(x, y, symbol, xSymbol, ySymbol);
                    minima[j] = knot;
                }
                curve.setMinima(minima);

                curves[i] = curve;
            }
            data.put("curves", curves);

            return data;

        } catch (NullPointerException npExn) {
            throw new CheckerException("Invalid JSON: key information missing");
        } catch (ClassCastException ccExn) {
            throw new CheckerException("Invalid JSON: incorrect format");
        }
    }


    public static boolean getIsCorrect(String JSONString) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(JSONString);
        JSONObject jsonResult = (JSONObject) obj;
        boolean correct = (Boolean) jsonResult.get("isCorrect");
        return correct;
    }

    public static String getErrCause(String JSONString) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(JSONString);
        JSONObject jsonResult = (JSONObject) obj;
        String errCause = (String) jsonResult.get("errCause");
        return errCause;
    }

    public static void main(String[] args) {
//        ArrayList<Integer> arr = new ArrayList<Integer>();
//        arr.add(3);
//        arr.add(1);
//
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("abc", "123");
//        map.put("arr", arr);
//        JSONObject json = new JSONObject(map);

    }

}
