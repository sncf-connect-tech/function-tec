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
import com.vsct.component.util.IntBoundaries;
import com.vsct.testing.data.Dbl;
import com.vsct.testing.data.Dist;
import com.vsct.testing.data.RefAndValues;
import com.vsct.testing.tool.VAssertions;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.Comparator.comparingDouble;
import static org.assertj.core.api.Assertions.assertThat;

public class VCollectorsTest {

    @Test
    void groupingByAndFlatten_should_merge_Set_when_duplicate_key() {
        final RefAndValues<Set<String>> a1 = RefAndValues.uniq("a", false).with("y").with("5");
        final RefAndValues<Set<String>> a2 = RefAndValues.uniq("a", false).with("r").with("3").with("5").with("y");
        final RefAndValues<Set<String>> b1 = RefAndValues.uniq("b", true).with("c").with("p");

        final Map<String, Set<String>> values = Stream.of(a1, b1, a2)
                // conteneur de fusion trié, pour faciliter le test
                .collect(VCollectors.groupingByAndFlatten(RefAndValues::getRef, RefAndValues::getValues, TreeSet::new));

        assertThat(values.get("a")).as("merged")
                .isInstanceOf(TreeSet.class)
                .containsExactly("3", "5", "r", "y");
        assertThat(values.get("b")).as("no merge necessary").isSameAs(b1.getValues());
    }

    @Test
    void groupingByAndFlatten_should_merge_list_when_duplicate_key() {
        final RefAndValues<List<String>> a1 = RefAndValues.array("a").with("y").with("5");
        final RefAndValues<List<String>> a2 = RefAndValues.array("a").with("r").with("3").with("5");
        final RefAndValues<List<String>> a3 = RefAndValues.array("a").with("1").with("z");
        final RefAndValues<List<String>> b1 = RefAndValues.array("b").with("c").with("p");

        final Map<String, List<String>> values = Stream.of(a1, b1, a2, a3)
                .collect(VCollectors.groupingByAndFlatten(RefAndValues::getRef, RefAndValues::getValues, LinkedList::new));

        assertThat(values.get("a")).as("merged")
                .isInstanceOf(LinkedList.class)
                .containsExactly("y", "5", "r", "3", "5", "1", "z");
        assertThat(values.get("b")).as("no merge necessary").isSameAs(b1.getValues());
    }

    @Test
    void toBoundaries_should_return_empty_on_empty_Stream() {
        // given
        final Stream<Dist> elems = Stream.of(new Dist(30.9, "m"), new Dist(3.5, "m"), new Dist(17.0, "cm"))
                .parallel()
                .filter(d -> d.getDimension().equals("km"));

        // when
        final Optional<Boundaries<Dbl>> optB = elems.collect(VCollectors.toBoundaries(comparingDouble(Dbl::getVal)));

        // then
        assertThat(optB).isEmpty();
    }

    @Test
    void toBoundaries_should_reduce_on_natural_order() {
        // given
        final Stream<String> elems = Stream.of("b0", "e2", "d4", "0a", "31", "e0").parallel();

        // when
        final Optional<Boundaries<String>> optB = elems.collect(VCollectors.toBoundaries());

        // then
        assertThat(optB).hasValueSatisfying(b -> VAssertions.assertBoundaries(b, "0a", "e2"));
    }

    @Test
    void toBoundaries_should_return_empty_when_null_values() {
        // given
        final Stream<String> elems = Stream.of(null, null);

        // when
        final Optional<Boundaries<String>> optB = elems.collect(VCollectors.toBoundaries());

        // then
        assertThat(optB).isEmpty();
    }

    @Test
    void toIntBoundaries_should_return_empty_when_null_values() {
        // given
        final Stream<Integer> elems = Stream.of(null, null);

        // when
        final Optional<IntBoundaries> optB = elems.collect(VCollectors.toIntBoundaries());

        // then
        assertThat(optB).isEmpty();
    }

}
