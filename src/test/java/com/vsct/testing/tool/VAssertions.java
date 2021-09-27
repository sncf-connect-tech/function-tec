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

import com.vsct.component.util.Boundaries;
import com.vsct.component.util.IntBoundaries;
import com.vsct.component.util.ctrl.Either;
import com.vsct.component.util.ctrl.Try;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.SoftAssertions;

/**
 * @since 1.0
 */
public class VAssertions {

    public static <E> void assertBoundaries(Boundaries<E> actual, E expectedMin, E expectedMax) {
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual.getMin()).as("min").isEqualTo(expectedMin);
        softly.assertThat(actual.getMax()).as("max").isEqualTo(expectedMax);
        softly.assertAll();
    }

    public static void assertIntBoundaries(IntBoundaries actual, int expectedMin, int expectedMax) {
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual.getMin()).as("min").isEqualTo(expectedMin);
        softly.assertThat(actual.getMax()).as("max").isEqualTo(expectedMax);
        softly.assertAll();
    }

    public static <L, R> EitherAssert<L, R> assertThat(Either<L, R> actual) {
        return new EitherAssert<>(actual);
    }

    public static <E> TryAssert<E> assertThat(Try<E> actual) {
        return new TryAssert<>(actual);
    }

    public static class EitherAssert<L, R>
            extends AbstractObjectAssert<EitherAssert<L, R>, Either<L, R>> {

        EitherAssert(Either<L, R> actual) {
            super(actual, EitherAssert.class);
        }

        /**
         * unboxing de la partie alternative
         *
         * @return {@code ObjetAssert} du bon type
         * @throws AssertionError si l'instance était {@link Either.Right}
         */
        public ObjectAssert<L> left() {
            if (actual.isRight()) {
                throw new AssertionError("expected:<Left[…]> but was:<" + actual + ">");
            }
            return new ObjectAssert<>(actual.getLeft());
        }

        /**
         * unboxing de la partie nominale
         *
         * @return {@code ObjetAssert} du bon type
         * @throws AssertionError si l'instance était {@link Either.Left}
         */
        public ObjectAssert<R> right() {
            if (!actual.isRight()) {
                throw new AssertionError("expected:<Right[…]> but was:<" + actual + ">");
            }
            return new ObjectAssert<>(actual.get());
        }
    }

    public static class TryAssert<E>
            extends AbstractObjectAssert<TryAssert<E>, Try<E>> {

        TryAssert(Try<E> actual) {
            super(actual, TryAssert.class);
        }

        /**
         * unboxing de l'exception
         *
         * @param expected point de comparaison
         * @param <X>      type d'erreur attendue
         * @return {@code AbstractThrowableAssert} du bon type
         * @throws AssertionError si l'instance était {@link Try.Success}
         */
        @SuppressWarnings("unchecked")
        public <X extends RuntimeException> AbstractThrowableAssert<?, X> failureOfType(Class<X> expected) {
            if (!actual.isFailure(expected)) {
                throw new AssertionError("expected:<Failure[" + expected.getName() + "]> but was:<" + actual + ">");
            }
            return (AbstractThrowableAssert<?, X>) Assertions.assertThat(actual.getCause());
        }

        /**
         * unboxing de la partie nominale
         *
         * @return {@code ObjetAssert} du bon type
         * @throws AssertionError si l'instance était {@link Try.Failure}
         */
        public ObjectAssert<E> success() {
            if (!actual.isSuccess()) {
                throw new AssertionError("expected:<Success[…]> but was:<" + actual + ">");
            }
            return new ObjectAssert<>(actual.get());
        }
    }
}
