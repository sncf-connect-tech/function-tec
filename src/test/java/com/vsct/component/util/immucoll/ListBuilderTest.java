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

package com.vsct.component.util.immucoll;

import com.vsct.component.util.immucoll.ImmutableList.ListBuilder;
import com.vsct.testing.data.Strings;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests de la classe interne {@link ListBuilder}
 */
class ListBuilderTest {

    @Test
    void add_should_fail_on_null() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);

        // Act
        Throwable t = catchThrowable(() -> builder.add(null));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("element to add must exist");
    }

    @Test
    void add_should_work_on_nonnull() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);

        // Act
        builder.add("aa");

        // Assert
        assertThat(builder.build()).containsExactly("aa");
    }

    @Test
    void addAll_should_fail_on_null_Collection() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        List<String> strings = null;

        // Act
        Throwable t = catchThrowable(() -> builder.addAll(strings));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("elements to add must exist");
    }

    @Test
    void addAll_should_fail_on_null_element_in_Collection() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        List<String> strings = new ArrayList<>();
        strings.add("some");
        strings.add(null);
        strings.add("oops");

        // Act
        Throwable t = catchThrowable(() -> builder.addAll(strings));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("no null allowed in elements to add");
    }

    @Test
    void addAll_should_fail_on_null_ImmutableList() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        Strings strings = null;

        // Act
        Throwable t = catchThrowable(() -> builder.addAll(strings));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("holder of elements to add must exist");
    }

    @Test
    void addAll_should_work_on_empty_Collection() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        Set<String> strings = Set.of();

        // Act
        builder.addAll(strings);

        // Assert
        assertThat(builder.build()).isEmpty();
    }

    @Test
    void addAll_should_work_on_empty_ImmutableList() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        Strings strings = Strings.of();

        // Act
        builder.addAll(strings);

        // Assert
        assertThat(builder.build()).isEmpty();
    }

    @Test
    void addAll_should_work_on_nonempty_Collection() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        List<String> strings = List.of("a", "b");

        // Act
        builder.addAll(strings);

        // Assert
        assertThat(builder.build()).containsExactly("a", "b");
    }

    @Test
    void addAll_should_work_on_nonempty_ImmutableList() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        Strings strings = Strings.of("2", "1");

        // Act
        builder.addAll(strings);

        // Assert
        assertThat(builder.build()).containsExactly("2", "1");
    }

    @Test
    void merge_should_fail_on_null() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        ListBuilder<String, Strings> other = null;

        // Act
        Throwable t = catchThrowable(() -> builder.merge(other));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("builder to merge with must exist");
    }

    @Test
    void merge_should_append_all_on_nonnull() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        builder.add("1");
        builder.add("2");
        ListBuilder<String, Strings> other = ImmutableList.builder(Strings::new);
        other.add("b");
        other.add("a");

        // Act
        builder.merge(other);

        // Assert
        assertThat(builder.build()).containsExactly("1", "2", "b", "a");
    }

    @Test
    void collector_should_work() {
        // Arrange
        Stream<String> letters = Stream.of("a", "b", "c", "d");

        // Act
        Strings result = letters.parallel()
                .collect(ImmutableList.toImmutableList(Strings::new));

        // Assert
        assertThat(result.asList()).containsExactlyInAnyOrder("a", "b", "c", "d");
    }

    @Test
    void containsSame_should_be_order_dependant() {
        // Arrange
        ListBuilder<String, Strings> builder = ImmutableList.builder(Strings::new);
        builder.add("1");
        builder.add("2");

        Strings same = Strings.of("1", "2");
        Strings unordered = Strings.of("2", "1");
        Strings different = Strings.of("1", "5");
        Strings shorter = same.remove("2");
        Strings longer = same.add("3");

        // Act & Assert
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(builder.containsSame(same)).as("same content").isTrue();
        softly.assertThat(builder.containsSame(unordered)).as("same content, different order").isFalse();
        softly.assertThat(builder.containsSame(different)).as("different content").isFalse();
        softly.assertThat(builder.containsSame(shorter)).as("less content in param").isFalse();
        softly.assertThat(builder.containsSame(longer)).as("more content in param").isFalse();
        softly.assertAll();
    }
}
