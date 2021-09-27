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
import java.util.function.Supplier;

/**
 * Copie de {@code com.google.common.base.Suppliers.NonSerializableMemoizingSupplier} débarrassée de
 * l'implémentation "non-standard" de {@code Supplier}.
 *
 * @since 1.0
 */
class MemoizedSupplier<T> implements Supplier<T> {
    private volatile Supplier<? extends T> delegate;
    private volatile boolean initialized;
    // "value" does not need to be volatile; visibility piggy-backs
    // on volatile read of "initialized".
    private T value;

    MemoizedSupplier(Supplier<? extends T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must exist");
    }

    @Override
    public T get() {
        // A 2-field variant of Double Checked Locking.
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    T t = delegate.get();
                    value = t;
                    initialized = true;
                    // Release the delegate to GC.
                    delegate = null;
                    return t;
                }
            }
        }
        return value;
    }
}
