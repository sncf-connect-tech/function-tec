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

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/**
 * Contient le minumum et maximum d'un ensemble de valeurs (prévu pour {@link java.util.stream.Stream#reduce}).
 * <p/>
 * L'implémentation est immuable, chaque appel à {@link #accumulate(Object)} produisant une nouvelle instance
 * si une des valeurs change.
 * L'« élément neutre » présente deux valeurs {@code null} (comme un {@code Optional} vide),
 * mais {@code accumulate} ne traite PAS une valeur {@code null} (<em>no-op</em>).
 * <p/>
 * 2 formes d'utilisation <ol>
 *   <li>encapsuler chaque valeur dans une instance de cette classe et les combiner : <pre>stream.map(elt -> Boundaries.single(elt, comparator))
 *   .reduce(Boundaries::merge)</pre></li>
 *   <li>réduire le résultat directement, à partir de l'élément neutre :
 *   <pre>stream.reduce(Boundaries.empty(comparator), Boundaries::accumulate, Boundaries::merge)</pre></li>
 * </ol>
 * <p/>
 * La <em>factory-method</em> {@link #of} permet de suivre la 2e forme, en "allégeant" la rédaction et en ne récupérant
 * PAS l'élément neutre dans le cas d'un {@code Stream} vide.
 * <p/>
 * La <em>factory-method</em> {@link com.vsct.component.util.stream.VCollectors#toBoundaries} produit
 * le même comportement, mais en utilisant {@code Stream.collect} au lieu de {@code Stream.reduce}
 *
 * @param <E> type de donnée manipulée
 * @since 1.0
 */
public class Boundaries<E> {
    private final Comparator<? super E> comparator;
    private final E max, min;

    // ======================================================================================
    //
    // ctors
    //
    // ======================================================================================

    Boundaries(Comparator<? super E> comparator, E min, E max) {
        this.comparator = Objects.requireNonNull(comparator, "comparator must exist");
        this.max = max;
        this.min = min;
    }

    public static <T extends Comparable<? super T>> Boundaries<T> empty() {
        return new Boundaries<>(T::compareTo, null, null);
    }

    public static <T> Boundaries<T> empty(Comparator<? super T> comparator) {
        return new Boundaries<>(comparator, null, null);
    }

    /**
     * Abstrait la complexité rédactionnelle de {@code Stream<T>#reduce(R, BiFunction<R, T, R>, BinaryOperator<R>)}
     * et suit le modèle de {@link Stream#max(Comparator)} :
     * détermine une valeur si possible, mais renvoit "vide" s'il n'y avait aucune donnée.
     * <p/>
     * &lt;!> Cette méthode applique une opération terminale sur le flux, elle le <strong>CONSOMME</strong> donc &lt;!>
     *
     * @param elems      flux de données à traiter
     * @param comparator définition de l'ordre
     * @param <T>        type de la donnée
     * @return les bornes si possible, l'{@code Optional} vide sinon.
     */
    public static <T> Optional<Boundaries<T>> of(Stream<? extends T> elems, Comparator<? super T> comparator) {
        final Boundaries<T> identity = empty(comparator);
        final Boundaries<T> b = elems.reduce(identity, Boundaries::accumulate, Boundaries::merge);

        return b == identity ? Optional.empty() : Optional.of(b);
    }

    /**
     * Abstrait la complexité rédactionnelle de {@code Stream<T>#reduce(R, BiFunction<R, T, R>, BinaryOperator<R>)}
     * et suit le modèle de {@link Stream#max(Comparator)} :
     * détermine une valeur si possible, mais renvoit "vide" s'il n'y avait aucune donnée.
     * <p/>
     * &lt;!> Cette méthode applique une opération terminale sur le flux, elle le <strong>CONSOMME</strong> donc &lt;!>
     *
     * @param elems      flux de données à traiter
     * @param <T>        type de la donnée naturellement ordonnée
     * @return les bornes si possible, l'{@code Optional} vide sinon.
     */
    public static <T extends Comparable<? super T>> Optional<Boundaries<T>> ofNaturalOrder(Stream<? extends T> elems) {
        return of(elems, T::compareTo);
    }

    public static <T extends Comparable<? super T>> Boundaries<T> single(T elem) {
        return new Boundaries<>(T::compareTo, elem, elem);
    }

    public static <T> Boundaries<T> single(T elem, Comparator<? super T> comparator) {
        return new Boundaries<>(comparator, elem, elem);
    }

    @Deprecated
    public static <T extends Comparable<? super T>> Boundaries<T> singleNaturalOrder(T elem) {
        return new Boundaries<>(T::compareTo, elem, elem);
    }

    // ======================================================================================
    //
    // public methods
    //
    // ======================================================================================

