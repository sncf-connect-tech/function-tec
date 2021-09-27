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

package com.vsct.testing.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * implémentation retournant toujours la valeur fournie à la construction, en trackant l'indice d'appel
 * (en utilisant la référence pour ce faire).
 *
 * @since 1.0
 */
public class TrackingSupplier<T> implements Supplier<T> {
    private final AtomicInteger reference;

    private final T val;
    private final List<Integer> indexes;

    public TrackingSupplier(T val, final AtomicInteger reference) {
        indexes = new ArrayList<>();
        this.reference = reference;
        this.val = val;
    }

    @Override
    public T get() {
        indexes.add(reference.getAndIncrement());
        return val;
    }

    public void assertCalls(final Integer... expectedIndexes) {
        assertThat(indexes).as("unexpected calls").containsOnly(expectedIndexes);
    }
}
