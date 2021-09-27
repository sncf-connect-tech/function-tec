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

import com.vsct.component.util.immucoll.ImmutableSet.SetBuilder;
import com.vsct.testing.data.UniqueStrings;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests de la classe interne {@link SetBuilder}
 */
class SetBuilderTest {

    @Test
    void add_should_fail_on_null() {
        // Arrange
        Tested builder = new Tested();

        // Act
        Throwable t = catchThrowable(() -> builder.add(null));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("element to add must exist");
    }

    @Test
    void add_should_deduplicate_on_nonnull() {
        // Arrange
        Tested builder = new Tested();

        // Act
        builder.add("aa");
        builder.add("aa");

        // Assert
        assertThat(builder.build()).containsExactly("aa");
    }

    @Test
    void addAll_should_fail_on_null_Collection() {
        // Arrange
        Tested builder = new Tested();
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
        Tested builder = new Tested();
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
    void addAll_should_fail_on_null_ImmutableSet() {
        // Arrange
        Tested builder = new Tested();
        SortedStrings strings = null;

        // Act
        Throwable t = catchThrowable(() -> builder.addAll(strings));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("holder of elements to add must exist");
    }

    @Test
    void addAll_should_work_on_empty_Collection() {
        // Arrange
        Tested builder = new Tested();
        Set<String> strings = Set.of();

        // Act
        builder.addAll(strings);

        // Assert
        assertThat(builder.build()).isEmpty();
    }

    @Test
    void addAll_should_work_on_empty_ImmutableSet() {
        // Arrange
        Tested builder = new Tested();
        SortedStrings strings = SortedStrings.of();

        // Act
        builder.addAll(strings);

        // Assert
        assertThat(builder.build()).isEmpty();
    }

    @Test
    void addAll_should_work_on_nonempty_Collection() {
        // Arrange
        Tested builder = new Tested();
        List<String> strings = List.of("a", "b");

        // Act
        builder.addAll(strings);

        // Assert
        assertThat(builder.build()).containsExactly("a", "b");
    }

    @Test
    void addAll_should_work_on_nonempty_ImmutableSet() {
        // Arrange
        Tested builder = new Tested();
        SortedStrings strings = SortedStrings.of("2", "1");

        // Act
        builder.addAll(strings);

        // Assert
        assertThat(builder.build()).containsExactly("1", "2");
    }

    @Test
    void merge_should_fail_on_null() {
        // Arrange
        Tested builder = new Tested();
        Tested other = null;

        // Act
        Throwable t = catchThrowable(() -> builder.merge(other));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("builder to merge with must exist");
    }

    @Test
    void merge_should_append_all_on_nonnull() {
        // Arrange
        Tested builder = new Tested();
        builder.add("d");
        builder.add("c");
        Tested other = new Tested();
        other.add("b");
        other.add("a");

        // Act
        builder.merge(other);

        // Assert
        assertThat(builder.build()).containsExactly("a", "b", "c", "d");
    }

    @Test
    void containsSame_should_be_size_dependant() {
        // Arrange
        SetBuilder<String, Set<String>, UniqueStrings> builder = ImmutableSet
                .builder(UniqueStrings::new, TypeDelegate.hashSet());
        builder.add("2");
        builder.add("1");

        UniqueStrings same = UniqueStrings.of("1", "2");
        UniqueStrings different = UniqueStrings.of("2", "5");
        UniqueStrings shorter = same.remove("1");
        UniqueStrings longer = same.add("3");

        // Act & Assert
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(builder.containsSame(same)).as("same content").isTrue();
        softly.assertThat(builder.containsSame(different)).as("same content, different order").isFalse();
        softly.assertThat(builder.containsSame(shorter)).as("less content in param").isFalse();
        softly.assertThat(builder.containsSame(longer)).as("more content in param").isFalse();
        softly.assertAll();
    }

    /**
     * implémentation "basique" pour pouvoir tester.
     */
    private static class SortedStrings extends AbstractImmutableSet<String, SortedSet<String>, SortedStrings> {
        static final TypeDelegate<String, NavigableSet<String>> DLG = TypeDelegate.treeSet();

        SortedStrings(SortedSet<String> inner) {
            super(inner, SortedStrings::new, DLG);
        }

        static SortedStrings of(String... values) {
            return Arrays.stream(values)
                    .collect(ImmutableSet.toImmutableSet(SortedStrings::new, DLG));
        }

        @Override
        public String toString() {
            return "T" + inner.toString();
        }
    }

    private static class Tested extends SetBuilder<String, SortedSet<String>, SortedStrings>  {
        Tested() {
            super(SortedStrings::new, SortedStrings.DLG);
        }
    }
}
