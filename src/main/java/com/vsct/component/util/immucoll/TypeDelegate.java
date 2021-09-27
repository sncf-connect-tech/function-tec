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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;

/**
 * Permet d'abstraire le type de {@code Collection} voulu dans {@link AbstractImmutableCollection}.
 *
 * @param <ELEM> type d'éléments manipulés
 * @param <TYPE> type précis de {@code Collection} désiré
 *
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public interface TypeDelegate<ELEM, TYPE extends Collection<ELEM>> {

    Collector<ELEM, ?, ? extends TYPE> collector();

    TYPE newHolder();

    default void requireNonNull(Object challenge, String message) {
        if (challenge == null) {
            throw new IllegalArgumentException(message);
        }
    }

    default void requireNoNull(Collection<? extends ELEM> challenges, String context) {
        requireNonNull(challenges, context + " must exist");
        if (challenges.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("no null allowed in " + context);
        }
    }

    static <E> TypeDelegate<E, List<E>> arrayList() {
        return (TypeDelegate<E, List<E>>) KnownTypeDelegates.ARRAY_LIST;
    }

    static <E> TypeDelegate<E, Set<E>> hashSet() {
        return (TypeDelegate<E, Set<E>>) KnownTypeDelegates.HASH_SET;
    }

    static <E extends Comparable<? super E>> TypeDelegate<E, NavigableSet<E>> treeSet() {
        return treeSet(E::compareTo);
    }

    static <E> TypeDelegate<E, NavigableSet<E>> treeSet(Comparator<E> order) {
        return new KnownTypeDelegates.TreeSetDelegate<>(order);
    }
}
