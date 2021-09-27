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
import com.vsct.testing.data.Mutable;
import com.vsct.testing.data.Mutables;
import com.vsct.testing.data.UniqueStrings;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests des comportements spécifiques à une collection immuable.
 *
 * <p>Conformément à l'avertissement dans la classe principale, chaque
 * élément <em>peut</em> bouger (et un test couvre d'ailleurs ce point :
 * {@link #cannot_prevent_element_alteration_when_they_are_mutable}).
 * <br>C'est pourquoi vous trouverez une instance "{@code canary}" dans les
 * méthodes (construite par duplication de la ligne "donnée initiale"),
 * de manière à prouver que celle-ci n'a bien aucunement bougé (ni ses éléments).
 */
class AbstractImmutableCollectionTest {

    /**
     * tester l'égalité en 1er pour avoir confiance ensuite lorsqu'on valide que les instances
     * n'ont pas bougé (variable {@code canary}).
     */
    @Test
    void equals_should_lookup_content_in_order() {
        // Arrange
        Tested t1 = new Tested(List.of("a", "b", "c"));
        Tested t2 = new Tested(List.of("a", "b", "c"));
        Tested t3 = Tested.of("c", "a", "b");
        Tested t4 = Tested.of("a", "b");

        // Act
        boolean same = t1.equals(t2);
        boolean disorder = t1.equals(t3);
        boolean missing = t1.equals(t4);

        // Assert
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(same).as("same content").isTrue();
        softly.assertThat(disorder).as("wrong order").isFalse();
        softly.assertThat(missing).as("missing content").isFalse();
        softly.assertAll();
    }

    @Test
    void cannot_prevent_element_alteration_when_they_are_mutable() {
        // Arrange
        Mutables base = Mutables.of("k", "o", "o", "l");
        Mutables canary = Mutables.of("k", "o", "o", "l");

        // Act
        Mutables result = base.stream()
                .map(m -> m.withSuffix("2"))
                .collect(ImmutableList.toImmutableList(Mutables::new));

        // Assert
        assertThat(result.stream().map(Mutable::toString)).containsExactly("k2", "o2", "o2", "l2");
        assertThat(result).as("new instance").isNotSameAs(base);
        assertThat(result).as("but mutated content").isEqualTo(base);
        assertThat(base).as("not really immutable").isNotEqualTo(canary);
    }

    @org.junit.jupiter.api.Test
    void addAll_should_fail_on_null() {
        // Arrange
        Tested base = Tested.of();

        // Act
        Throwable t = catchThrowable(() -> base.addAll(null));

        // Assert
        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("elements to add must exist");
    }

    @Test
    void addAll_should_return_same_instance_on_empty_param() {
        // Arrange
        Tested base = Tested.of("o", "h");

        // Act
        Tested result = base.addAll(List.of());

        // Assert
        assertThat(result).isSameAs(base);
    }

    @org.junit.jupiter.api.Test
    void addAll_should_return_new_instance_on_non_empty_param() {
        // Arrange
        Tested base = Tested.of("a", "b");
        Tested canary = Tested.of("a", "b");

        // Act
        Tested result = base.addAll(List.of("b", "a"));

        // Assert
        assertThat(result).containsExactly("a", "b", "b", "a");
        assertThat(result).as("new instance").isNotSameAs(base);
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void filter_should_return_same_instance_when_all_match() {
        // Arrange
        Tested base = Tested.of("f", "o", "o");

        // Act
        Tested result = base.filter(str -> str.length() < 2);

        // Assert
        assertThat(result).isSameAs(base);
    }

    @Test
    void filter_should_return_new_instance_when_match() {
        // Arrange
        Tested base = Tested.of("b", "a", "r");
        Tested canary = Tested.of("b", "a", "r");

        // Act
        Tested result = base.filter(str -> str.charAt(0) > 'd');

        // Assert
        assertThat(result).containsExactly("r");
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @org.junit.jupiter.api.Test
    void groupBy_should_force_value_type() {
        // Arrange
        Tested base = Tested.of("Foo", "Bar", "Baz");

        // Act
        ByBasic result = base.groupBy(str -> new BasicKey("k" + str.charAt(0)), ByBasic::new);

        // Assert
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.get(new BasicKey("kB"))).as("Bs").isInstanceOf(Tested.class).containsExactly("Bar", "Baz");
        softly.assertThat(result.get(new BasicKey("kF"))).as("Fs").isInstanceOf(Tested.class).containsExactly("Foo");
        softly.assertAll();
    }

    @Test
    void groupByThenMap_should_use_downstream_Immutable() {
        // Arrange
        Tested base = Tested.of("C", "O", "F", "E", "F", "E", "3", "long", "");
        // > déclaration explicite pour "aider" l'inférence de type
        Function<Map<Letter, UniqueStrings>, Lettered<UniqueStrings>> groupConstructor = Lettered::uniq;

        // Act
        Lettered<UniqueStrings> result = base.groupByThenMap(
                Letter::resolve,
                s -> Stream.of(s + s),
                UniqueStrings::new,
                TypeDelegate.treeSet(), // alors que UniqueStrings est normalement basée sur HashSet
                groupConstructor);

        // Assert
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.get(Letter.EVEN)).as("evens").isInstanceOf(UniqueStrings.class).containsExactly("FF");
        softly.assertThat(result.get(Letter.ODD)).as("odds").containsExactlyInAnyOrder("CC", "EE", "OO");
        softly.assertThat(result.get(Letter.UNDEFINED)).as("undef").containsExactlyInAnyOrder("", "33", "longlong");
        softly.assertAll();
    }

    @org.junit.jupiter.api.Test
    void hash_should_lookup_inner_collection() {
        // Arrange
        List<String> inner = List.of("some", "content");
        Tested base = new Tested(inner);

        // Act & Assert
        assertThat(base.hashCode()).isEqualTo(inner.hashCode());
    }

    @org.junit.jupiter.api.Test
    void iterator_should_fail_on_remove() {
        // Arrange
        Tested base = Tested.of("a", "z");

        // Act
        Iterator<String> ite = base.iterator();

        // Assert
        assertThat(ite.hasNext()).isTrue();
        assertThat(ite.next()).isEqualTo("a");
        final Throwable t = catchThrowable(ite::remove);
        assertThat(t).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("cannot remove element of AbstractImmutableCollectionTest$Tested");
    }

    @Test
    void minus_should_return_instance_when_no_intersection() {
        // Arrange
        Tested base = Tested.of("a", "b", "c");
        Tested disjoined = Tested.of("1", "2", "3");
        Tested empty = Tested.of();

        // Act
        Tested resultDisjoined = base.minus(disjoined);
        Tested resultEmpty = base.minus(empty);

        // Assert
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(resultDisjoined).as("disjoined").isSameAs(base);
        softly.assertThat(resultEmpty).as("empty").isSameAs(base);
        softly.assertThat(base).as("immutable content").containsExactly("a", "b", "c");
        softly.assertAll();
    }

    @org.junit.jupiter.api.Test
    void minus_should_return_new_instance_when_intersect() {
        // Arrange
        Tested base = Tested.of("k", "o", "o", "l");
        Tested canary = Tested.of("k", "o", "o", "l");
        Tested removal = Tested.of("o", "k");

        // Act
        Tested result = base.minus(removal);

        // Assert
        assertThat(result).containsExactly("l");
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void plus_should_return_instance_when_empty_complement() {
        // Arrange
        Tested base = Tested.of("a", "b", "c");
        Tested complement = Tested.of();

        // Act
        Tested result = base.plus(complement);

        // Assert
        assertThat(result).isSameAs(base);
    }

    @Test
    void plus_should_return_complement_when_empty_instance() {
        // Arrange
        Tested base = Tested.of();
        Tested complement = Tested.of("a", "b", "c");

        // Act
        Tested result = base.plus(complement);

        // Assert
        assertThat(result).isSameAs(complement);
        assertThat(base.isEmpty()).as("immutable content").isTrue();
    }

    @org.junit.jupiter.api.Test
    void plus_should_return_combination_when_both_non_empty() {
        // Arrange
        Tested base = Tested.of("f", "o", "o");
        Tested canary = Tested.of("f", "o", "o");
        Tested complement = Tested.of("b", "a", "r");

        // Act
        Tested result = base.plus(complement);

        // Assert
        assertThat(result).containsExactly("f", "o", "o", "b", "a", "r");
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @Test
    void remove_should_return_instance_when_null() {
        // Arrange
        Tested base = Tested.of("a", "b", "c");

        // Act
        Tested result = base.remove(null);

        // Assert
        assertThat(result).isSameAs(base);
        assertThat(base).as("immutable content").containsExactly("a", "b", "c");
    }

    @Test
    void remove_should_return_instance_when_not_contained() {
        // Arrange
        Tested base = Tested.of("a", "b", "c");
        String notHere = "1";

        // Act
        Tested result = base.remove(notHere);

        // Assert
        assertThat(result).isSameAs(base);
        assertThat(base).as("immutable content").containsExactly("a", "b", "c");
    }

    @Test
    void remove_should_return_new_instance_when_contained() {
        // Arrange
        Tested base = Tested.of("k", "o", "o", "l");
        Tested canary = Tested.of("k", "o", "o", "l");
        String removal = "o";

        // Act
        Tested result = base.remove(removal);

        // Assert
        assertThat(result).containsExactly("k", "l");
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    @org.junit.jupiter.api.Test
    void removeAll_should_return_instance_when_no_intersection() {
        // Arrange
        Tested base = Tested.of("a", "b", "c");
        Set<String> disjoined = Set.of("1", "2", "3");
        List<String> empty = List.of();

        // Act
        Tested resultDisjoined = base.removeAll(disjoined);
        Tested resultEmpty = base.removeAll(empty);

        // Assert
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(resultDisjoined).as("disjoined").isSameAs(base);
        softly.assertThat(resultEmpty).as("empty").isSameAs(base);
        softly.assertThat(base).as("immutable content").containsExactly("a", "b", "c");
        softly.assertAll();
    }

    @Test
    void removeAll_should_return_new_instance_when_intersect() {
        // Arrange
        Tested base = Tested.of("k", "o", "o", "l");
        Tested canary = Tested.of("k", "o", "o", "l");
        Set<String> removal = Set.of("o", "k");

        // Act
        Tested result = base.removeAll(removal);

        // Assert
        assertThat(result).containsExactly("l");
        assertThat(base).as("immutable content").isEqualTo(canary);
    }

    /**
     * implémentation "basique" pour pouvoir tester tous les comportements abstraits,
     * <strong>sans</strong> s'appuyer sur {@link AbstractImmutableList}.
     */
    private static class Tested extends AbstractImmutableCollection<String, List<String>, Tested> {

        private Tested(List<String> inner) {
            super(inner, Tested::new, TypeDelegate.arrayList());
        }

        static Tested of(String... values) {
            return new Tested(List.of(values));
        }

        @Override
        public Tested add(String s) {
            return this; // no-op & not tested
        }
    }

    private static class ByBasic extends GroupedImmutables<BasicKey, String, Tested> {
        public ByBasic(Map<BasicKey, Tested> groups) {
            super(groups, Tested.of());
        }
    }
}
