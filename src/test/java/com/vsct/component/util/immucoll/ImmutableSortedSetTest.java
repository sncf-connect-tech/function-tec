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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;

class ImmutableSortedSetTest {

    @Test
    void addAll_should_return_new_instance_on_non_duplicate() {
        // Arrange
        Tested base = Tested.of("a", "b");
        Tested canary = Tested.of("a", "b");

        // Act
        Tested result = base.addAll(List.of("b", "c"));

        // Assert
        assertThat(result).containsExactly("a", "b", "c");
        assertThat(result).as("new instance").isNotSameAs(base);
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void plus_should_return_combination_when_simple_overlap() {
        // Arrange
        Tested base = Tested.of("m", "u");
        Tested canary = Tested.of("m", "u");
        Tested complement = Tested.of("g", "u");

        // Act
        Tested result = base.plus(complement);

        // Assert
        assertThat(result).containsExactly("g", "m", "u");
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void size_should_defer_to_content() {
        // Arrange
        Tested base = Tested.of("a", "a", "a", "h");

        // Act & Assert
        assertThat(base.size()).isEqualTo(2);
    }

    /**
     * implémentation "basique" pour pouvoir tester tous les comportements.
     */
    private static class Tested extends AbstractImmutableSet<String, SortedSet<String>, Tested> {
        private static final TypeDelegate<String, NavigableSet<String>> DLG = TypeDelegate.treeSet();

        private Tested(SortedSet<String> inner) {
            super(inner, Tested::new, DLG);
        }

        static Tested of(String... values) {
            return Arrays.stream(values)
                    .collect(ImmutableSet.toImmutableSet(Tested::new, DLG));
        }

        @Override
        public String toString() {
            return "T" + inner.toString();
        }
    }
}
