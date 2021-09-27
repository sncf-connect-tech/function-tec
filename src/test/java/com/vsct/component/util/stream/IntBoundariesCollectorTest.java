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
import com.vsct.testing.data.Dist;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static com.vsct.testing.tool.VAssertions.assertIntBoundaries;
import static org.assertj.core.api.Assertions.assertThat;

class IntBoundariesCollectorTest {

    @Test
    void should_reduce() {
        // given
        final Stream<Integer> elems = Stream.of(30, 1, 3, 17).parallel();

        // when
        final Optional<IntBoundaries> optB = elems.collect(new IntBoundariesCollector());

        // then
        assertThat(optB).hasValueSatisfying(b -> assertIntBoundaries(b, 1, 30));
    }

    @Test
    void should_return_empty_on_empty_Stream() {
        // given
        final Stream<Integer> elems = Stream.of(new Dist(30.9, "m"), new Dist(3.5, "m"), new Dist(17.0, "cm"))
                .filter(d -> d.getDimension().equals("km"))
                .map(d -> BigDecimal.valueOf(d.getVal()).intValue());

        // when
        final Optional<IntBoundaries> optB = elems.collect(new IntBoundariesCollector());

        // then
        assertThat(optB).isEmpty();
    }
}