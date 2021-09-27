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

import com.vsct.component.util.ctrl.Either;
import com.vsct.component.util.ctrl.Try;

/**
 * Encapsule 1 valeur décimale immuable
 */
public class Dbl {
    private final double val;

    public Dbl(final double val) {
        this.val = val;
    }

    public Either<MathFailure, Double> invert() {
        if (val == 0d) {
            return Either.left(MathFailure.DIV_BY_0);
        } else {
            final double inv = 1d / val;
            return Either.right(inv);
        }
    }

    public Try<Double> tryInvert() {
        if (val == 0d) {
            return Try.failure(new ArithmeticException("div by 0"));
        } else {
            final double inv = 1d / val;
            return Try.success(inv);
        }
    }

    public double getVal() {
        return val;
    }

    boolean sameVal(final Dbl other) {
        return Double.compare(other.val, val) == 0 || Math.abs(other.val - val) < 0.05;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this
                || obj instanceof Dbl && sameVal((Dbl) obj);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "d(%.3f)", val);
    }
}
