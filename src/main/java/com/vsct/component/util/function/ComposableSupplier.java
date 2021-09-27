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

package com.vsct.component.util.function;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Extension de {@link Supplier}, supportant la transformation de la donnée produite (via {@link #andThen(Function)}).
 *
 * @param <T> type de donnée produite
 * @since 1.0
 */
@FunctionalInterface
public interface ComposableSupplier<T> extends Supplier<T> {

    static <O> ComposableSupplier<O> asComposable(Supplier<O> base) {
        if (base instanceof ComposableSupplier<?>) {
            return (ComposableSupplier<O>) base;
        }
        return base::get;
    }

    /**
     * Chaine une source de donnée avec une transformation.
     *
     * @param after transformation à appliquer à la sortie
     * @param <O> type d'objet en sortie
     * @return nouveau {@code ComposableSupplier}, composition des 2 éléments
     */
    default <O> ComposableSupplier<O> andThen(Function<? super T, ? extends O> after) {
        Objects.requireNonNull(after, "mapping Function must exist");
        return () -> after.apply(get());
    }
}
