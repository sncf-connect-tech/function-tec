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
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Définit les comportements communs pour un <em>Value Object</em> qui représente
 * <strong>uniquement</strong> une collection immuable d'éléments.
 *
 * <p>Notez que c'est la collection en tant que contenant qui est protégée contre
 * l'écriture.
 * Il n'y a pas de magie : si elle contient des éléments qui admettent une modification,
 * elle pourra être "altérée".
 *
 * @param <ELEM> type d'élément collectés
 *
 * @see <a href="https://williamdurand.fr/2013/06/03/object-calisthenics/#4-first-class-collections">calisthenic #4</a>
 * @since 1.0
 */
public interface ImmutableCollection<ELEM> extends Iterable<ELEM> {

    /**
     * Pour retourner dans le "monde des {@code Collection}", utile aux frontières du modèle
     * (e.g. les assertions).
     *
     * @return une <em>shallow-copy</em> de la collection représentée par cette instance
     */
    default List<ELEM> asList() {
        return stream().collect(toList());
    }

    /**
     * Pour retourner dans le "monde des {@code Collection}", utile aux frontières du modèle
     * (e.g. les assertions).
     *
     * @return une <em>shallow-copy</em> de la collection représentée par cette instance,
     * sans doublon ni garantie d'ordre
     */
    default Set<ELEM> asSet() {
        return stream().collect(toSet());
    }

    default boolean contains(ELEM challenge) {
        if (challenge == null) {
            // comme on refuse de stocker `null` (cf. Builder, ci-après),
            // on sait qu'on ne le trouvera pas
            return false;
        }
        return stream().anyMatch(challenge::equals);
    }

    default boolean containsAll(Iterable<ELEM> challenge) {
        for (ELEM elem : challenge) {
            if (!contains(elem)) {
                return false;
            }
        }
        return true;
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    Stream<ELEM> stream();

    int size();

    /**
     * Classe "mutable" permettant de construire une instance <em>immutable</em> itérativement
     *
     * @param <E> type de la donnée listée
     * @param <T> type de {@code Collection} qu'on encapsule
     * @param <C> type concret d'{@code ImmutableCollection} voulue
     */
    class Builder<E, T extends Collection<E>, C extends ImmutableCollection<E>> {
        private final Function<? super T, C> constructor;
        private final T inner;
        private final TypeDelegate<E, ? extends T> typeDelegate;

        public Builder(Function<? super T, C> constructor, TypeDelegate<E, ? extends T> typeDelegate) {
            this.constructor = constructor;
            this.typeDelegate = typeDelegate;
            this.inner = typeDelegate.newHolder();
        }

        public void add(E elem) {
            typeDelegate.requireNonNull(elem, "element to add must exist");
            inner.add(elem);
        }

        public void addAll(Collection<? extends E> elems) {
            typeDelegate.requireNoNull(elems, "elements to add");
            inner.addAll(elems);
        }

        public void addAll(C holder) {
            typeDelegate.requireNonNull(holder, "holder of elements to add must exist");
            inner.addAll(holder.asList());
        }

        public C build() {
            // copie qui "défend" le `Builder` !
            // (contre des saloperies que pourrait commettre `constructor`)
            final T copy = typeDelegate.newHolder();
            copy.addAll(inner);
            // vu qu'_in fine_, le super-constructeur qui se cache derrière cette instruction
            // va refaire sa propre copie
            return constructor.apply(copy);
        }

        public boolean containsSame(C otherImmutable) {
            return sameContent(inner, otherImmutable);
        }

        public Builder<E, T, C> merge(Builder<E, T, C> other) {
            typeDelegate.requireNonNull(other, "builder to merge with must exist");
            this.inner.addAll(other.inner);
            return this;
        }

        boolean sameContent(T a, C b) {
            throw new UnsupportedOperationException("cannot compare content");
        }
    }

    /**
     * {@code Collector} qui délègue à {@link Builder} l'implémentation de ses méthodes contractuelles.
     *
     * @param <E> type de la donnée collectée
     * @param <T> type de {@code Collection} représentée
     * @param <C> type concret d'{@code ImmutableCollection} voulue au final
     */
    class ImmCollector<E, T extends Collection<E>, C extends ImmutableCollection<E>> implements Collector<E, Builder<E, T, C>, C> {

        private final Function<? super T, C> constructor;
        private final TypeDelegate<E, ? extends T> typeDelegate;

        public ImmCollector(Function<? super T, C> constructor, TypeDelegate<E, ? extends T> typeDelegate) {
            this.constructor = constructor;
            this.typeDelegate = typeDelegate;
        }

        @Override
        public Supplier<Builder<E, T, C>> supplier() {
            return () -> new Builder<>(constructor, typeDelegate);
        }
        @Override
        public BiConsumer<Builder<E, T, C>, E> accumulator() {
            return Builder::add;
        }
        @Override
        public BinaryOperator<Builder<E, T, C>> combiner() {
            return Builder::merge;
        }
        @Override
        public Function<Builder<E, T, C>, C> finisher() {
            return Builder::build;
        }
        @Override
        public Set<Characteristics> characteristics() {
            return Set.of();
        }
    }
}
