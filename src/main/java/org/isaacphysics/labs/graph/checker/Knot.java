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
 * Knot represents a special point, (among maximum, minimum and intercepts). xSymbol is the symbol of the knot
 * on the x-axis, and ySymbol is the symbol of knot on the y-axis.
 */
public class Knot extends Point {
    final Symbol symbol;
    final Symbol xSymbol;
    final Symbol ySymbol;

    public Knot(final double x, final double y, final Symbol symbol, final Symbol xSymbol, final Symbol ySymbol) {
        super(x, y);
        this.symbol = symbol;
        this.xSymbol = xSymbol;
        this.ySymbol = ySymbol;
    }

}
