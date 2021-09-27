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

import com.vsct.testing.data.Dbl;
import com.vsct.testing.data.Direction;
import com.vsct.testing.data.Dist;
import com.vsct.testing.data.Heading;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static com.vsct.testing.tool.VAssertions.assertBoundaries;
import static com.vsct.testing.tool.VAssertions.assertIntBoundaries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class BoundariesTest {

    private final Comparator<String> byHexValue = Comparator.comparingLong(str -> Long.parseLong(str, 16));
    private final Comparator<String> byLength = Comparator.comparing(String::length);

    // ======================================================================================
    //
    // Object standard behavior
    //
    // ======================================================================================

    @Test
    void equals_based_on_values_and_comparator() {
        final Boundaries<String> ab1 = Boundaries.single("a").accumulate("bb");
        final Boundaries<String> ab2 = Boundaries.single("bb").accumulate("a");
        final Boundaries<String> ab3 = Boundaries.single("a", byHexValue).accumulate("bb");
        final Boundaries<String> ab4 = Boundaries.single("a", byLength).accumulate("bb");
        final Boundaries<String> ab5 = Boundaries.single("a", (s1, s2) -> s2.length() - s1.length()).accumulate("bb");
        final Boundaries<String> ab6 = Boundaries.single("a", (s1, s2) -> s2.length() - s1.length()).accumulate("bb");

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ab2).as("both natural order").isEqualTo(ab1);
        softly.assertThat(ab3).as("hexa value vs. natural order").isNotEqualTo(ab1);
        softly.assertThat(ab4).as("length vs. natural order").isNotEqualTo(ab1);
        softly.assertThat(ab4).as("length vs. hexa value").isNotEqualTo(ab3);
        softly.assertThat(ab6).as("both reverse length, but separate lambda").isNotEqualTo(ab5);
        softly.assertAll();
    }

    // ======================================================================================
    //
    // Stream#reduce standard behavior
    //
    // ======================================================================================

    @Test
    void identity_must_be_transparent_in_combiner() {
        // given
        final BinaryOperator<Boundaries<String>> combiner = Boundaries::merge;
        final Boundaries<String> identity = initS(null);
        final Boundaries<String> b = initS("a");

        // when
        final Boundaries<String> actual = combiner.apply(identity, b);

        // then
        assertThat(actual).isEqualTo(b);
    }

    @Test
    void combiner_must_be_compatible_with_accumulator() {
        // given
        final BiFunction<Boundaries<String>, String, Boundaries<String>> accumulator = Boundaries::accumulate;
        final BinaryOperator<Boundaries<String>> combiner = Boundaries::merge;
        final Boundaries<String> identity = initS(null);
        final String t = "a";
        final Boundaries<String> u = initS("b");

        // when
        final Boundaries<String> identityAccumulated = combiner.apply(u, accumulator.apply(identity, t));
        final Boundaries<String> direct = accumulator.apply(u, t);

        // then
        assertThat(identityAccumulated).isEqualTo(direct);
    }

    // ======================================================================================
    //
    // specific behavior
    //
    // ======================================================================================

    @Test
    void accumulate_should_accept_null() {
        // given
        final Boundaries<String> x = initS("h");

        // when
        final Boundaries<String> a = x.accumulate(null);

        // then
        assertThat(a).as("expected no-op").isSameAs(x);
    }

    @Test
    void accumulate_should_limit_new() {
        // given
        final Boundaries<String> x = initS("h").accumulate("b");

        // when
        final Boundaries<String> a = x.accumulate("d");

        // then
        assertThat(a).as("unneeded instance").isSameAs(x);
        assertBoundaries(a, "b", "h");
    }

    @Test
    void empty_is_single() {
        // given
        Boundaries<String> x = Boundaries.empty();

        // when + then
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(x.isEmpty()).as("expected empty").isTrue();
        softly.assertThat(x.isSingle()).as("expected single").isTrue();
        softly.assertThat(x.isRange()).as("unexpected range").isFalse();
        softly.assertAll();
    }

    @Test
    void map_should_instanciate_when_comparator_differs() {
        // given
        final Boundaries<String> x = initS(null);

        // when
        final Boundaries<Direction> mapped = x.mapNaturalOrder(Direction::valueOf);

        // then
        assertThat(mapped).as("loose alt. comparator").isNotSameAs(x);
    }

    @Test
    void map_should_reverse_when_comparator_requires() {
        // given
        final Boundaries<String> base = initS("a").accumulate("zzzz");
        final Comparator<Integer> reverseOrder = (a, b) -> b - a;

        // when
        final Boundaries<Integer> mapped = base.map(String::length, reverseOrder);

        // then
        assertBoundaries(mapped, 4, 1);
    }

    @Test
    void mapToInt_should_return_empty_on_empty() {
        // given
        final Comparator<List<Direction>> byFirstElem = Comparator.comparing(list -> list.get(0));
        final Boundaries<List<Direction>> base = Boundaries.empty(byFirstElem);

        // when
        final IntBoundaries mapped = base.mapToInt(List::size);

        // then
        assertThat(mapped.isEmpty()).isTrue();
    }

    @Test
    void mapToInt_should_reverse_when_order_requires() {
        // given
        final Comparator<String> reverseLenOrder = Comparator.comparingInt(String::length).reversed();
        final Boundaries<String> base = Boundaries.single("a", reverseLenOrder).accumulate("zzzz");

        // when
        final IntBoundaries mapped = base.mapToInt(String::length);

        // then
        assertIntBoundaries(mapped, 1, 4);
    }

    @Test
    void merge_should_keep_extremes() {
        // given
        final Heading d1 = new Heading(1.0, "m", Direction.N);
        final Heading d50 = new Heading(50.1, "mm", Direction.S);
        final Boundaries<Dist> x = initD(new Dist(3.3, "cm"), d1);
        final Boundaries<Dist> y = initD(d50, new Dist(4.09, "dm"));

        // when
        final Boundaries<Dist> m = x.merge(y);

        // then
        assertBoundaries(m, d1, d50);
    }

    @Test
    void merge_should_limit_new() {
        // given
        final Boundaries<Dist> x = initD(new Dist(30.9, "m"), new Dist(1.1, "mm"));
        final Boundaries<Dist> y = initD(new Dist(3.5, "m"), new Dist(17.0, "cm"));

        // when
        final Boundaries<Dist> m1 = x.merge(y);
        final Boundaries<Dist> m2 = y.merge(x);

        // then
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(m1).as("on best").isSameAs(x);
        softly.assertThat(m2).as("merging best").isSameAs(x);
        softly.assertAll();
    }

    @Test
    void merge_shouldNotVerifyComparator() {
        // given
        Boundaries<String> b1 = initS("b").accumulate("f");
        Boundaries<String> b2 = Boundaries.single("a", byHexValue).accumulate("d");

        // when
        Boundaries<String> m1 = b1.merge(b2).accumulate("10"); // alphabétique
        Boundaries<String> m2 = b2.merge(b1).accumulate("10"); // hexa

        // then
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(m1.getMin()).as("merge #1 min").isEqualTo("10");
        softly.assertThat(m1.getMax()).as("merge #1 max").isEqualTo("f");
        softly.assertThat(m2.getMin()).as("merge #2 min").isEqualTo("a");
        softly.assertThat(m2.getMax()).as("merge #2 max").isEqualTo("10");
        softly.assertAll();
    }

    @Test
    void of_should_reduce() {
        // given
        final Dbl max = new Dbl(30.9);
        final Dbl min = new Dbl(3.5);
        final Stream<Dbl> elems = Stream.of(new Dist(max, "m"), new Dist(1.1, "mm"), new Dist(min, "m"), new Dist(17.0, "cm"))
                .parallel()
                .filter(d -> d.getDimension().equals("m"))
                .map(Dist::toVal);

        // when
        final Optional<Boundaries<Dbl>> optB = Boundaries.of(elems, Comparator.comparingDouble(Dbl::getVal));

        // then
        assertThat(optB).hasValueSatisfying(b -> assertBoundaries(b, min, max));
    }

    @Test
    void of_should_return_empty_on_empty_Stream() {
        // given
        final Stream<Dist> elems = Stream.of(new Dist(30.9, "m"), new Dist(3.5, "m"), new Dist(17.0, "cm"))
                .filter(d -> d.getDimension().equals("km"));

        // when
        final Optional<Boundaries<Dbl>> optB = Boundaries.of(elems, Comparator.comparingDouble(Dbl::getVal));

        // then
        assertThat(optB).isEmpty();
    }

    @Test
    void ofNaturalOrder_should_reduce() {
        // given
        final Stream<String> elems = Stream.of("b0", "e2", "d4", "0a", "31", "e0").parallel();

        // when
        final Optional<Boundaries<String>> optB = Boundaries.ofNaturalOrder(elems);

        // then
        assertThat(optB).hasValueSatisfying(b -> assertBoundaries(b, "0a", "e2"));
    }

    @Test
    void of_should_return_empty_when_null_values() {
        // given
        final Stream<String> elems = Stream.of(null, null);

        // when
        final Optional<Boundaries<String>> optB = Boundaries.ofNaturalOrder(elems);

        // then
        assertThat(optB).isEmpty();
    }

    @Test
    void onX_shouldDoNothing_onNull() {
        final List<Long> visitOne = new ArrayList<>();

        Boundaries.single(null, Long::compare)
                .onBoth((n, x) -> fail("visited null!"))
                .onMax(visitOne::add)
                .onMin(visitOne::add);

        assertThat(visitOne).as("no visit on null").isEmpty();
    }

    @Test
    void onX_shouldDoPassValue_onNonNull() {
        final List<String> visitMax = new ArrayList<>();
        final List<String> visitMin = new ArrayList<>();

        initS("a").accumulate("b")
                .onBoth((n, x) -> {
                    visitMax.add("b_" + x);
                    visitMin.add("b_" + n);
                })
                .onMax(visitMax::add)
                .onMin(visitMin::add);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(visitMin).as("minima").containsExactly("b_a", "a");
        softly.assertThat(visitMax).as("maxima").containsExactly("b_b", "b");
        softly.assertAll();
    }

    @Test
    void relaxedComparableContract() {
        // given
        final LocalDateTime d1 = LocalDateTime.of(1980, Month.JANUARY, 2, 3, 4);
        final LocalDateTime d2 = LocalDateTime.of(1998, Month.JULY, 12, 21, 27);

        // when
        // fonctionne quand bien même LocalDateTime est Comparable<ChronoLocalDateTime>
        //                                                         ^^^^^^
        final Boundaries<LocalDateTime> base = Boundaries.single(d1).accumulate(d2);
        // là encore, LocalDate est Comparable<ChronoLocalDate>
        //                                     ^^^^^^
        final Boundaries<LocalDate> result = base.mapNaturalOrder(LocalDateTime::toLocalDate);

        // then
        assertBoundaries(result, LocalDate.of(1980, 1, 2), LocalDate.of(1998, 7, 12));
    }

    @Test
    void reverse_shouldSwap() {
        // given
        final Boundaries<String> str = Boundaries
                .single("aa", byLength) // = [aa,aa]
                .accumulate("b")        // = [b,aa]
                .accumulate("ccc")      // = [b,ccc]
                .accumulate("d");       // = [b,ccc] (car la chaine "longueur 1" existe déjà)

        // when
        final Boundaries<String> result = str.reverse()
                // chaine + longue : sera considérée comme nouveau minimum
                .accumulate("eeee");

        // then
        assertBoundaries(result, "eeee", "b");
    }

    // ======================================================================================
    //
    // data-set
    //
    // ======================================================================================

    private static Boundaries<Dist> initD(Dist val1, Dist val2) {
        return Boundaries.single(val1, Comparator.comparingDouble(Dbl::getVal))
                .accumulate(val2);
    }

    private static Boundaries<String> initS(String value) {
        return Boundaries.single(value);
    }
}
