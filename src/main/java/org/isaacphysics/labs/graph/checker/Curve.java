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

/**
 * Trusted and untrusted graph may include multiple curves. Each curve has fields:
 * pts: for points constituting the curve
 * interX: x intercepts
 * interY: y intercepts
 * maxima: maxima
 * minima: minima
 */
public class Curve implements Comparable<Curve> {

    private Point[] pts;
    private Knot[] interX;
    private Knot[] interY;
    private Knot[] maxima;
    private Knot[] minima;
    private int colorIdx;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    public Point[] getPts() {
        return pts;
    }

    public Knot[] getInterX() {
        return interX;
    }

    public Knot[] getInterY() {
        return interY;
    }

    public Knot[] getMaxima() {
        return maxima;
    }

    public Knot[] getMinima() {
        return minima;
    }

    public void setPts(final Point[] pts) {
        this.pts = pts;
    }

    public void setInterX(final Knot[] interX) {
        this.interX = interX;
    }

    public void setInterY(final Knot[] interY) {
        this.interY = interY;
    }

    public void setMaxima(final Knot[] maxima) {
        this.maxima = maxima;
    }

    public void setMinima(final Knot[] minima) {
        this.minima = minima;
    }

    public int getColorIdx() {
        return colorIdx;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setColorIdx(int colorIdx) {
        this.colorIdx = colorIdx;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    @Override
    public int compareTo(Curve o) {
        double m1 = this.getMinX();
        double m2 = o.getMinX();

        if (m1 < m2) {
            return -1;
        } else if (m1 == m2) {
            return 0;
        } else {
            return 1;
        }
    }
}
