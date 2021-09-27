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

import java.util.Objects;

public class Mutable {
    private String value;

    public Mutable(String value) {
        this.value = value;
    }

    public Mutable withSuffix(String suffix) {
        if (value == null) {
            value = suffix;
        } else {
            value += suffix;
        }
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Mutable && Objects.equals(value, ((Mutable) o).value);
    }

    @Override
    public String toString() {
        return value;
    }
}
