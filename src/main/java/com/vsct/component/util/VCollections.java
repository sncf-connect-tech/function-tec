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

import com.vsct.component.util.ctrl.Either;
import com.vsct.component.util.immucoll.ImmutableCollection;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * Méthodes utilitaires autour des Collections
 *
 * @since 1.0
 */
public class VCollections {

    public enum LookupFailure {
        EMPTY_CONTAINER, NO_MATCHING_ELT, NULL_CONTAINER, NULL_ELT
    }

    private VCollections() {
        // Non instanciable
    }

    /**
     * Factorisation d'un traitement simple récurrent :
     * <code><pre>someColl.stream()
     * .map(e -> [transform it])
     * .collect(Collectors.to[some collection type]);</pre></code>
     * NE PAS UTILISER si vous devez filtrer, aplatir, bref tout ce qui fait la richesse des streams
     *
     * @param orig      collections initiale (<em>nullable</em>)
     * @param transform foncteur de transformation (<em>not-null</em>)
     * @param spawner   foncteur de création de la collection finale
     * @param <I>       type d'élément initial
     * @param <O>       type d'élément final
     * @param <C>       type de collection de destination
     * @return nouvelle instance de la collection demandée, potentiellement vide mais jamais {@code null}.
     */
    public static <I, O, C extends Collection<O>> C collect(Collection<I> orig,
                                                            Function<? super I, ? extends O> transform,
                                                            Supplier<? extends C> spawner) {
        return collectWith(orig, transform, toCollection(spawner));
    }

    /**
     * Factorisation d'un traitement simple récurrent :
     * <pre>{@code someColl.stream().map(e -> [transform it]).collect(toList());}</pre>
     *
     * <p>NE PAS UTILISER si vous devez filtrer, aplatir, bref tout ce qui fait la richesse des streams
     *
     * @param orig      collections initiale (<em>nullable</em>)
     * @param transform foncteur de transformation (<em>not-null</em>)
     * @param <I>       type d'élément initial
     * @param <O>       type d'élément final
     * @return nouvelle {@code List}, potentiellement vide mais jamais {@code null}.
     */
    public static <I, O> List<O> collectList(Collection<I> orig, Function<? super I, ? extends O> transform) {
        return collectWith(orig, transform, toList());
    }

    /**
     * Factorisation d'un traitement simple récurrent :
     * <code><pre>someColl.stream()
     * .map(e -> [transform it])
     * .collect([some collector]);</pre></code>
     * NE PAS UTILISER si vous devez filtrer, aplatir, bref tout ce qui fait la richesse des streams
     *
     * @param orig      collections initiale (<em>nullable</em>)
     * @param transform foncteur de transformation (<em>not-null</em>)
     * @param collector responsable de l'accumulation des résultats des transformations
     * @param <I>       type d'élément initial
     * @param <O>       type d'élément final
     * @param <E>       type d'aggrégation
     * @return résultat de l'aggrégation par le {@code Collector} fourni.
     */
    public static <I, O, E> E collectWith(Collection<I> orig,
                                          Function<? super I, ? extends O> transform,
                                          Collector<O, ?, E> collector) {
        return toStream(orig).map(transform).collect(collector);
    }

    /**
     * Renvoit (lecture non destructrice) le 1er élément de la collection fournie.
     * <br/>Notez qu'appliquée à une {@code Collection} qui ne garantie pas l'ordre ({@code Set}), la notion de premier n'est pas "stable".
     *
     * @param from source de donnée (<em>nullable</em>)
     * @param <E>  type de donnée de la source.
     * @return 1er élément de la collection fournie, s'il existe.
     */
    public static <E> Optional<E> first(final Collection<E> from) {
        if (from != null && !from.isEmpty()) {
            return Optional.ofNullable(findFirst(from));
        }
        return Optional.empty();
    }

