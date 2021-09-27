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
import java.util.Set;
import java.util.function.Function;

/**
 * Base d'implémentation d'une {@link ModifiableImmutableCollection}, pour un {@code Set}.
 *
 * @param <ELEM> type d'élément assemblés
 * @param <TYPE> type de {@code Set} voulu
 * @param <SELF> instance concrète courante
 *
 * @since 1.0
 */
public abstract class AbstractImmutableSet<ELEM, TYPE extends Set<ELEM>, SELF extends AbstractImmutableSet<ELEM, TYPE, SELF>>
        extends AbstractImmutableCollection<ELEM, TYPE, SELF>
        implements ImmutableSet<ELEM> {

    protected AbstractImmutableSet(TYPE inner,
                                   Function<? super TYPE, SELF> newSelf,
                                   TypeDelegate<ELEM, ? extends TYPE> typeDelegate) {
        super(inner, newSelf, typeDelegate);
    }

    @Override
    public SELF add(ELEM elem) {
        typeDelegate.requireNonNull(elem, "element to add must exist");
        final TYPE copy = copyValues();
        final boolean added = copy.add(elem);
        return added ? wrap(copy) : self();
    }

    @Override
    public SELF addAll(Collection<? extends ELEM> elems) {
        typeDelegate.requireNoNull(elems, "elements to add");

        final TYPE combined = copyValues();
        final boolean added = combined.addAll(elems);
        return added ? wrap(combined) : self();
    }

    @Override
    public SELF plus(SELF other) {
        typeDelegate.requireNonNull(other, "elements to add must exist");

        final SELF big, small;
        if (other.size() > size()) {
            big = other;
            small = self();
        } else {
            big = self();
            small = other;
        }

        // concaténation
        TYPE combined = big.copyValues();
        boolean added = combined.addAll(small.inner);
        return added ? wrap(combined) : big;
    }

    @Override
    protected TYPE copyValues() {
        TYPE copy = typeDelegate.newHolder();
        copy.addAll(inner);
        return copy;
    }
}
