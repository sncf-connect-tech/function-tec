/*
 *  Copyright (C) 2021 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vsct.testing.data;

import static java.util.Locale.ENGLISH;

public class Dist extends Dbl {
    private final String dimension;

    public Dist(final double val, final String dimension) {
        super(val);
        this.dimension = dimension;
    }

    public Dist(final Dbl val, final String dimension) {
        super(val.getVal());
        this.dimension = dimension;
    }

    public String getDimension() {
        return dimension;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (obj instanceof Dist) {
            Dist other = (Dist) obj;
            return sameVal(other) && other.dimension.equals(dimension);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format(ENGLISH, "dist(%.2f%s)", getVal(), dimension);
    }

    public Dbl toVal() {
        return new Dbl(getVal());
    }
}