    /**
     * Renvoit (lecture non destructrice) le 1er élément de la collection fournie.
     * <br/>Notez qu'appliquée à une {@code Collection} qui ne garantie pas l'ordre ({@code Set}), la notion de premier n'est pas "stable".
     *
     * @param from source de donnée.
     * @param <E>  type de donnée de la source.
     * @return 1er élément de la collection fournie, ou la raison pourquoi il n'a pas été trouvé.
     */
    public static <E> Either<LookupFailure, E> firstOrExplain(final Collection<E> from) {
        return Either.ofNullableOr(from, LookupFailure.NULL_CONTAINER)
                .filter(c -> !c.isEmpty(), LookupFailure.EMPTY_CONTAINER)
                .mapNullable(VCollections::findFirst, LookupFailure.NULL_ELT);
    }

    /**
     * Factorisation d'un traitement simple récurrent :
     * {@code someColl.stream().filter(e -> [eval it]).findFirst();}
     * <p>
     * NE PAS UTILISER si vous devez transformer, projeter, bref tout ce qui fait la richesse des streams
     *
     * @param from      source de donnée (<em>nullable</em>)
     * @param condition contrat à respecter (<em>non-null</em>)
     * @param <E>       type de donnée de la source
     * @return 1er élément qui respecte la condition fournie, s'il existe.
     */
    public static <E> Optional<E> firstThat(final Collection<E> from, final Predicate<? super E> condition) {
        Objects.requireNonNull(condition, "filtering without condition!");
        return toStream(from).filter(condition).findFirst();
    }

    /**
     * Déclinaison {@code Either} de {@link #firstThat(Collection, Predicate)}.
     *
     * @param from      source de donnée (<em>nullable</em>)
     * @param condition contrat à respecter (<em>non-null</em>)
     * @param <E>       type de donnée de la source
     * @return 1er élément qui respecte la condition fournie, ou raison de son absense.
     */
    public static <E> Either<LookupFailure, E> firstThatOrExplain(final Collection<E> from, final Predicate<? super E> condition) {
        Objects.requireNonNull(condition, "filtering without condition!");
        return Either.ofNullableOr(from, LookupFailure.NULL_CONTAINER)
                .filter(c -> !c.isEmpty(), LookupFailure.EMPTY_CONTAINER)
                .flatMap(c -> Either.fromOptionalOr(c.stream().filter(condition).findFirst(), LookupFailure.NO_MATCHING_ELT));
    }

    /**
     * Généralisation de {@link #toConcatStream} à un nombre variable de collections en entrée
     *
     * @param collections instances, <em>nullable</em>, dont on veut les éléments
     * @param <E> type d'élément
     * @return flux des valeurs des différentes collections, dans l'ordre de déclaration,
     *         en assimilant une liste {@code null} à "vide".
     */
    @SafeVarargs
    public static <E> Stream<E> flatStream(Collection<? extends E>... collections) {
        switch (collections.length) {
            case 0: return Stream.empty();
            case 1: return toStream(collections[0]);
            case 2: return toConcatStream(collections[0], collections[1]);
            default: return Stream.of(collections).flatMap(VCollections::toStream);
        }
    }

    /**
     * Factorisation d'un traitement simple récurrent :
     * <pre>{@code someColl.stream().filter(e -> [eval it]).collect(toList());}</pre>
     *
     * <p>NE PAS UTILISER si vous devez transformer, projeter, bref tout ce qui fait la richesse des streams
     *
     * @param from      source de donnée (<em>nullable</em>)
     * @param condition contrat à respecter (<em>non-null</em>)
     * @param <E>       type de donnée de la source
     * @return nouvelle {@code List}, potentiellement vide mais jamais {@code null}.
     */
    public static <E> List<E> select(final Collection<E> from, final Predicate<? super E> condition) {
        return select(from, condition, ArrayList::new);
    }

    /**
     * Généralisation de {@link #select(Collection, Predicate)} permettant la collecte dans une collection qui
     * n'est PAS une liste.
     *
     * @param from      source de donnée (<em>nullable</em>)
     * @param condition contrat à respecter (<em>non-null</em>)
     * @param spawner   création de la collection résultat
     * @param <E>       type de donnée de la source
     * @param <C>       type de la collection résultat
     * @return nouvelle {@code Collection}, potentiellement vide mais jamais {@code null}.
     */
    public static <E, C extends Collection<E>> C select(
            final Collection<E> from,
            final Predicate<? super E> condition,
            final Supplier<? extends C> spawner) {
        Objects.requireNonNull(condition, "filtering without condition!");
        Objects.requireNonNull(spawner, "no result container supplier!");
        return toStream(from).filter(condition).collect(toCollection(spawner));
    }

