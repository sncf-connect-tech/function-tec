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

import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class VArraysTest {

    @Test
    void first_shouldReturnEmpty_whenNullArray() {
        final String[] arr = null;

        final Optional<String> first = VArrays.first(arr);

        assertThat(first).isEmpty();
    }

    @Test
    void first_shouldReturnEmpty_whenEmptyArray() {
        final String[] arr = {};

        final Optional<String> first = VArrays.first(arr);

        assertThat(first).isEmpty();
    }

    @Test
    void first_shouldReturnEmpty_whenNullFirstElem() {
        final String[] arr = {null, "bb"};

        final Optional<String> first = VArrays.first(arr);

        assertThat(first).isEmpty();
    }

    @Test
    void first_shouldReturnElement_whenNonNull() {
        final Heading expected = new Heading(2d, "m", Direction.N);
        final Dist[] arr = {expected, new Dist(1d, "km")};

        // coerciction de type : pas besoin de complexifier la signature de la méthode
        final Optional<Dbl> first = VArrays.first(arr);

        assertThat(first).hasValue(expected);
    }

    @Test
    void firstThat_shouldReturnEmpty_whenNullArray() {
        final String[] arr = null;

        final Optional<String> first = VArrays.firstThat(arr, Objects::nonNull);

        assertThat(first).isEmpty();
    }

    @Test
    void firstThat_shouldFail_whenNullPredicate() {
        final String[] arr = {"a", "b"};

        final Throwable e = catchThrowable(() -> VArrays.firstThat(arr, null));

        assertThat(e).isInstanceOf(NullPointerException.class)
                .hasMessage("filtering without condition!");
    }

    @Test
    void firstThat_shouldReturnEmpty_whenEmptyArray() {
        final String[] arr = {};

        final Optional<String> first = VArrays.firstThat(arr, o -> true);

        assertThat(first).isEmpty();
    }

    @org.junit.jupiter.api.Test
    void firstThat_shouldReturnEmpty_whenNoMatchInArray() {
        final String[] arr = {"a", "bb"};

        final Optional<String> first = VArrays.firstThat(arr, s -> s.length() == 3);

        assertThat(first).isEmpty();
    }

    @Test
    void firstThat_shouldReturnElement_whenMatchInArray() {
        final Heading expected = new Heading(2d, "m", Direction.N);
        final Heading[] arr = {new Heading(0.1, "mi", Direction.W), expected};
        final Predicate<Dist> meter = d -> "m".equals(d.getDimension());

        final Optional<Heading> first = VArrays.firstThat(arr, meter);

        assertThat(first).hasValue(expected);
    }

}