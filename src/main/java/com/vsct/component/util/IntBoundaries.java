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
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Version spécialisée de {@link Boundaries}, adaptée aux entiers primitifs.<br/>
 * Par construction, <strong>seul l'ordre naturel est possible</strong> : le mininum sera <strong>toujours</strong>
 * numériquement inférieur au maximum.
 * <p/>
 * L'« élément neutre » présente deux valeurs {@code 0} (comme un {@code OptionalInt} vide) et est identifié
 * formellement, pour pouvoir le reconnaître d'un {@code single(0)}.
 * <p/>
 * 3 formes d'utilisation pour des flux :<ol>
 *   <li>encapsuler chaque valeur dans une instance de cette classe et les combiner :<ul>
 *     <li>avec des {@code int} <pre>intStream.mapToObj(IntBoundaries::single)
 *   .reduce(IntBoundaries::merge)</pre></li>
 *     <li>avec des {@code Integer} <pre>stream.filter(Objects::nonNull)
 *   .map(IntBoundaries::single)
 *   .reduce(IntBoudaries::merge)</pre></li>
 *   </ul></li>
 *   <li>(uniquement valable avec {@code Integer}) réduire le résultat directement, à partir de l'élément neutre
 *   <pre>stream.reduce(IntBoundaries.EMPTY, IntBoundaries::accumulateObj, IntBoundaries::merge)</pre></li>
 *   <li>(uniquement valable avec {@code int}) collecter le résultat, en utilisant la déclinaison mutable
 *   <pre>intStream.collect(IntBoundaries.Mutable::new, IntBoundaries.Mutable::udapte, IntBoundaries.Mutable::merge)</pre></li>
 * </ol>
 *
 * @since 1.0
 */
public class IntBoundaries {

    public static final IntBoundaries EMPTY = new IntBoundaries(0, 0);

    private final int max, min;

    // ======================================================================================
    //
    // ctors
    //
    // ======================================================================================

    IntBoundaries(int min, int max) {
        this.max = max;
        this.min = min;
    }

    public static IntBoundaries empty() {
        return EMPTY;
    }

    /**
     * Abstrait la complexité rédactionnelle de {@code IntStream#collect(Supplier<R>, ObjIntConsumer<R>, BiConsumer<R, R>)}
     * et renvoit "vide" s'il n'y avait aucune donnée.
     * <p/>
     * &lt;!> Cette méthode applique une opération terminale sur le flux, elle le <strong>CONSOMME</strong> donc &lt;!>
     *
     * @param elems      flux de données à traiter
     * @return les bornes si possible, l'{@code Optional} vide sinon.
     */
    public static Optional<IntBoundaries> of(IntStream elems) {
        return elems.collect(Mutable::new, Mutable::update, Mutable::merge)
                .finish();

    }

    /**
     * Abstrait la complexité rédactionnelle de {@code Stream<T>#reduce(R, BiFunction<R, T, R>, BinaryOperator<R>)}
     * et suit le modèle de {@link Stream#max(Comparator)} :
     * détermine une valeur si possible, mais renvoit "vide" s'il n'y avait aucune donnée.
     * <p/>
     * &lt;!> Cette méthode applique une opération terminale sur le flux, elle le <strong>CONSOMME</strong> donc &lt;!>
     *
     * @param elems      flux de données à traiter
     * @return les bornes si possible, l'{@code Optional} vide sinon.
     */
    public static Optional<IntBoundaries> of(Stream<Integer> elems) {
        final IntBoundaries b = elems.reduce(EMPTY, IntBoundaries::accumulateObj, IntBoundaries::merge);

        return b == EMPTY ? Optional.empty() : Optional.of(b);
    }

    public static IntBoundaries single(int elem) {
        return new IntBoundaries(elem, elem);
    }

    // ======================================================================================
    //
    // public methods
    //
    // ======================================================================================

    public IntBoundaries accumulate(int candidate) {
        if (isEmpty()) {
            return single(candidate);
        } else if (lowerMin(candidate)) {
            return new IntBoundaries(candidate, max);
        } else if (greaterMax(candidate)) {
            // "+else+ if" car impossible d'être à la fois inférieur au min et supérieur au max
            return new IntBoundaries(min, candidate);
        } else {
            return this;
        }
    }

