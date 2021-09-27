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

package com.vsct.component.util.ctrl;

import com.vsct.testing.data.Dbl;
import com.vsct.testing.data.Direction;
import com.vsct.testing.data.Dist;
import com.vsct.testing.data.Heading;
import com.vsct.testing.data.MathFailure;
import com.vsct.testing.data.Oops;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.vsct.testing.tool.VAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
class EitherTest {

    private static final String NONE = "no element";
    private static final String FAILED_ROTATION = "no direction to rotate from";

    private final Dist e0 = new Dist(0d, "cm");
    private final Dist e2 = new Dist(2d, "m");
    private final Dist neg = new Dist(-1d, "dm");
    private final List<Either<Boolean, Dist>> canRetry = asList(
            Either.right(e2),
            Either.left(false),
            Either.left(true),
            Either.right(new Dist(10d, "km")),
            Either.right(new Dist(8.4d, "mm")),
            Either.left(true),
            Either.right(e0)
    );

    private Heading north(double distance) {
        if (distance < 0) {
            return new Heading(-distance, "m", Direction.S);
        } else {
            return new Heading(distance, "m", Direction.N);
        }
    }

    private Either<String, Heading> rotate(Dist e) {
        if (e instanceof Heading) {
            return Either.right(new Heading(e.getVal(), e.getDimension(), ((Heading) e).getDir().next()));
        } else {
            return Either.left(FAILED_ROTATION);
        }
    }

    private String unsafe(boolean fail) {
        if (fail) {
            throw new Oops("asked for");
        } else {
            return "success";
        }
    }

    // ======================================================================================
    //
    // factory methods
    //
    // ======================================================================================

