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

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Méthodes utilitaires autour du package {@code java.util.function}.
 *
 * @since 1.0
 */
public class VFunctions {

    private VFunctions() {
        // Non instanciable
    }

    /**
     * Returns a supplier which caches the instance retrieved during the first call to {@code get()}
     * and returns that value on subsequent calls to {@code get()}. See:
     * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
     *
     * <p>The returned supplier is thread-safe. The delegate's {@code get()} method will be invoked at
     * most once unless the underlying {@code get()} throws an exception.
     *
     * <p>When the underlying delegate throws an exception then this memoizing supplier will keep
     * delegating calls until it returns valid data.
     *
     * <p>If {@code delegate} is an instance created by an earlier call to {@code memoize}, it is
     * returned directly.
     */
    public static <T> Supplier<T> memoize(Supplier<? extends T> delegate) {
        if (delegate instanceof MemoizedSupplier<?>) {
            return (Supplier<T>) delegate;
        }
        return new MemoizedSupplier<>(delegate);
    }

    /**
     * Returns a supplier that caches the instance supplied by the delegate and removes the cached
     * value after the specified time has passed. Subsequent calls to {@code get()} return the cached
     * value if the expiration time has not passed. After the expiration time, a new value is
     * retrieved, cached, and returned. See: <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
     *
     * <p>The returned supplier is thread-safe.
     *
     * <p>The actual memoization does not happen when the underlying delegate throws an exception.
     * When the underlying delegate throws an exception then this memoizing supplier will keep
     * delegating calls until it returns valid data.
     *
     * @param duration the length of time after a value is created that it should stop being returned
     *     by subsequent {@code get()} calls
     * @throws IllegalArgumentException if {@code duration} is not positive
     */
    public static <T> Supplier<T> memoizeForDuration(Supplier<? extends T> delegate, Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("duration must be > 0 (got " + duration + ")");
        }
        return new ExpiringMemoizedSupplier<>(delegate, duration);
    }

    /**
     * Returns a supplier that caches the instance supplied by the delegate and removes the cached
     * value after the specified time has passed. Subsequent calls to {@code get()} return the cached
     * value if the expiration time has not passed. After the expiration time, a new value is
     * retrieved, cached, and returned. See: <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
     *
     * <p>The returned supplier is thread-safe.
     *
     * <p>The actual memoization does not happen when the underlying delegate throws an exception.
     * When the underlying delegate throws an exception then this memoizing supplier will keep
     * delegating calls until it returns valid data.
     *
     * @param duration the length of time after a value is created that it should stop being returned
     *     by subsequent {@code get()} calls
     * @param unit the unit that {@code duration} is expressed in
     * @throws IllegalArgumentException if {@code duration} is not positive
     */
    public static <T> Supplier<T> memoizeForDuration(Supplier<? extends T> delegate, long duration, TimeUnit unit) {
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be > 0 (got " + duration + " " + unit + ")");
        }
        return new ExpiringMemoizedSupplier<>(delegate, duration, unit);
    }
}
