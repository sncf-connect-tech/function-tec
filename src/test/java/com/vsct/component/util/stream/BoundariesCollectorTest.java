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

import com.vsct.component.util.Boundaries;
import com.vsct.testing.data.Dbl;
import com.vsct.testing.data.Dist;
import com.vsct.testing.tool.VAssertions;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BoundariesCollectorTest {

    @Test
    void should_reduce() {
        // given
        final Dbl max = new Dbl(30.9);
        final Dbl min = new Dbl(3.5);
        final Stream<Dbl> elems = Stream.of(new Dist(max, "m"), new Dist(1.1, "mm"), new Dist(min, "m"), new Dist(17.0, "cm"))
                .parallel()
                .filter(d -> d.getDimension().equals("m"))
                .map(Dist::toVal);

        // when
        final Optional<Boundaries<Dbl>> optB = elems.collect(new BoundariesCollector<>(Comparator.comparingDouble(Dbl::getVal)));

        // then
        assertThat(optB).hasValueSatisfying(b -> VAssertions.assertBoundaries(b, min, max));
    }

}