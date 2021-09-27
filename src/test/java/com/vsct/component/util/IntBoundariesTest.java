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

package com.vsct.component.util;

import com.vsct.testing.data.Direction;
import com.vsct.testing.data.Dist;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.vsct.testing.tool.VAssertions.assertIntBoundaries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class IntBoundariesTest {

    // ======================================================================================
    //
    // Stream#reduce standard behavior
    //
    // ======================================================================================

    @Test
    void identity_must_be_transparent_in_combiner() {
        // given
        final BinaryOperator<IntBoundaries> combiner = IntBoundaries::merge;
        final IntBoundaries identity = IntBoundaries.empty();
        final IntBoundaries b = IntBoundaries.single(2);

        // when
        final IntBoundaries actual = combiner.apply(identity, b);

        // then
        assertThat(actual).isEqualTo(b);
    }

    @Test
    void combiner_must_be_compatible_with_accumulator() {
        // given
        final BiFunction<IntBoundaries, Integer, IntBoundaries> accumulator = IntBoundaries::accumulate;
        final BinaryOperator<IntBoundaries> combiner = IntBoundaries::merge;
        final IntBoundaries identity = IntBoundaries.empty();
        final int t = 0;
        final IntBoundaries u = IntBoundaries.single(2);

        // when
        final IntBoundaries identityAccumulated = combiner.apply(u, accumulator.apply(identity, t));
        final IntBoundaries direct = accumulator.apply(u, t);

        // then
        assertThat(identityAccumulated).isEqualTo(direct);
    }

    // ======================================================================================
    //
    // specific behavior
    //
    // ======================================================================================

    @Test
    void accumulateObj_should_accept_null() {
        // given
        final IntBoundaries x = IntBoundaries.single(1);

        // when
        final IntBoundaries a = x.accumulateObj(null);

        // then
        assertThat(a).as("expected no-op").isSameAs(x);
    }

    @Test
    void accumulate_should_limit_new() {
        // given
        final IntBoundaries x = IntBoundaries.single(9).accumulate(2);

        // when
        final IntBoundaries a = x.accumulate(4);

        // then
        assertThat(a).as("unneeded instance").isSameAs(x);
        assertIntBoundaries(a, 2, 9);
    }

    @Test
    void empty_is_single() {
        // given
        IntBoundaries x = IntBoundaries.empty();

        // when + then
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(x.isEmpty()).as("expected empty").isTrue();
        softly.assertThat(x.isSingle()).as("expected single").isTrue();
        softly.assertThat(x.isRange()).as("unexpected range").isFalse();
        softly.assertAll();
    }

    @Test
    void map_should_keep_empty() {
        // given
        final IntBoundaries x = IntBoundaries.empty();

        // when
        final IntBoundaries mapped = x.map(any -> 4);

        // then
        assertThat(mapped).as("unneeded instance").isSameAs(x);
    }

    @Test
    void map_should_reverse_when_order_requires() {
        // given
        final IntBoundaries base = IntBoundaries.single(2).accumulate(5);

        // when
        final IntBoundaries mapped = base.map(x -> -2 * x);

        // then
        assertIntBoundaries(mapped, -10, -4);
    }

    @Test
    void mapToObj_should_keep_empty() {
        // given
        final IntBoundaries x = IntBoundaries.empty();

        // when
        final Boundaries<String> mapped = x.mapToObj(any -> "foo", Comparator.comparing(String::length));

        // then
        assertThat(mapped.isEmpty()).isTrue();
    }

    @Test
    void mapToObj_should_reverse_when_comparator_requires() {
        // given
        final IntBoundaries base = IntBoundaries.single(1).accumulate(0);
        final Comparator<Direction> byName = Comparator.comparing(Direction::name);

        // when
        final Boundaries<Direction> mapped = base.mapToObj(x -> Direction.values()[x], byName);

        // then
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(mapped.getMin()).as("min").isEqualTo(Direction.E);
        softly.assertThat(mapped.getMax()).as("max").isEqualTo(Direction.N);
        softly.assertAll();
    }

    @Test
    void mapToNaturalOrderObj_should_handle_single() {
        // given
        final IntBoundaries base = IntBoundaries.single(0);

        // when
        final Boundaries<BigDecimal> mapped = base.mapToNaturalOrderObj(BigDecimal::new);

        // then
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(mapped.getMin()).as("val").isEqualTo(BigDecimal.ZERO);
        softly.assertThat(mapped.isSingle()).as("single").isTrue();
        softly.assertAll();
    }

    @Test
    void merge_should_keep_extremes() {
        // given
        final IntBoundaries x = IntBoundaries.single(3).accumulate(1);
        final IntBoundaries y = IntBoundaries.single(50).accumulate(4);

        // when
        final IntBoundaries m = x.merge(y);

        // then
        assertIntBoundaries(m, 1, 50);
    }

    @Test
    void merge_should_limit_new() {
        // given
        final IntBoundaries x = IntBoundaries.single(30).accumulate(1);
        final IntBoundaries y = IntBoundaries.single(3).accumulate(17);

        // when
        final IntBoundaries m1 = x.merge(y);
        final IntBoundaries m2 = y.merge(x);

        // then
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(m1).as("on best").isSameAs(x);
        softly.assertThat(m2).as("merging best").isSameAs(x);
        softly.assertAll();
    }

    @Test
    void of_should_reduce() {
        // given
        final IntStream elems = IntStream.of(30, 1, 3, 17).parallel();

        // when
        final Optional<IntBoundaries> optB = IntBoundaries.of(elems);

        // then
        assertThat(optB).hasValueSatisfying(b -> assertIntBoundaries(b, 1, 30));
    }

    @Test
    void of_should_return_empty_on_empty_Stream() {
        // given
        final Stream<Integer> elems = Stream.of(new Dist(30.9, "m"), new Dist(3.5, "m"), new Dist(17.0, "cm"))
                .filter(d -> d.getDimension().equals("km"))
                .map(d -> BigDecimal.valueOf(d.getVal()).intValue());

        // when
        final Optional<IntBoundaries> optB = IntBoundaries.of(elems);

        // then
        assertThat(optB).isEmpty();
    }

    @Test
    void of_should_return_empty_when_null_values() {
        // given
        final Stream<Integer> elems = Stream.of(null, null);

        // when
        final Optional<IntBoundaries> optB = IntBoundaries.of(elems);

        // then
        assertThat(optB).isEmpty();
    }

    @Test
    void onX_shouldDoNothing_onEmpty() {
        final List<Integer> visitOne = new ArrayList<>();

        IntBoundaries.empty()
                .onBoth((n, x) -> fail("visited EMPTY!"))
                .onMax(visitOne::add)
                .onMin(visitOne::add);

        assertThat(visitOne).as("no visit on EMPTY").isEmpty();
    }

    @Test
    void onX_shouldDoPassValue_onNonEmpy() {
        final List<Integer> visitMax = new ArrayList<>();
        final List<Integer> visitMin = new ArrayList<>();

        IntBoundaries.single(1).accumulate(2)
                .onBoth((n, x) -> {
                    visitMax.add(10 * x);
                    visitMin.add(10 * n);
                })
                .onMax(visitMax::add)
                .onMin(visitMin::add);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(visitMin).as("minima").containsExactly(10, 1);
        softly.assertThat(visitMax).as("maxima").containsExactly(20, 2);
        softly.assertAll();
    }

}