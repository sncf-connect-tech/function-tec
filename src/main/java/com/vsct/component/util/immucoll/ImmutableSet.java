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

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Spécialisation d'{@link ImmutableCollection} pour les {@code Set}.
 *
 * @since 1.0
 */
public interface ImmutableSet<ELEM> extends ImmutableCollection<ELEM> {

    static <E, W extends Set<E>, S extends ImmutableSet<E>> SetBuilder<E, W, S> builder(
            Function<? super W, S> constructor,
            TypeDelegate<E, ? extends W> typeDelegate) {

        return new SetBuilder<>(constructor, typeDelegate);
    }

    static <E, W extends Set<E>, L extends ImmutableSet<E>> Collector<E, ?, L> toImmutableSet(
            Function<? super W, L> constructor,
            TypeDelegate<E, ? extends W> typeDelegate) {

        return new ImmCollector<>(constructor, typeDelegate);
    }

    /**
     *
     * @param <E> type d'élément accumulé
     * @param <W> type de {@code Set} représenté
     * @param <S> type d'ensemble immuable produit
     */
    class SetBuilder<E, W extends Set<E>, S extends ImmutableSet<E>> extends Builder<E, W, S> {
        protected SetBuilder(Function<? super W, S> constructor,
                             TypeDelegate<E, ? extends W> typeDelegate) {
            super(constructor, typeDelegate);
        }

        // de même taille et tous les éléments de l'un se trouvent dans l'autre
        @Override
        boolean sameContent(W a, S b) {
            if (a.size() != b.size()) return false;
            return b.containsAll(a);
        }
    }
}
