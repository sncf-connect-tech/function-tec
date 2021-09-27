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

import com.vsct.component.util.VCollections.LookupFailure;
import com.vsct.component.util.ctrl.Either;
import com.vsct.testing.data.Direction;
import com.vsct.testing.data.Dist;
import com.vsct.testing.data.Heading;
import com.vsct.testing.data.Strings;
import com.vsct.testing.tool.VAssertions;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VCollectionsTest {

    private static final String FIRST = "foo";

    @Test
    void collect_shouldReturnMapped_withCollectionChoice() {
        final Queue<String> coll = new LinkedList<>();
        coll.add("parle");
        coll.add("moi");
        coll.add("de");
        coll.add("tes");
        coll.add("rêves");
        final UnaryOperator<String> lastChar = in -> in.substring(in.length() - 1);

        final SortedSet<String> actual = VCollections.collect(coll, lastChar, TreeSet::new);

        assertThat(actual).containsExactly("e", "i", "s");
    }

    @Test
    void collectList_shouldReturnEmpty_whenNull() {
        final Collection<String> coll = null;

        final List<Integer> actual = VCollections.collectList(coll, String::length);

        assertThat(actual).isEmpty();
    }

    @Test
    void collectList_shouldReturnMapped() {
        final Collection<String> coll = new TreeSet<>();
        coll.add("ichi");
        coll.add("ni");
        coll.add("san");

        final List<Integer> actual = VCollections.collectList(coll, String::length);

        assertThat(actual).containsExactly(4, 2, 3);
    }

    @Test
    void collectWith_shouldCollectMapped_withCollectorChoice() {
        final Set<String> coll = new TreeSet<>();
        coll.add("parle");
        coll.add("moi");
        coll.add("de");
        coll.add("tes");
        coll.add("rêves");
        final UnaryOperator<String> firstChar = in -> in.substring(0, 1);

        final String actual = VCollections.collectWith(coll, firstChar, Collectors.joining());

        assertThat(actual).isEqualTo("dmprt");
    }

    @Test
    void flatStream_should_handle_no_arg() {
        assertThat(VCollections.flatStream()).isEmpty();
    }

    @Test
    void flatStream_should_handle_single_arg() {
        List<String> single = List.of("a");
        assertThat(VCollections.flatStream(single)).containsExactly("a");
    }

    @Test
    void flatStream_should_handle_all_null() {
        assertThat(VCollections.flatStream(null, null, null)).isEmpty();
    }

    @Test
    void flatStream_should_handle_null_first() {
        List<String> last = List.of("a");
        assertThat(VCollections.flatStream(null, last)).containsExactly("a");
    }

    @Test
    void flatStream_should_handle_polymorphism() {
        final Queue<Integer> first = new LinkedList<>();
        first.offer(1);
        first.offer(2);
        final Set<Double> second = Set.of(3d);
        final List<Float> third = List.of(4f, 5f);

        final Stream<Number> allNums = VCollections.flatStream(first, second, third);

        assertThat(allNums).containsExactly(1, 2, 3d, 4f, 5f);
    }

    @Test
    void firstOrExplain_shouldReturnNullContainer_whenNullCollection() {
        final Collection<String> coll = null;

        final Either<LookupFailure, String> first = VCollections.firstOrExplain(coll);

        VAssertions.assertThat(first).left().isEqualTo(LookupFailure.NULL_CONTAINER);
    }

    @Test
    void firstOrExplain_shouldReturnEmptyContainer_whenEmptyCollection() {
        final Collection<String> coll = new ArrayList<>(2);

        final Either<LookupFailure, String> first = VCollections.firstOrExplain(coll);

        VAssertions.assertThat((Either<? extends LookupFailure, ?>) first).left().isSameAs(LookupFailure.EMPTY_CONTAINER);
    }

    @Test
    void firstOrExplain_shouldReturnNullElt_whenFirstElementIsNull() {
        final Collection<String> coll = new ArrayList<>(2);
        coll.add(null);
        coll.add("not-null");

        final Either<LookupFailure, String> first = VCollections.firstOrExplain(coll);

        VAssertions.assertThat((Either<? extends LookupFailure, ?>) first).left().isSameAs(LookupFailure.NULL_ELT);
    }

    @Test
    void firstOrExplain_shouldReturnFirst_whenList() {
        final Collection<String> coll = new ArrayList<>(2);
        coll.add(FIRST);
        coll.add("not-null");

        final Either<LookupFailure, String> first = VCollections.firstOrExplain(coll);

        VAssertions.assertThat((Either<?, String>) first).right().isSameAs(FIRST);
    }

    @Test
    void firstOrExplain_shouldReturnFirst_whenSorted() {
        final TreeSet<String> coll = mock(TreeSet.class);
        when(coll.first()).thenReturn(FIRST);

        final Either<LookupFailure, String> first = VCollections.firstOrExplain(coll);

        VAssertions.assertThat((Either<?, String>) first).right().isSameAs(FIRST);
    }

    @Test
    void firstOrExplain_shouldReturnPeeked_whenQueue() {
        final Deque<String> coll = mock(Deque.class);
        when(coll.peek()).thenReturn(FIRST);

        final Either<LookupFailure, String> first = VCollections.firstOrExplain(coll);

        VAssertions.assertThat((Either<?, String>) first).right().isSameAs(FIRST);
    }

    @Test
    void firstOrExplain_shouldReturnIterated_whenOtherCollection() {
        final Collection<String> coll = new Custom();

        final Either<LookupFailure, String> first = VCollections.firstOrExplain(coll);

        VAssertions.assertThat((Either<?, String>) first).right().isSameAs(FIRST);
    }

    @Test
    void firstThat_shouldReturnEmpty_whenNullCollection() {
        final Collection<String> coll = null;

        final Optional<String> first = VCollections.firstThat(coll, Objects::nonNull);

        assertThat(first).isEmpty();
    }

    @Test
    void firstThat_shouldFail_whenNullPredicate() {
        final Collection<String> coll = asList("a", "b");

        final Throwable e = catchThrowable(() -> VCollections.firstThat(coll, null));

        assertThat(e).isInstanceOf(NullPointerException.class)
                .hasMessage("filtering without condition!");
    }

    @Test
    void firstThat_shouldReturnEmpty_whenEmptyCollection() {
        final Collection<String> coll = new ArrayList<>();

        final Optional<String> first = VCollections.firstThat(coll, o -> true);

        assertThat(first).isEmpty();
    }

    @Test
    void firstThat_shouldReturnEmpty_whenNoMatchInCollection() {
        final Collection<String> coll = asList("a", "bb");

        final Optional<String> first = VCollections.firstThat(coll, s -> s.length() == 3);

        assertThat(first).isEmpty();
    }

    @Test
    void firstThat_shouldReturnElement_whenMatchInCollection() {
        final Heading expected = new Heading(2d, "m", Direction.N);
        final Collection<Heading> coll = asList(new Heading(0.1, "mi", Direction.W), expected);
        final Predicate<Dist> meter = d -> "m".equals(d.getDimension());

        final Optional<Heading> first = VCollections.firstThat(coll, meter);

        assertThat(first).hasValue(expected);
    }

    @Test
    void firstThatOrExplain_shouldReturnNullContainer_whenNullCollection() {
        final Collection<String> coll = null;

        final Either<LookupFailure, String> first = VCollections.firstThatOrExplain(coll, Objects::nonNull);

        VAssertions.assertThat((Either<? extends LookupFailure, ?>) first).left().isSameAs(LookupFailure.NULL_CONTAINER);
    }

    @Test
    void firstThatOrExplain_shouldFail_whenNullPredicate() {
        final Collection<String> coll = asList("a", "b");

        final Throwable e = catchThrowable(() -> VCollections.firstThatOrExplain(coll, null));

        assertThat(e).isInstanceOf(NullPointerException.class)
                .hasMessage("filtering without condition!");
    }

    @Test
    void firstThatOrExplain_shouldReturnEmptyContainer_whenEmptyCollection() {
        final Collection<String> coll = new ArrayList<>();

        final Either<LookupFailure, String> first = VCollections.firstThatOrExplain(coll, o -> true);

        VAssertions.assertThat((Either<? extends LookupFailure, ?>) first).left().isSameAs(LookupFailure.EMPTY_CONTAINER);
    }

    @Test
    void firstThatOrExplain_shouldReturnNoMatchingElt_whenNoMatchInCollection() {
        final Collection<String> coll = asList("a", "bb");

        final Either<LookupFailure, String> first = VCollections.firstThatOrExplain(coll, s -> s.length() == 3);

        VAssertions.assertThat((Either<? extends LookupFailure, ?>) first).left().isSameAs(LookupFailure.NO_MATCHING_ELT);
    }

    @Test
    void firstThatOrExplain_shouldReturnElement_whenMatchInCollection() {
        final Heading expected = new Heading(2d, "m", Direction.N);
        final Collection<Heading> coll = asList(new Heading(0.1, "mi", Direction.W), expected);
        final Predicate<Dist> meter = d -> "m".equals(d.getDimension());

        final Either<LookupFailure, Heading> first = VCollections.firstThatOrExplain(coll, meter);

        VAssertions.assertThat((Either<?, Heading>) first).right().isSameAs(expected);
    }

    @Test
    void select_shouldReturnEmpty_whenNullCollection() {
        final List<String> selected = VCollections.select(null, any -> true);

        assertThat(selected).hasSize(0);
    }

    @Test
    void select_shouldReturnEmpty_whenNoMatchingElement() {
        final Set<String> input = new HashSet<>();
        input.add("a");
        input.add("b");
        input.add("0");

        final List<String> selected = VCollections.select(input, any -> false);

        assertThat(selected).hasSize(0);
    }

    @Test
    void select_shouldRetainMatchingElements() {
        final Set<String> input = new LinkedHashSet<>(); // pour que l'ordre d'itération soit prévisible
        input.add("a");
        input.add("b");
        input.add("0");

        final List<String> selected = VCollections.select(input, str -> str.charAt(0) < 'b');

        assertThat(selected).containsExactly("a", "0");
    }

    @Test
    void select_shouldUseGivenCollection_onMatchingElements() {
        final List<String> input = new ArrayList<>();
        input.add("a");
        input.add("b");
        input.add("c");
        input.add("c");
        input.add("a");

        final Set<String> selected = VCollections.select(input, str -> str.charAt(0) > 'a', TreeSet::new);

        assertThat(selected).containsExactly("b", "c");
    }

    @Test
    void toConcatStream_should_handle_both_null() {
        assertThat(VCollections.toConcatStream(null, null)).isEmpty();
    }

    @Test
    void toConcatStream_should_handle_null_left() {
        List<String> right = List.of("a");
        assertThat(VCollections.toConcatStream(null, right)).containsExactly("a");
    }

    @Test
    void toConcatStream_should_handle_null_right() {
        List<String> left = List.of("a");
        assertThat(VCollections.toConcatStream(left, null)).containsExactly("a");
    }

    @Test
    void toConcatStream_should_handle_polymorphism() {
        final Queue<Integer> left = new LinkedList<>();
        left.offer(1);
        left.offer(2);
        final HashSet<Double> right =  new HashSet<>();
        right.add(3.0d);

        final Stream<Number> allNums = VCollections.toConcatStream(left, right);

        assertThat(allNums).containsExactly(1, 2, 3.0d);
    }

    @Test
    void toStream_Immutable_should_return_empty_on_null() {
        // Arrange
        Strings candidate = null;

        // Act
        Stream<String> result = VCollections.toStream(candidate);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void toStream_Immutable_should_return_elements_on_non_null() {
        // Arrange
        Strings candidate = Strings.of("a", "b");

        // Act
        Stream<String> result = VCollections.toStream(candidate);

        // Assert
        assertThat(result).containsExactly("a", "b");
    }

    @Test
    void union_should_add_all_in_given_collection() {
        Set<String> left = new TreeSet<>();
        left.add("u");
        left.add("o");
        List<String> right = List.of("i", "!");

        List<String> out = new ArrayList<>();
        out.add("z");
        out.add("b");

        final List<String> salute = VCollections.union(left, right, () -> out);

        assertThat(salute).containsExactly("z", "b", "o", "u", "i", "!");
    }

    @Test
    void union_should_add_all_in_new_list() {
        final Queue<Integer> left = new LinkedList<>();
        left.offer(1);
        left.offer(2);
        final HashSet<Double> right =  new HashSet<>();
        right.add(3.0d);

        final List<Number> allNums = VCollections.union(left, right, ArrayList::new);

        assertThat(allNums).containsExactly(1, 2, 3.0d);
    }

    private static class Custom extends AbstractCollection<String> {
        @Override
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                int idx = 0;

                @Override
                public boolean hasNext() {
                    return idx < 2;
                }

                @Override
                public String next() {
                    switch (idx++) {
                        case 0:
                            return FIRST;
                        case 1:
                            return "second";
                        default:
                            throw new IndexOutOfBoundsException();
                    }
                }
            };
        }

        @Override
        public int size() {
            return 2;
        }
    }
}