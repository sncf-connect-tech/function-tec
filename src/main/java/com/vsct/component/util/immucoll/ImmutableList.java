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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Spécialisation d'{@link ImmutableCollection} pour les {@code List}.
 *
 * @since 1.0
 */
public interface ImmutableList<ELEM> extends ImmutableCollection<ELEM> {

    static <E, L extends ImmutableList<E>> ListBuilder<E, L> builder(Function<List<E>, L> constructor) {
        return new ListBuilder<>(constructor);
    }

    static <E, L extends ImmutableList<E>> Collector<E, ?, L> toImmutableList(Function<List<E>, L> constructor) {
        return new ImmCollector<>(constructor, TypeDelegate.arrayList());
    }

    /**
     * Méthode "dangereuse" (comme {@link java.util.Optional#get}) qui renvoit le premier élément.
     *
     * @return premier élément, s'il existe
     * @throws IndexOutOfBoundsException s'il n'existe pas
     */
    ELEM first();

    /**
     * Méthode "dangereuse" (comme {@link java.util.Optional#get}) qui renvoit le dernier élément.
     *
     * @return dernier élément, s'il existe
     * @throws IndexOutOfBoundsException s'il n'existe pas
     */
    ELEM last();

    /**
     *
     * @param <E> type d'élément accumulé
     * @param <L> type de liste immuable produit
     */
    class ListBuilder<E, L extends ImmutableList<E>> extends Builder<E, List<E>, L> {
        protected ListBuilder(Function<List<E>, L> constructor) {
            super(constructor, TypeDelegate.arrayList());
        }

        @Override
        boolean sameContent(List<E> a, L b) {
            Iterator<E> iteB = b.iterator();
            for (E elt : a) {
                if (!iteB.hasNext() || !elt.equals(iteB.next())) {
                    return false;
                }
            }
            return !iteB.hasNext();
        }
    }
}