    public Boundaries<E> accumulate(E candidate) {
        if (candidate != null) {
            if (isEmpty()) {
                return Boundaries.single(candidate, comparator);
            } else if (lowerMin(candidate)) {
                return new Boundaries<>(comparator, candidate, max);
            } else if (greaterMax(candidate)) {
                // "+else+ if" car impossible d'être à la fois inférieur au min et supérieur au max
                return new Boundaries<>(comparator, min, candidate);
            }
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Boundaries) {
            Boundaries<?> other = (Boundaries<?>) o;
            return other.comparator.equals(comparator)
                    && Objects.equals(other.max, max)
                    && Objects.equals(other.min, min);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(comparator, min, max);
    }

    public boolean isEmpty() {
        return min == null;
    }

    /**
     * Renvoie {@code true} si l'instance contient deux valeurs distinctes.
     * Faux pour une instance vide.
     */
    public boolean isRange() {
        return !isSingle();
    }

    /**
     * Renvoie {@code true} si l'instance ne contient qu'une seule valeur.
     * Vrai pour une instance vide.
     */
    public boolean isSingle() {
        return isEmpty() || min.equals(max);
    }

    public <O> Boundaries<O> map(Function<? super E, ? extends O> mapper, Comparator<? super O> comparator) {
        if (isEmpty()) {
            return Boundaries.empty(comparator);
        } else if (isSingle()) {
            return Boundaries.single(mapper.apply(min), comparator);
        } else {
            return mapBoth(mapper, comparator);
        }
    }

    public <O extends Comparable<? super O>> Boundaries<O> mapNaturalOrder(Function<? super E, ? extends O> mapper) {
        return map(mapper, O::compareTo);
    }

    /**
     * transformation des éléments vers un type primitif
     */
    public IntBoundaries mapToInt(ToIntFunction<? super E> mapper) {
        if (isEmpty()) {
            return IntBoundaries.empty();
        } else {
            int mappedMin = mapper.applyAsInt(min);
            int mappedMax = mapper.applyAsInt(max);
            if (mappedMin > mappedMax) {
                // reversed
                return new IntBoundaries(mappedMax, mappedMin);
            } else {
                return new IntBoundaries(mappedMin, mappedMax);
            }
        }
    }

    /**
     * &lt;!> AUCUN contrôle sur l'identité des comparateur &lt;!>
     * (car la méthode {@code equals} renvoit {@code false} sur des lambdas identiques,
     * on ne peut donc pas se baser dessus...)
     * Il en résulte que la fusion de deux instances portant sur le même type mais triées différemment,
     * bien que déterministe, n'est pas identique :
     * <pre><code>
     *   Boundaries&lt;String> b1 = singleNaturalOrder("b").accumulate("xxxx"); // [b,xxxx]
     *   Boundaries&lt;String> b2 = single("aaa", (str1, str2) -> str2.length() - str1.length())
     *      .accumulate("pp"); // ["aaa", "pp"]
     *
     *   Boundaries&lt;String> m1 = b1.merge(b2); // [aaa, xxxx] & ordre naturel
     *   Boundaries&lt;String> m2 = b2.merge(b1); // [xxxx, b] & ordre de taille décroissante
     * </code></pre>
     *
     * @param other instance à combiner
     * @return fusion, ne créant une nouvelle instance que si c'est inévitable
     */
    public Boundaries<E> merge(Boundaries<E> other) {
        if (isEmpty()) {
            return other;
        } else if (!other.isEmpty()) {
            final boolean gt = greaterMax(other.max);
            final boolean lt = lowerMin(other.min);

            if (gt && lt) {
                return other;
            } else if (gt ^ lt) {
                final E newMin = lt ? other.min : min;
                final E newMax = gt ? other.max : max;
                return new Boundaries<>(comparator, newMin, newMax);
            }
        }
        return this;
    }

    /**
     * Accès aux deux bornes, UNIQUEMENT si l'instance représente quelque chose.
     * L'argument ne sera jamais appelé avec des valeurs {@code null}
     *
     * @param visitor traitement externe
     * @return instance, pour chaînage
     */
    public Boundaries<E> onBoth(BiConsumer<? super E, ? super E> visitor) {
        if (!isEmpty()) {
            visitor.accept(min, max);
        }
        return this;
    }

    /**
     * Accès à la borne supérieure, UNIQUEMENT si l'instance représente quelque chose.
     * L'argument ne sera jamais appelé avec {@code null}
     *
     * @param visitor traitement externe
     * @return instance, pour chaînage
     *
     * @see #getMax() pour obtenir la valeur (potentiellement {@code null}
     */
    public Boundaries<E> onMax(Consumer<? super E> visitor) {
        if (max != null) {
            visitor.accept(max);
        }
        return this;
    }

    /**
     * Accès à la borne inférieure, UNIQUEMENT si l'instance représente quelque chose.
     * L'argument ne sera jamais appelé avec {@code null}
     *
     * @param visitor traitement externe
     * @return instance, pour chaînage
     *
     * @see #getMin() pour obtenir la valeur (potentiellement {@code null}
     */
    public Boundaries<E> onMin(Consumer<? super E> visitor) {
        if (min != null) {
            visitor.accept(min);
        }
        return this;
    }

    public Boundaries<E> reverse() {
        return new Boundaries<>(comparator.reversed(), max, min);
    }

    @Override
    public String toString() {
        return String.format("(%s,%s) by %s", min, max, comparator);
    }

    // ======================================================================================
    //
    // retricted-visibility methods
    //
    // ======================================================================================

    private boolean greaterMax(E candidate) {
        return comparator.compare(candidate, max) > 0;
    }
    private boolean lowerMin(E candidate) {
        return comparator.compare(candidate, min) < 0;
    }

    private <O> Boundaries<O> mapBoth(Function<? super E, ? extends O> mapper, Comparator<? super O> comparator) {
        O mappedMin = mapper.apply(min);
        O mappedMax = mapper.apply(max);
        if (comparator.compare(mappedMin, mappedMax) > 0) {
            // reversed
            return new Boundaries<>(comparator, mappedMax, mappedMin);
        } else {
            return new Boundaries<>(comparator, mappedMin, mappedMax);
        }
    }

    // ======================================================================================
    //
    // accessors
    //
    // ======================================================================================

    public E getMax() { return max; }
    public E getMin() { return min; }
}
