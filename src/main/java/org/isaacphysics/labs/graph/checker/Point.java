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
 * Point represents a single point in the canvas.
 */
public class Point {
	final double x;
	final double y;


	public Point(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

    /**
     * claculate the distance between two points.
     *
     * @param pt1 point 1
     * @param pt2 point 2
     * @return the distance between two points calculated by sqrt((dx)^2 + (dy)^2)
     */
	public static double getDist(final Point pt1, final Point pt2) {
		return Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
	}

}