    /**
     * Produit un flux concaténé à partir deux collections qui peuvent être {@code null}
     *
     * @param left  1ère collection
     * @param right 2nde collection
     * @param <E> type d'objet du flux en sortie
     * @return flux, potentiellement vide
     */
    @SuppressWarnings("unchecked")
    public static <E> Stream<E> toConcatStream(Collection<? extends E> left, Collection<? extends E> right) {
        if (left == null) {
            return toStream(right);
        } else if (right == null) {
            return (Stream<E>) left.stream();
        } else {
            return Stream.concat(left.stream(), right.stream());
        }
    }

    /**
     * Implémentation <em>null-safe</em>.
     *
     * @param c   canditat, peut être {@code null}.
     * @param <E> type d'élément
     * @return résultat de {@link Collection#stream()} si la collection existe, le flux vide sinon.
     */
    @SuppressWarnings("unchecked")
    public static <E> Stream<E> toStream(Collection<? extends E> c) {
        return c == null ? Stream.empty() : (Stream<E>) c.stream();
    }

    /**
     * Déclinaison de {@link #toStream(Collection)} pour les {@link ImmutableCollection}.
     *
     * <p>Même si, en toute rigueur, vous ne devriez <strong>jamais</strong> balader une instance
     * {@code null} d'une telle classe !
     * Comme l'implémentation concrète correspond à un concept métier, une représentation convenable
     * de son absence doit pouvoir être définie (une simple instance "vide" peut faire l'affaire).
     *
     * @param c   canditat, peut être {@code null}.
     * @param <E> type d'élément
     * @return résultat de {@link ImmutableCollection#stream()} si la collection existe, le flux vide sinon.
     */
    @SuppressWarnings("unchecked")
    public static <E> Stream<E> toStream(ImmutableCollection<? extends E> c) {
        return c == null ? Stream.empty() : (Stream<E>) c.stream();
    }

    /**
     * Emprunt à apache-commons, avec un typage libre de la collection de sortie.
     *
     * <p>Cette méhtode se contente d'invoquer deux fois {@link Collection#addAll(Collection)}, ce qui garantit
     * l'ordre d'occurence <strong>SSI</strong> le conteneur de sortie est ordonné.
     *
     * @param left  1ère collection
     * @param right 2nde collection
     * @param spawn foncteur de récupération de la collection de sortie (typiquement un constructeur,
     *              mais ce n'est pas obligatoire)
     * @param <E> type d'objet de la collection de sortie
     * @param <C> type de collection de sortie
     * @return instance issue du 3e paramètre, contenant tous les éléments des 2 premiers paramètres
     * @throws NullPointerException si l'un des trois paramètres vaut {@code null}
     */
    public static <E, C extends Collection<E>> C union(
            Collection<? extends E> left, Collection<? extends E> right,
            Supplier<? extends C> spawn) {
        Objects.requireNonNull(spawn, "output supplier is mandatory");
        Objects.requireNonNull(left, "1st collection of elements to add is mandatory");
        Objects.requireNonNull(right, "2nd collection of elements to add is mandatory");

        C union = spawn.get();
        union.addAll(left);
        union.addAll(right);
        return union;
    }

    /**
     * @param from source de donnée.
     * @param <E>  type de donnée de la source.
     * @return 1er élement, récupéré selon la méthode adaptée au conteneur.
     */
    private static <E> E findFirst(final Collection<E> from) {
        if (from instanceof List<?>) {
            return ((List<E>) from).get(0);
        } else if (from instanceof Queue<?>) {
            return ((Queue<E>) from).peek();
        } else if (from instanceof SortedSet<?>) {
            return ((SortedSet<E>) from).first();
        } else {
            return from.iterator().next();
        }
    }
}
