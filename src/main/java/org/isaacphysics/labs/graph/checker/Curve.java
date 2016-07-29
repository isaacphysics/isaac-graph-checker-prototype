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

public class Curve {

    private Point[] pts;
    private Knot[] interX;
    private Knot[] interY;
    private Knot[] maxima;
    private Knot[] minima;

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

    public void setPts(Point[] pts) {
        this.pts = pts;
    }

    public void setInterX(Knot[] interX) {
        this.interX = interX;
    }

    public void setInterY(Knot[] interY) {
        this.interY = interY;
    }

    public void setMaxima(Knot[] maxima) {
        this.maxima = maxima;
    }

    public void setMinima(Knot[] minima) {
        this.minima = minima;
    }
}