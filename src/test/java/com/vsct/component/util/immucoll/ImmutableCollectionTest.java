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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ImmutableCollectionTest {

    @Test
    void asList_should_return_List() {
        // Arrange
        Tested base = Tested.of("b", "o", "o", "l");

        // Act & Assert
        assertThat(base.asList()).containsExactly("b", "o", "o", "l");
    }

    @Test
    void asSet_should_return_Set() {
        // Arrange
        Tested base = Tested.of("b", "o", "o", "l");

        // Act & Assert
        assertThat(base.asSet()).containsExactlyInAnyOrder("b", "o", "l");
    }

    @Test
    void contains_should_return_false_when_null() {
        // Arrange
        Tested base = Tested.of("a");

        // Act &  Assert
        assertThat(base.contains(null)).isFalse();
    }

    @Test
    void contains_should_return_false_when_unkown() {
        // Arrange
        Tested base = Tested.of("a");

        // Act &  Assert
        assertThat(base.contains("b")).isFalse();
    }

    @Test
    void contains_should_return_true_when_contained() {
        // Arrange
        Tested base = Tested.of("a", "b");

        // Act &  Assert
        assertThat(base.contains("b")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void isEmpty_should_defer_to_size() {
        // Arrange
        List<String> inner = mock(List.class);
        given(inner.size()).willThrow(new UnsupportedOperationException("boom"));
        Tested base = new Tested(inner);

        // Act
        Throwable t = catchThrowable(base::isEmpty);

        // Assert
        assertThat(t).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("boom");
    }

    @Test
    void contains_should_fail_on_base_Builder() {
        // Arrange
        ImmutableCollection.Builder<String, List<String>, Tested> builder =
                new ImmutableCollection.Builder<>(Tested::new, TypeDelegate.arrayList());
        builder.add("o");

        Tested same = Tested.of("o");

        // Act
        Throwable t = catchThrowable(() -> builder.containsSame(same));

        // Assert
        assertThat(t).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("cannot compare content");
    }

    @Test
    void builder_inner_list_should_be_safe_from_constructor_function_tampering() {
        // Arrange
        ImmutableCollection.Builder<String, List<String>, Tested> safeBuilder =
                new ImmutableCollection.Builder<>(Tested::new, TypeDelegate.arrayList());
        safeBuilder.add("o");

        ImmutableCollection.Builder<String, List<String>, Tested> taintedBuilder =
                new ImmutableCollection.Builder<>(this::evil, TypeDelegate.arrayList());
        taintedBuilder.add("n");
        taintedBuilder.add("i");

        // Act
        Tested tainted = taintedBuilder.build(); // donc remplace tout le contenu par
        safeBuilder.merge(taintedBuilder); // pour copier le contenu réel

        // Assert
        assertThat(tainted).containsExactly("💔");
        assertThat(safeBuilder.build()).containsExactly("o", "n", "i");
    }

    private Tested evil(List<String> base) {
        base.clear();
        base.add("🔥");
        return Tested.of("💔");
    }

    /**
     * implémentation "basique" pour pouvoir tester tous les comportements abstraits,
     * <strong>sans</strong> s'appuyer sur {@link AbstractImmutableList}.
     */
    private static class Tested implements ImmutableCollection<String> {

        private final List<String> inner;
        Tested(List<String> inner) {
            this.inner = inner;
        }
        static Tested of(String... values) {
            return new Tested(List.of(values));
        }

        @Override
        public Stream<String> stream() {
            return inner.stream();
        }

        @Override
        public int size() {
            return inner.size();
        }

        @Override
        public Iterator<String> iterator() {
            return inner.iterator();
        }

        @Override
        public String toString() {
            return "T" + inner.toString();
        }
    }
}
