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

import static java.util.Objects.requireNonNull;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Copie de {@code com.google.common.base.Suppliers.ExpiringMemoizingSupplier} débarrassée de
 * l'implémentation "non-standard" de {@code Supplier}.
 * Également possible d'utiliser {@code java.time.Duration} directement pour spécifier la TTL.
 *
 * @since 1.0
 */
class ExpiringMemoizedSupplier<T> implements Supplier<T> {

    private final Supplier<? extends T> delegate;
    private final long durationNanos;
    // The special value 0 means "not yet initialized".
    private volatile long expirationNanos;
    private volatile T memo;

    ExpiringMemoizedSupplier(Supplier<? extends T> delegate, Duration duration) {
        this.delegate = requireNonNull(delegate, "delegate must exist");
        this.durationNanos = duration.toNanos();
    }

    ExpiringMemoizedSupplier(Supplier<? extends T> delegate, long duration, TimeUnit unit) {
        this.delegate = requireNonNull(delegate, "delegate must exist");
        this.durationNanos = requireNonNull(unit, "duration unit must exist").toNanos(duration);
    }

    @Override
    public T get() {
        // Another variant of Double Checked Locking.
        //
        // We use two volatile reads. We could reduce this to one by
        // putting our fields into a holder class, but (at least on x86)
        // the extra memory consumption and indirection are more
        // expensive than the extra volatile reads.
        long nanos = expirationNanos;
        long now = System.nanoTime();
        if (nanos == 0 || now - nanos >= 0) {
            synchronized (this) {
                if (nanos == expirationNanos) { // recheck for lost race
                    T t = delegate.get();
                    memo = t;
                    nanos = now + durationNanos;
                    // In the very unlikely event that nanos is 0, set it to 1;
                    // no one will notice 1 ns of tardiness.
                    expirationNanos = (nanos == 0) ? 1 : nanos;
                    return t;
                }
            }
        }
        return memo;
    }

    @Override
    public String toString() {
        // This is a little strange if the unit the user provided was not NANOS,
        // but we don't want to store the unit just for toString
        return "ExpiringMemoization(" + delegate + ", " + durationNanos + " nano-seconds)";
    }
}