    public IntBoundaries accumulateObj(Integer candidate) {
        return candidate == null ? this : accumulate(candidate);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IntBoundaries) {
            IntBoundaries other = (IntBoundaries) o;
            return other.max ==  max && other.min == min;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    /**
     * Renvoie {@code true} si l'instance contient deux valeurs distinctes.
     * Faux pour une instance vide.
     */
    public boolean isRange() {
        return min != max;
    }

    /**
     * Renvoie {@code true} si l'instance ne contient qu'une seule valeur.
     * Vrai pour une instance vide.
     */
    public boolean isSingle() {
        return min == max;
    }

    public IntBoundaries map(IntUnaryOperator mapper) {
        if (isEmpty()) {
            return this;
        } else if (isSingle()) {
            return IntBoundaries.single(mapper.applyAsInt(min));
        } else {
            int mappedMin = mapper.applyAsInt(min);
            int mappedMax = mapper.applyAsInt(max);
            if (mappedMin > mappedMax) {
                // reversed (e.g. multiply by negative)
                return new IntBoundaries(mappedMax, mappedMin);
            } else {
                return new IntBoundaries(mappedMin, mappedMax);
            }
        }
    }

    public <O> Boundaries<O> mapToObj(IntFunction<? extends O> mapper, Comparator<? super O> comparator) {
        if (isEmpty()) {
            return Boundaries.empty(comparator);
        } else if (isSingle()) {
            return Boundaries.single(mapper.apply(min), comparator);
        } else {
            return mapBothToObj(mapper, comparator);
        }
    }

    public <O extends Comparable<? super O>> Boundaries<O> mapToNaturalOrderObj(IntFunction<? extends O> mapper) {
        return mapToObj(mapper, O::compareTo);
    }

    /**
     * @param other instance à combiner
     * @return fusion, ne créant une nouvelle instance que si c'est inévitable
     */
    public IntBoundaries merge(IntBoundaries other) {
        if (isEmpty()) {
            return other;
        } else if (!other.isEmpty()) {
            final boolean gt = greaterMax(other.max);
            final boolean lt = lowerMin(other.min);

            if (gt && lt) {
                return other;
            } else if (gt ^ lt) {
                final int newMin = lt ? other.min : min;
                final int newMax = gt ? other.max : max;
                return new IntBoundaries(newMin, newMax);
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
    public IntBoundaries onBoth(BiConsumer<Integer, Integer> visitor) {
        if (!isEmpty()) {
            visitor.accept(min, max);
        }
        return this;
    }

    /**
     * Accès à la borne supérieure, UNIQUEMENT si l'instance représente quelque chose.
     * L'argument ne sera jamais appelé sur {@code EMPTY}
     *
     * @param visitor traitement externe
     * @return instance, pour chaînage
     *
     * @see #getMax() pour obtenir la valeur
     */
    public IntBoundaries onMax(IntConsumer visitor) {
        if (!isEmpty()) {
            visitor.accept(max);
        }
        return this;
    }

    /**
     * Accès à la borne inférieure, UNIQUEMENT si l'instance représente quelque chose.
     * L'argument ne sera jamais appelé sur {@code EMPTY}
     *
     * @param visitor traitement externe
     * @return instance, pour chaînage
     *
     * @see #getMin() pour obtenir la valeur
     */
    public IntBoundaries onMin(IntConsumer visitor) {
        if (!isEmpty()) {
            visitor.accept(min);
        }
        return this;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", min, max);
    }

    // ======================================================================================
    //
    // retricted-visibility methods
    //
    // ======================================================================================

    private boolean greaterMax(int candidate) {
        return max < candidate;
    }
    private boolean lowerMin(int candidate) {
        return candidate < min;
    }

    private <O> Boundaries<O> mapBothToObj(IntFunction<? extends O> mapper, Comparator<? super O> comparator) {
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

    public int getMax() { return max; }
    public int getMin() { return min; }

    // ======================================================================================
    //
    // inner types
    //
    // ======================================================================================

    /**
     * "Intermédiaire" obligatoire car {@code IntStream.collect} et {@code Stream.collect} doivent
     * altérer leur conteneur et {@code IntBoundaries} est <em>immutable</em>.
     */
    public static class Mutable {
        private IntBoundaries current = EMPTY;

        public Optional<IntBoundaries> finish() {
            return current == EMPTY ? Optional.empty() : Optional.of(current);
        }

        public Mutable merge(Mutable other) {
            current = current.merge(other.current);
            return this;
        }

        public void update(int val) {
            current = current.accumulate(val);
        }

        public void update(Integer val) {
            current = current.accumulateObj(val);
        }
    }
}
