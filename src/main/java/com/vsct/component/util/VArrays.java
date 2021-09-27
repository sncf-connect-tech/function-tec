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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Pendant des méthodes utilitaires des Collections appliquées aux tableaux.
 *
 * @since 1.0
 */
public class VArrays {

    /**
     * Renvoit le 1er élément du tableau fourni.
     *
     * @param from source de donnée (<em>nullable</em>)
     * @param <E>  type de donnée de la source.
     * @return 1er élément du tableau fourni, s'il existe.
     */
    public static <E> Optional<E> first(final E[] from) {
        if (from != null && from.length > 0) {
            return Optional.ofNullable(from[0]);
        }
        return Optional.empty();
    }

    /**
     * Factorisation d'un traitement simple récurrent :
     * {@code Arrays.stream(someArr).filter(e -> [eval it]).findFirst();}
     * <p>
     * NE PAS UTILISER si vous devez transformer, projeter, bref tout ce qui fait la richesse des streams
     *
     * @param from      source de donnée (<em>nullable</em>)
     * @param condition contrat à respecter (<em>non-null</em>)
     * @param <E>       type de donnée de la source
     * @return 1er élément qui respecte la condition fournie, s'il existe.
     */
    public static <E> Optional<E> firstThat(final E[] from, final Predicate<? super E> condition) {
        Objects.requireNonNull(condition, "filtering without condition!");
        return toStream(from).filter(condition).findFirst();
    }

    /**
     * Implémentation <em>null-safe</em>.
     *
     * @param input canditat, peut être {@code null}.
     * @param <E>   type d'élément
     * @return résultat de {@link Arrays#stream(Object[])} si le tableau existe, le flux vide sinon.
     */
    public static <E> Stream<E> toStream(final E[] input) {
        if (input == null || input.length == 0) {
            return Stream.empty();

        } else {
            return Arrays.stream(input);
        }
    }
}
