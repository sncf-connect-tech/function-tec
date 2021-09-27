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
import java.util.function.Supplier;

/**
 * @since 1.0
 */
public class VOptionals {

    private VOptionals() {
        // non instanciable
    }

    /**
     * Permet de récupérer la première valeur à respecter une condition, en <em>lazy evaluation</em> (via {@link Supplier}).
     *
     * @param condition      contrat que la donnée doit respecter.
     * @param alternateChain liste de {@code Supplier}, à invoquer dans l'ordre, en s'arrêtant dès qu'un résultat est accepté par le prédicat fourni.
     * @param <T>            type de donées manipulée.
     * @return 1ère valeur respectant la condition, si elle existe.
     * @throws NullPointerException si la condition vaut {@code null}.
     */
    @SafeVarargs
    public static <T> Optional<T> firstThat(final Predicate<? super T> condition, final Supplier<T>... alternateChain) {
        Objects.requireNonNull(condition, "conditional search without condition!");
        return Arrays.stream(alternateChain)
                .map(Supplier::get)
                .filter(condition)
                .findFirst();
    }

    public static Optional<String> nonEmpty(final String s) {
        return Optional.ofNullable(s).filter(str -> str.length() > 0);
    }
}
