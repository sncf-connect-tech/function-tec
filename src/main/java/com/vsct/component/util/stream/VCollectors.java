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

package com.vsct.component.util.stream;

import com.vsct.component.util.Boundaries;
import com.vsct.component.util.IntBoundaries;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toMap;

public class VCollectors {
    private VCollectors() {
        // non instanciable
    }

    /**
     * Groupement d'éléments par clef, avec un "aplatissement" de la liste résultat.
     *
     * @param classifier      foncteur de récupération de la clef
     * @param valueProjection foncteur de récupération de la valeur
     * @param newValueHolder  création de la collection fusionnée
     * @param <I>             type de donnée collectée
     * @param <K>             type de la clef
     * @param <V>             type de la valeur
     * @param <C>             spécialisation du conteneur des valeurs
     * @return collecteur produisant une {@code HashMap}
     */
    public static <I, K, V, C extends Collection<V>> Collector<I, ?, Map<K, C>> groupingByAndFlatten(
            final Function<I, K> classifier,
            final Function<I, C> valueProjection,
            final Supplier<? extends C> newValueHolder) {
        return toMap(classifier, valueProjection, (a, b) -> merge(a, b, newValueHolder));
    }

    /**
     * Ordre naturel.
     *
     * @param <T> type de la donnée à traiter
     * @return nouvelle intance de collecteur
     */
    public static <T extends Comparable<? super T>> Collector<T, ?, Optional<Boundaries<T>>> toBoundaries() {
        return new BoundariesCollector<>(T::compareTo);
    }

    /**
     * Ordre spécifié.
     *
     * @param comparator sens de tri
     * @param <T>        type de la donnée à traiter
     * @return nouvelle instance de collecteur
     */
    public static <T> Collector<T, ?, Optional<Boundaries<T>>> toBoundaries(Comparator<? super T> comparator) {
        return new BoundariesCollector<>(comparator);
    }
    public static Collector<Integer, ?, Optional<IntBoundaries>> toIntBoundaries() {
        return new IntBoundariesCollector();
    }

    private static <E, C extends Collection<E>> C merge(C a, C b, Supplier<? extends C> spawn) {
        final C m = spawn.get();
        m.addAll(a);
        m.addAll(b);
        return m;
    }
}
