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

/**
 * cousin d'{@link ImmutableCollection.ImmCollector}, qui gère en plus un mapping initial entre la valeur qui entre dans
 * le collecteur et celle qui est voulue en sortie
 *
 * @param <I> type de la donnée "entrante"
 * @param <O> type de la donnée collectée dans la collection immuable
 * @param <T> type de {@code Collection} représentée
 * @param <C> type concret d'{@code ImmutableCollection} voulue au final
 *
 * @since 1.0
 */
class MappingCollector<I, O, T extends Collection<O>, C extends ImmutableCollection<O>>
        implements Collector<I, ImmutableCollection.Builder<O, T, C>, C> {

    private final Function<? super T, C> constructor;
    private final Function<? super I, Stream<? extends O>> inputMapper;
    private final TypeDelegate<O, ? extends T> typeDelegate;

    public MappingCollector(Function<? super T, C> constructor,
                            TypeDelegate<O, ? extends T> typeDelegate,
                            Function<? super I, Stream<? extends O>> inputMapper) {
        this.constructor = constructor;
        this.inputMapper = inputMapper;
        this.typeDelegate = typeDelegate;
    }

    @Override
    public Supplier<ImmutableCollection.Builder<O, T, C>> supplier() {
        return () -> new ImmutableCollection.Builder<>(constructor, typeDelegate);
    }

    @Override
    public BiConsumer<ImmutableCollection.Builder<O, T, C>, I> accumulator() {
        return this::mapAndAdd;
    }

    @Override
    public BinaryOperator<ImmutableCollection.Builder<O, T, C>> combiner() {
        return ImmutableCollection.Builder::merge;
    }

    @Override
    public Function<ImmutableCollection.Builder<O, T, C>, C> finisher() {
        return ImmutableCollection.Builder::build;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }

    private void mapAndAdd(ImmutableCollection.Builder<O, T, C> builder, I input) {
        typeDelegate.requireNonNull(input, "element to map must exist");
        final List<O> mapped = inputMapper.apply(input).collect(toList());
        builder.addAll(mapped);
    }
}
