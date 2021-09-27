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

import static java.util.Collections.emptySet;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Permet d'éviter la complexité rédactionnelle du {@code reduce}, en bénéficiant de la logique
 * "aucun élément -> {@code Optional} vide".
 *
 * @param <E> type de donnée manipulée
 */
class BoundariesCollector<E> implements Collector<E, BoundariesCollector.Holder, Optional<Boundaries<E>>> {

    private static final Set<Collector.Characteristics> CH_NONE = emptySet();

    private final Comparator<? super E> comparator;

    BoundariesCollector(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    @Override
    public BiConsumer<BoundariesCollector.Holder, E> accumulator() {
        return Holder::update;
    }

    @Override
    public Set<Collector.Characteristics> characteristics() {
        return CH_NONE;
    }

    @Override
    public BinaryOperator<BoundariesCollector.Holder> combiner() {
        return Holder::merge;
    }

    @Override
    public Function<BoundariesCollector.Holder, Optional<Boundaries<E>>> finisher() {
        return Holder::finish;
    }

    @Override
    public Supplier<BoundariesCollector.Holder> supplier() {
        return Holder::new;
    }

    /**
     * "Intermédiaire" obligatoire car {@code Stream.collect} repose sur un conteneur <em>mutable</em>
     * et {@code Boundaries} est <em>immutable</em>.
     */
    class Holder {
        private Boundaries<E> current = Boundaries.empty(comparator);

        Optional<Boundaries<E>> finish() {
            return current.getMin() == null ? Optional.empty() : Optional.of(current);
        }

        Holder merge(Holder other) {
            current = current.merge(other.current);
            return this;
        }

        void update(E val) {
            current = current.accumulate(val);
        }
    }
}
