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

import com.vsct.component.util.immucoll.AbstractImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * autre implémentation "basique", mais où l'élément admet la modification
 */
public class Mutables extends AbstractImmutableList<Mutable, Mutables> {

    public Mutables(List<Mutable> inner) {
        super(inner, Mutables::new);
    }

    public static Mutables of(String... rawValues) {
        return new Mutables(Arrays.stream(rawValues).map(Mutable::new).collect(toList()));
    }

    @Override
    public String toString() {
        return "mut" + super.toString();
    }

    public Stream<String> values() {
        return stream().map(Mutable::toString);
    }
}