    @Test
    void left_shouldFail_onNull() {
        Throwable t = catchThrowable(() -> Either.left(null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("alternate value must exist");
    }

    @Test
    void right_shouldFail_onNull() {
        Throwable t = catchThrowable(() -> Either.right(null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("right value must exist");
    }

    @Test
    void fromOptionalOr_shouldReturnLeft_whenEmpty() {
        Optional<Dist> opt = Optional.empty();
        Dist alt = new Dist(-1d, "cm");
        Either<Dist, Dist> e = Either.fromOptionalOr(opt, alt);

        assertThat((Either<? extends Dist, ?>) e).left().isSameAs(alt);
    }

    @Test
    void fromOptionalOr_shouldReturnRight_whenNonEmpty() {
        Optional<Dist> opt = Optional.of(e0);

        Either<String, Dist> e = Either.fromOptionalOr(opt, NONE);

        assertThat((Either<?, Dist>) e).right().isSameAs(e0);
    }

    @Test
    void fromOptionalOrGet_shouldReturnLeft_whenEmpty() {
        Optional<Dist> opt = Optional.empty();
        Dist alt = new Dist(-1, "cm");
        Either<Dbl, Dist> e = Either.fromOptionalOrGet(opt, () -> alt);

        assertThat((Either<? extends Dbl, ?>) e).left().isSameAs(alt);
    }

    @Test
    void fromOptionalOrGet_shouldReturnRight_whenNonEmpty() {
        Supplier<Heading> alt = mock(Supplier.class);
        Optional<Dist> opt = Optional.of(e0);

        Either<Dbl, Dist> e = Either.fromOptionalOrGet(opt, alt);

        assertThat((Either<?, Dist>) e).right().isSameAs(e0);
        verify(alt, never()).get();
    }

    @Test
    void ofNullableOr_shouldReturnLeft_whenNull() {
        Dist val = null;
        Either<Dist, Dist> e = Either.ofNullableOr(val, neg);

        assertThat((Either<? extends Dist, ?>) e).left().isSameAs(neg);
    }

    @Test
    void ofNullableOr_shouldReturnRight_whenNotNull() {
        Either<String, Dist> e = Either.ofNullableOr(e0, NONE);

        assertThat((Either<?, Dist>) e).right().isSameAs(e0);
    }

    @Test
    void ofNullableOrGet_shouldReturnLeft_whenNull() {
        Dist val = null;
        Either<Dist, Dist> e = Either.ofNullableOrGet(val, () -> neg);

        assertThat((Either<? extends Dist, ?>) e).left().isSameAs(neg);
    }

    @Test
    void ofNullableOrGet_shouldReturnRight_whenNotNull() {
        Supplier<Heading> alt = mock(Supplier.class);

        Either<Dbl, Dist> e = Either.ofNullableOrGet(e0, alt);

        assertThat((Either<?, Dist>) e).right().isSameAs(e0);
        verify(alt, never()).get();
    }

    @Test
    void trying_shouldReturnLeft_whenFailed() {
        Either<RuntimeException, String> actual = Either.trying(() -> unsafe(true));

        assertThat(actual).left().isInstanceOf(Oops.class);
    }

    @Test
    void trying_shouldReturnRight_whenSucceeded() {
        Either<RuntimeException, String> actual = Either.trying(() -> unsafe(false));

        assertThat(actual).right().isEqualTo("success");
    }

    // ======================================================================================
    //
    // contracts
    //
    // ======================================================================================

    @Test
    void bimap_shouldTransform_onLeft() {
        Function<Dist, Dist> right = mock(Function.class);
        String expected = "foo";
        Either<String, Dist> base = Either.left(expected);

        Either<Oops, Dist> mapped = base.bimap(Oops::new, right);

        assertThat(mapped).left()
                .satisfies(l -> assertThat(l.getArg()).isEqualTo(expected));
        verify(right, never()).apply(any(Dist.class));
    }

    @Test
    void bimap_shouldFail_onLeft_andNullLeftMapper() {
        Either<String, Dist> base = Either.left("foo");

        Throwable t = catchThrowable(() -> base.bimap(null, mock(Function.class)));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("leftMapper must exist");
    }

    @Test
    void bimap_shouldTransform_onRight() {
        Function<String, Oops> left = mock(Function.class);
        Heading expected = new Heading(1d, "m", Direction.N);
        Either<String, Double> base = Either.right(1d);

        Either<Oops, Dist> mapped = base.bimap(left, this::north);

        assertThat(mapped).right().isEqualTo(expected);
        verify(left, never()).apply(anyString());
    }

    @Test
    void bimap_shouldFail_onRight_andNullRightMapper() {
        Either<String, Dist> base = Either.right(e0);

        Throwable t = catchThrowable(() -> base.bimap(mock(Function.class), null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("rightMapper must exist");
    }

    @Test
    void equals_shouldDeferToValue_onLeft() {
        Either<Long, ?> one = Either.left(1L);
        Either<Long, ?> bis = Either.left(1L);
        Either<Long, ?> two = Either.left(2L);

        assertThat(one).isEqualTo(bis);
        assertThat(one).isNotEqualTo(two);
    }

    @Test
    void equals_shouldDeferToValue_onRight() {
        Either<?, Long> one = Either.right(1L);
        Either<?, Long> bis = Either.right(1L);
        Either<?, Long> two = Either.right(2L);

        assertThat(one).isEqualTo(bis);
        assertThat(one).isNotEqualTo(two);
    }

    @Test
    void equals_shouldLookupContainer() {
        Either<Long, ?> oneL = Either.left(1L);
        Either<?, Long> oneR = Either.right(1L);

        assertThat(oneR).isNotEqualTo(oneL);
    }

    @Test
    void filter_shouldDoNothing_onLeft() {
        Predicate<Dist> pred = mock(Predicate.class);
        Either<String, Dist> base = Either.left(NONE);

        Either<String, Dist> filtered = base.filter(pred, "bad value");

        Assertions.assertThat(filtered).isSameAs(base);
        verify(pred, never()).test(any(Dist.class));
    }

    @Test
    void filter_shouldDoNothing_onMatchingRight() {
        Either<Integer, String> base = Either.right("foo");

        Either<Integer, String> filtered = base.filter(s -> s.length() > 1, 1);

        Assertions.assertThat(filtered).isSameAs(base);
    }

    @Test
    void filter_shouldReturnLeft_onNonmatchingRight() {
        Either<Dist, String> base = Either.right("");
        Heading expected = new Heading(1d, "km", Direction.E);

        Either<Dist, String> filtered = base.filter(s -> s.length() > 1, expected);

        assertThat((Either<? extends Dist, ?>) filtered).left().isSameAs(expected);
    }

    @Test
    void flatMap_shouldDoNothing_onLeft() {
        Either<MathFailure, Dist> base = Either.left(MathFailure.NOT_A_NUM);

        Either<MathFailure, Double> flatMapped = base.flatMap(Dbl::invert);

        Assertions.assertThat(flatMapped).isSameAs(base);
    }

    @Test
    void flatMap_shouldTransform_onRight() {
        Either<String, Dist> base = Either.right(new Heading(1d, "m", Direction.W));

        Either<String, Dist> flatMapped = base.flatMap(this::rotate);

        Heading expected = new Heading(1d, "m", Direction.N);
        assertThat(flatMapped).right().isEqualTo(expected);
    }

    @Test
    void flatMap_couldReturnLeft_onRight() {
        Either<String, Dist> base = Either.right(e0);

        Either<String, Dist> flatMapped = base.flatMap(this::rotate);

        assertThat((Either<? extends String, ?>) flatMapped).left().isSameAs(FAILED_ROTATION);
    }

    @Test
    void flatMap_shouldFail_onRight_andNullRightMapper() {
        Either<String, Dist> base = Either.right(e0);

        Throwable t = catchThrowable(() -> base.flatMap(null));

        assertThat(t).isInstanceOf(NullPointerException.class).hasMessage("mapper must exist");
    }

    @Test
    void flatMapLeft_shouldDoNothing_onRight() {
        Either<Dist, Double> base = Either.right(1d);

        Either<MathFailure, Double> flatMapped = base.flatMapLeft(Dbl::invert);

        assertThat(flatMapped).isSameAs(base);
    }

    @Test
    void flatMapLeft_shouldTransform_onLeft() {
        Either<Dist, Double> base = Either.left(e0);

        Either<MathFailure, Double> flatMapped = base.flatMapLeft(Dbl::invert);

        assertThat(flatMapped).left().isEqualTo(MathFailure.DIV_BY_0);
    }

    @Test
    void flatMapLeft_couldReturnRight_onLeft() {
        Either<Dist, Double> base = Either.left(e2);

        Either<MathFailure, Double> flatMapped = base.flatMapLeft(Dbl::invert);

        assertThat(flatMapped).right().satisfies(val -> assertThat(val).isEqualTo(0.5d, within(0.01)));
    }

    @Test
    void flatMapLeft_shouldFail_onRLeft_andNullLeftMapper() {
        Either<String, Dist> base = Either.left("any");

        Throwable t = catchThrowable(() -> base.flatMapLeft(null));

        assertThat(t).isInstanceOf(NullPointerException.class).hasMessage("mapper must exist");
    }

    @Test
    void fold_shouldMapLeft_onLeft() {
        Function<Object, Double> right = mock(Function.class);
        Either<String, ?> base = Either.left("foo");

        Double folded = base.fold(str -> 1d / str.length(), right);

        assertThat(folded).isEqualTo(0.33d, within(0.01));
        verify(right, never()).apply(any());
    }

    @Test
    void fold_shouldFail_onLeft_andNullLeftMapper() {
        Either<String, ?> base = Either.left("foo");

        Throwable t = catchThrowable(() -> base.fold(null, mock(Function.class)));

        assertThat(t).isInstanceOf(NullPointerException.class).hasMessage("leftMapper must exist");
    }

    @Test
    void fold_shouldMapRight_onRight() {
        Function<Object, Dist> left = mock(Function.class);
        Heading expected = new Heading(2d, "m", Direction.S);
        Either<?, Double> base = Either.right(-2d);

        Dist folded = base.fold(left, this::north);

        assertThat(folded).isEqualTo(expected);
        verify(left, never()).apply(any());
    }

    @Test
    void fold_shouldFail_onRight_andNullRightMapper() {
        Either<?, Dist> base = Either.right(e2);

        Throwable t = catchThrowable(() -> base.fold(mock(Function.class), null));

        assertThat(t).isInstanceOf(NullPointerException.class);
    }

    @Test
    void get_shouldFail_onLeft() {
        Either<String, Object> base = Either.left("a");

        Throwable t = catchThrowable(base::get);

        assertThat(t).isInstanceOf(UnsupportedOperationException.class).hasMessage("on Left");
    }

    @Test
    void get_shouldReturnValue_onRight() {
        String expected = "a";
        String actual = Either.right(expected).get();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getLeft_shouldReturnValue_onLeft() {
        String expected = "a";
        String actual = Either.left(expected).getLeft();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getLeft_shouldFail_onRight() {
        Either<Object, String> base = Either.right("a");

        Throwable t = catchThrowable(base::getLeft);

        assertThat(t).isInstanceOf(UnsupportedOperationException.class).hasMessage("on Right");
    }

    @Test
    void hashCode_shouldDeferToValue_onLeft() {
        Either<Long, ?> one = Either.left(1L);
        Either<Long, ?> bis = Either.left(1L);
        Either<Long, ?> two = Either.left(2L);

        assertThat(one.hashCode()).isEqualTo(bis.hashCode());
        assertThat(one.hashCode()).isNotEqualTo(two.hashCode());
    }

    @Test
    void hashCode_shouldDeferToValue_onRight() {
        Either<?, Long> one = Either.right(1L);
        Either<?, Long> bis = Either.right(1L);
        Either<?, Long> two = Either.right(2L);

        assertThat(one.hashCode()).isEqualTo(bis.hashCode());
        assertThat(one.hashCode()).isNotEqualTo(two.hashCode());
    }

    @Test
    void hashCode_shouldLookupContainer() {
        Either<Long, ?> oneL = Either.left(1L);
        Either<?, Long> oneR = Either.right(1L);

        assertThat(oneR.hashCode()).isNotEqualTo(oneL.hashCode());
    }

    @Test
    void onLeft_shouldExecute_onLeft() {
        Consumer<String> alt1 = mock(Consumer.class);
        Consumer<String> alt2 = mock(Consumer.class);
        String consumed = "f";
        Either<String, ?> base = Either.left(consumed);

        base.onLeft(alt1).onLeft(alt2);

        verify(alt1).accept(consumed);
        verify(alt2).accept(consumed);
    }

    @Test
    void onLeft_shouldDoNothing_onRight() {
        Consumer<String> alt = mock(Consumer.class);
        Either<String, ?> base = Either.right(e0);

        base.onLeft(alt);

        verify(alt, never()).accept(anyString());
    }

    @Test
    void onRight_shouldDoNothing_onLeft() {
        Consumer<String> correct = mock(Consumer.class);
        Either<?, String> base = Either.left(e0);

        base.onRight(correct);

        verify(correct, never()).accept(anyString());
    }

    @Test
    void onRight_shouldExecute_onRight() {
        Consumer<String> correct1 = mock(Consumer.class);
        Consumer<String> correct2 = mock(Consumer.class);
        String consumed = "f";
        Either<?, String> base = Either.right(consumed);

        base.onRight(correct1).onRight(correct2);

        verify(correct1).accept(consumed);
        verify(correct2).accept(consumed);
    }

    @Test
    void map_shouldDoNothing_onLeft() {
        Function<Number, Heading> mapper = mock(Function.class);
        Either<String, Integer> base = Either.left("error");

        Either<String, Dist> mapped = base.map(mapper);

        assertThat(mapped).isSameAs(base);
        verify(mapper, never()).apply(any());
    }

    @Test
    void map_shouldTransform_onRight() {
        double expected = 1d;
        Either<String, Double> base = Either.right(expected);

        Either<String, Dbl> mapped = base.map(v -> new Dist(v, "m"));

        assertThat(mapped).right().satisfies(r ->
                assertThat(r.getVal()).isEqualTo(1, within(0.1))
        );
    }

    @Test
    void mapNullable_shouldTransform_onRight_whenNonNull() {
        String expected = "km";
        Either<String, Dist> base = Either.right(new Dist(0.1, expected));

        Either<String, String> mapped = base.mapNullable(Dist::getDimension, "no dimension");

        assertThat((Either<?, String>) mapped).right().isSameAs(expected);
    }

    @Test
    void mapNullable_shouldReturnLeft_onRight_whenNull() {
        String expected = "oops";
        Either<String, Dist> base = Either.right(new Dist(0.1, null));

        Either<String, String> mapped = base.mapNullable(Dist::getDimension, expected);

        assertThat((Either<? extends String, ?>) mapped).left().isSameAs(expected);
    }

    @Test
    void mapLeft_shouldTransform_onLeft() {
        String expected = "foo";
        Either<String, Dist> base = Either.left(expected);

        Either<Oops, Dist> mapped = base.mapLeft(Oops::new);

        assertThat(mapped).left().satisfies(l ->
                assertThat(l.getArg()).isEqualTo(expected)
        );
    }

    @Test
    void mapLeft_shouldDoNothing_onRight() {
        Either<String, Dist> base = Either.right(e0);

        Either<IllegalStateException, Dist> mapped = base.mapLeft(IllegalStateException::new);

        assertThat(mapped).isSameAs(base);
    }

    @Test
    void orElse_shouldReturnOtherAndConsumeAlternate_onLeft() {
        Either<String, Dist> base = Either.left("f");

        Dist actual = base.orElse(e0);

        assertThat(actual).isSameAs(e0);
    }

    @Test
    void orElse_shouldReturnValue_onRight() {
        Either<String, Dist> base = Either.right(neg);

        Dist actual = base.orElse(e0);

        assertThat(actual).isSameAs(neg);
    }

    @Test
    void orElseRecover_shouldUseMapper_onLeft() {
        Integer expected = 1;
        Either<String, Integer> base = Either.left("a");

        Integer actual = base.orElseRecover(String::length);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void orElseRecover_shouldReturnValue_onRight() {
        Function<String, Dist> alt = mock(Function.class);
        Either<String, Dist> base = Either.right(e0);

        Dist actual = base.orElseRecover(alt);

        assertThat(actual).isSameAs(e0);
        verify(alt, never()).apply(anyString());
    }

    @Test
    void orElseThrow_shouldUseMapper_onLeft() {
        Throwable t = catchThrowable(() -> Either.left("a").orElseThrow(Oops::new));
        assertThat(t).isInstanceOf(Oops.class);
    }

    @Test
    void orElseThrow_shouldReturnValue_onRight() {
        Either<String, Dist> base = Either.right(e0);

        Dist actual = base.orElseThrow(s -> new AssertionError("should not have been called (" + s + ")"));

        assertThat(actual).isSameAs(e0);
    }

    @Test
    void stream_souldWork() {
        double actual = canRetry.stream()
                .flatMap(Either::stream)
                .mapToDouble(Dbl::getVal)
                .filter(v -> v > 5d)
                .sum();

        assertThat(actual).isEqualTo(18.4, within(0.01));
    }

    @Test
    void streamLeft_souldWork() {
        long actual = canRetry.stream()
                .flatMap(Either::streamLeft)
                .filter(b -> b)
                .count();

        assertThat(actual).isEqualTo(2);
    }

    @Test
    void swap_shouldWork_onLeft() {
        String expected = "a";
        Either<String, Dist> base = Either.left(expected);

        Either<Dist, String> swapped = base.swap();

        assertThat((Either<?, String>) swapped).right().isSameAs(expected);
    }

    @Test
    void swap_shouldWork_onRight() {
        Either<String, Dist> base = Either.right(neg);

        Either<Dist, String> swapped = base.swap();

        assertThat((Either<? extends Dist, ?>) swapped).left().isSameAs(neg);
    }

    @Test
    void toOptional_shouldReturnEmpty_whenLeft() {
        Either<String, Dist> base = Either.left("error");
        Optional<Dist> opt = base.toOptional();

        assertThat(opt).isEmpty();
    }

    @Test
    void toOptional_shouldReturnValue_whenRight() {
        Either<String, Dist> base = Either.right(e0);
        Optional<Dist> opt = base.toOptional();

        assertThat(opt).containsSame(e0);
    }

    @Test
    void toString_shouldWrap_onLeft() {
        String expected = "Left[a]";
        Either<String, ?> base = Either.left("a");

        String actual = base.toString();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toString_shouldWrap_onRight() {
        String expected = "Right[dist(0.22m)]";
        Either<?, Dbl> base = Either.right(new Dist(0.217d, "m"));

        String actual = base.toString();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toTry_shouldFail_onLeft_andNullMapper() {
        Throwable t = catchThrowable(() -> Either.left("any").toTry(null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("exceptionMapper must exist");
    }

    @Test
    void toTry_shouldReturnFailure_onLeft() {
        Try<?> actual = Either.left("uh-oh").toTry(IllegalStateException::new);

        assertThat(actual).failureOfType(IllegalStateException.class);
    }

    @Test
    void toTry_shouldReturnSuccess_onRight() {
        Try<Dist> actual = Either.right(e0)
                .toTry(left -> new IllegalArgumentException("unacceptable value: " + left));

        assertThat(actual).success().isSameAs(e0);
    }
}