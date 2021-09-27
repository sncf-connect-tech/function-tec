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

import java.util.Locale;

/**
 * distance + azimut
 */
public class Heading extends Dist {
    final Direction dir;

    public Heading(double distance, final String dimension, Direction dir) {
        super(distance, dimension);
        this.dir = dir;
    }

    public Direction getDir() {
        return dir;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof Heading && super.equals(obj) && ((Heading) obj).dir.equals(dir);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "h(%s: %.1f%s)", dir, getVal(), getDimension());
    }
}
