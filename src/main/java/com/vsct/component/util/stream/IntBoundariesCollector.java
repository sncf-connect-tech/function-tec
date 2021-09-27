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

package com.vsct.component.util.stream;

import com.vsct.component.util.IntBoundaries;
import com.vsct.component.util.IntBoundaries.Mutable;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Collections.emptySet;

class IntBoundariesCollector implements Collector<Integer, Mutable, Optional<IntBoundaries>> {

    private static final Set<Characteristics> CH_NONE = emptySet();

    @Override
    public BiConsumer<Mutable, Integer> accumulator() {
        return Mutable::update;
    }

    @Override
    public Set<Collector.Characteristics> characteristics() {
        return CH_NONE;
    }

    @Override
    public BinaryOperator<Mutable> combiner() {
        return Mutable::merge;
    }

    @Override
    public Function<Mutable, Optional<IntBoundaries>> finisher() {
        return Mutable::finish;
    }

    @Override
    public Supplier<Mutable> supplier() {
        return Mutable::new;
    }

}
