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

import com.vsct.testing.data.Strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests des comportement spécifiques à une {@code List} immuable.
 */
class AbstractImmutableListTest {

    @Test
    void add_should_fail_on_null() {
        // Arrange
        Strings base = Strings.of();

        // Act
        Throwable t = catchThrowable(() -> base.add(null));

        // Assert
        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("element to add must exist");
    }

    @Test
    void add_should_return_new_instance() {
        // Arrange
        Strings base = Strings.of("a");
        Strings canary = Strings.of("a");

        // Act
        Strings result = base.add("b");

        // Assert
        Assertions.assertThat(result).containsExactly("a", "b");
        Assertions.assertThat(result).as("new instance").isNotSameAs(base);
        Assertions.assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void first_should_fail_on_empty() {
        // Arrange
        Strings base = Strings.of();

        // Act
        Throwable t = catchThrowable(base::first);

        // Assert
        assertThat(t).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void first_should_return_on_non_empty() {
        // Arrange
        Strings base = Strings.of("t", "o", "l", "l");

        // Act
        String result = base.first();

        // Assert
        assertThat(result).isEqualTo("t");
    }

    @Test
    void last_should_fail_on_empty() {
        // Arrange
        Strings base = Strings.of();

        // Act
        Throwable t = catchThrowable(base::last);

        // Assert
        assertThat(t).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void last_should_return_first_on_single() {
        // Arrange
        Strings base = Strings.of("t");

        // Act
        String result = base.last();

        // Assert
        assertThat(result).isEqualTo("t");
    }

    @Test
    void sort_should_return_instance_when_already_ordered() {
        // Arrange
        Strings base = Strings.of("d", "e", "f");

        // Act
        Strings result = base.sort(String::compareTo);

        // Assert
        Assertions.assertThat(result).isSameAs(base);
        Assertions.assertThat(result).as("immutable content").containsExactly("d", "e", "f");
    }

    @Test
    void sort_should_reorder() {
        // Arrange
        Strings base = Strings.of("d", "o", "d", "u");
        Strings canary = Strings.of("d", "o", "d", "u");

        // Act
        Strings result = base.sort(String::compareTo);

        // Assert
        Assertions.assertThat(result).containsExactly("d", "d", "o", "u");
        Assertions.assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void size_should_defer_to_content() {
        // Arrange
        Strings base = Strings.of("a", "a", "a", "h");

        // Act & Assert
        assertThat(base.size()).isEqualTo(4);
    }

    @Test
    void toString_should_defer_to_content() {
        // Arrange
        Strings base = Strings.of("a", "a", "a", "h");

        // Act & Assert
        assertThat(base.toString()).isEqualTo("[a, a, a, h]");
    }
}