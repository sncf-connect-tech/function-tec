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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Base d'implémentation d'une {@link ModifiableImmutableCollection}, pour une {@code List}.
 *
 * @param <ELEM> type d'élément listés
 * @param <SELF> instance concrète courante
 *
 * @since 1.0
 */
public abstract class AbstractImmutableList<ELEM, SELF extends AbstractImmutableList<ELEM, SELF>>
        extends AbstractImmutableCollection<ELEM, List<ELEM>, SELF>
        implements ModifiableImmutableList<ELEM, SELF> {

    protected AbstractImmutableList(List<ELEM> inner, Function<List<ELEM>, SELF> newSelf) {
        super(inner, newSelf, TypeDelegate.arrayList());
    }

    @Override
    public SELF add(ELEM elem) {
        Objects.requireNonNull(elem, "element to add must exist");
        return plus(wrap(List.of(elem)));
    }

    @Override
    public List<ELEM> asList() {
        return new ArrayList<>(inner);
    }

    @Override
    public ELEM first() {
        return inner.get(0);
    }

    @Override
    public ELEM last() {
        return inner.get(inner.size() - 1);
    }

    @Override
    public SELF sort(Comparator<? super ELEM> comparator) {
        List<ELEM> sorted = asList();
        sorted.sort(comparator);
        return sorted.equals(inner) ? self() : wrap(sorted);
    }

    @Override
    protected List<ELEM> copyValues() {
        return asList();
    }
}
