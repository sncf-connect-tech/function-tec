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

import com.vsct.testing.data.UniqueStrings;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests des comportement spécifiques à un {@code Set} immuable.
 */
class AbstractImmutableSetTest {

    @Test
    void add_should_return_same_instance_on_duplicate() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("a", "b");

        // Act
        UniqueStrings result = base.add("b");

        // Assert
        assertThat(result).isSameAs(base);
    }

    @Test
    void addAll_should_fail_on_null() {
        // Arrange
        UniqueStrings base = UniqueStrings.of();

        // Act
        Throwable t = catchThrowable(() -> base.addAll(null));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("elements to add must exist");
    }

    @Test
    void addAll_should_return_same_instance_on_empty_param() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("o", "h");

        // Act
        UniqueStrings result = base.addAll(List.of());

        // Assert
        assertThat(result).isSameAs(base);
    }

    @Test
    void addAll_should_return_same_instance_on_duplicate() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("a", "b");

        // Act
        UniqueStrings result = base.addAll(List.of("b", "a"));

        // Assert
        assertThat(result).isSameAs(base);
    }

    @Test
    void addAll_should_return_new_instance_on_non_duplicate() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("a", "b");
        UniqueStrings canary = UniqueStrings.of("a", "b");

        // Act
        UniqueStrings result = base.addAll(List.of("b", "c"));

        // Assert
        assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        assertThat(result).as("new instance").isNotSameAs(base);
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void plus_should_return_instance_when_empty_complement() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("a", "b", "c");
        UniqueStrings complement = UniqueStrings.of();

        // Act
        UniqueStrings result = base.plus(complement);

        // Assert
        assertThat(result).isSameAs(base);
    }

    @Test
    void plus_should_return_complement_when_empty_instance() {
        // Arrange
        UniqueStrings base = UniqueStrings.of();
        UniqueStrings complement = UniqueStrings.of("a", "b", "c");

        // Act
        UniqueStrings result = base.plus(complement);

        // Assert
        assertThat(result).isSameAs(complement);
        assertThat(base.isEmpty()).as("immutable content").isTrue();
    }

    @Test
    void plus_should_return_instace_when_superset() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("b", "a", "z");
        UniqueStrings canary = UniqueStrings.of("b", "a", "z");
        UniqueStrings complement = UniqueStrings.of("a", "b");

        // Act
        UniqueStrings result = base.plus(complement);

        // Assert
        assertThat(result).isSameAs(base);
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void plus_should_return_complement_when_superset() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("b", "a");
        UniqueStrings canary = UniqueStrings.of("b", "a");
        UniqueStrings complement = UniqueStrings.of("b", "a", "r");

        // Act
        UniqueStrings result = base.plus(complement);

        // Assert
        assertThat(result).isSameAs(complement);
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void plus_should_return_combination_when_simple_overlap() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("m", "u");
        UniqueStrings canary = UniqueStrings.of("m", "u");
        UniqueStrings complement = UniqueStrings.of("g", "u");

        // Act
        UniqueStrings result = base.plus(complement);

        // Assert
        assertThat(result).containsExactlyInAnyOrder("g", "u", "m");
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void size_should_defer_to_content() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("a", "a", "a", "h");

        // Act & Assert
        assertThat(base.size()).isEqualTo(2);
    }

    @Test
    void toString_should_defer_to_content() {
        // Arrange
        UniqueStrings base = UniqueStrings.of("a", "a", "a", "h");

        // Act & Assert
        assertThat(base.toString()).isEqualTo("[a, h]");
    }
}
