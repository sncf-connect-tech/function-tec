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

import com.vsct.testing.data.Letter;
import com.vsct.testing.data.Lettered;
import com.vsct.testing.data.UniqueStrings;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;

class GroupedImmutablesTest {

    private final Lettered<UniqueStrings> base = Lettered.uniq(Map.of(
            Letter.EVEN, UniqueStrings.of("e1", "e2"),
            Letter.ODD, UniqueStrings.of()
    ));

    @Test
    void new_should_fail_on_null_Map() {
        // Act
        Throwable t = catchThrowable(() -> Lettered.uniq(null));

        // Assert
        assertThat(t).isInstanceOf(NullPointerException.class).hasMessage("initial groups must exist");
    }

    @Test
    void new_should_fail_on_null_empty_value() {
        // Act
        Throwable t = catchThrowable(() -> new Lettered<>(Map.of(), null));

        // Assert
        assertThat(t).isInstanceOf(NullPointerException.class).hasMessage("empty value must exist");
    }

    @Test
    void equals_should_nay_on_null() {
        // Arrange
        Object candidate = null;

        // Act & Assert
        assertThat(base.equals(candidate)).isFalse();
    }

    @Test
    void equals_should_nay_on_same_structure_but_different_concrete_class() {
        // Arrange
        class Similar extends GroupedImmutables<Letter, String, UniqueStrings> {
            Similar() {
                super(Map.of(
                        Letter.EVEN, UniqueStrings.of("e1", "e2"),
                        Letter.ODD, UniqueStrings.of("o1")
                ), UniqueStrings.of());
            }
        }
        Object candidate = new Similar();

        // Act & Assert
        assertThat(base.equals(candidate)).isFalse();
    }

    @Test
    void equals_should_nay_on_same_class_but_content_differs() {
        // Arrange
        Object candidate = Lettered.uniq(Map.of(
                Letter.EVEN, UniqueStrings.of("e1"),
                Letter.ODD, UniqueStrings.of()
        ));

        // Act & Assert
        assertThat(base.equals(candidate)).isFalse();
    }

    @Test
    void equals_should_yay_when_only_differs_on_empty_groups() {
        // Arrange
        Object candidate = Lettered.uniq(Map.of(
                Letter.EVEN, UniqueStrings.of("e1", "e2"),
                Letter.UNDEFINED, UniqueStrings.of() // no ODD but empty UNDEF
        ));

        // Act & Assert
        assertThat(base.equals(candidate)).isTrue();
    }

    @Test
    void get_should_never_return_null() {
        // Arrange
        final UniqueStrings specialEmpty = UniqueStrings.of("miss!");
        final Lettered<UniqueStrings> base = new Lettered<>(Map.of(), specialEmpty);

        // Act
        UniqueStrings notMapped = base.get(Letter.EVEN);

        // Assert
        assertThat(notMapped).isSameAs(specialEmpty);
    }

    @Test
    void has_should_nay_on_empty_group() {
        // Act & Assert
        assertThat(base.has(Letter.ODD)).isFalse();
    }

    @Test
    void has_should_nay_on_missing_group() {
        // Act & Assert
        assertThat(base.has(Letter.UNDEFINED)).isFalse();
    }

    @Test
    void has_should_yay_on_nonempty_group() {
        // Act & Assert
        assertThat(base.has(Letter.EVEN)).isTrue();
    }

    @Test
    void hash_should_lookup_inner_collection() {
        // Arrange
        final Map<Letter, UniqueStrings> inner = Map.of(Letter.EVEN, UniqueStrings.of("e1"));
        Object base = Lettered.uniq(inner);

        // Act & Assert
        assertThat(base.hashCode()).isEqualTo(inner.hashCode());
    }

    @Test
    void size_should_defer_to_content() {
        // Act & Assert
        assertThat(base.size()).as("empty groups matter").isEqualTo(2);
    }

    @Test
    void toString_should_defer_to_content() {
        // Act & Assert
        assertThat(base.toString()).as("unpredictable order")
                .isIn("{ODD=[], EVEN=[e1, e2]}", "{EVEN=[e1, e2], ODD=[]}");
    }

    @Test
    void streamEntries_should_include_empties() {
        // Act
        List<Map.Entry<Letter, UniqueStrings>> empties = base.streamEntries()
                .filter(entry -> entry.getValue().isEmpty())
                .collect(toList());

        // Assert
        assertThat(empties).containsExactly(Map.entry(Letter.ODD, UniqueStrings.of()));
    }
}