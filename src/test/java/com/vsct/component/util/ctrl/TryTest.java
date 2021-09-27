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
import com.vsct.testing.data.Oops;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
public class TryTest {

    private static final RuntimeException NONE = new NoSuchElementException("empty");
    private static final RuntimeException FAILED_ROTATION = new IllegalArgumentException("no direction to rotate from");

    private final Dist e0 = new Dist(0d, "cm");
    private final Dist e2 = new Dist(2d, "m");
    private final Dist neg = new Dist(-1d, "dm");
    private final List<Try<Dist>> dists = asList(
            Try.success(e2),
            Try.failure(new IllegalArgumentException("a")),
            Try.failure(new UnsupportedOperationException("b")),
            Try.success(new Dist(10d, "km")),
            Try.success(new Dist(8.4d, "mm")),
            Try.failure(new NullPointerException("c")),
            Try.success(e0)
    );

    private Try<Heading> rotate(Dist e) {
        if (e instanceof Heading) {
            return Try.success(new Heading(e.getVal(), e.getDimension(), ((Heading) e).getDir().next()));
        } else {
            return Try.failure(FAILED_ROTATION);
        }
    }

    private Try<Dist> trappedRecovery(RuntimeException from) {
        throw new UnsupportedOperationException("attempted to recover from " + from.getClass().getName());
    }

    private int calledTwoMeters = 0;

    private Try<Dist> twoMeters(Object toRespectFunctionInterface) {
        ++calledTwoMeters;
        return Try.success(e2);
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
    void failure_shouldFail_onNull() {
        Throwable t = catchThrowable(() -> Try.failure(null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("failure cause must exist");
    }

    @Test
    void success_shouldFail_onNull() {
        Throwable t = catchThrowable(() -> Try.success(null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("value must exist");
    }

    @Test
    void exec_shouldFail_onNullFunction() {
        Throwable t = catchThrowable(() -> Try.exec(null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("execution definition must exist");
    }

    @Test
    void exec_shouldReturnFailure_whenFailed() {
        Try<String> actual = Try.exec(() -> unsafe(true));

        assertThat(actual).failureOfType(Oops.class);
    }

    @Test
    void exec_shouldReturnSuccess_whenNoException() {
        Try<String> actual = Try.exec(() -> unsafe(false));

        assertThat(actual).success().isEqualTo("success");
    }

    @Test
    void fromOptional_shouldFail_whenEmpty_andNullSupplier() {
        Throwable t = catchThrowable(() -> Try.fromOptional(Optional.empty(), null));

        assertThat(t).isInstanceOf(NullPointerException.class);
    }

    @Test
    void fromOptional_shouldReturnFailure_whenEmpty() {
        Try<String> actual = Try.fromOptional(Optional.empty(), NoSuchElementException::new);

        assertThat(actual).failureOfType(NoSuchElementException.class);
    }

    @Test
    void fromOptional_shouldReturnSuccess_whenPresent() {
        final Supplier<NoSuchElementException> alt = mock(Supplier.class);
        final String expected = "ok";
        Try<String> actual = Try.fromOptional(Optional.of(expected), alt);

        assertThat(actual).success().isSameAs(expected);
        verify(alt, never()).get();
    }

    @Test
    void ofNullable_shouldReturnFailure_whenNull() {
        final Dist val = null;
        final Try<Dist> actual = Try.ofNullable(val);

        assertThat(actual).failureOfType(NullPointerException.class);
    }

    @Test
    void ofNullable_shouldReturnFailure_whenNull_andSpecificExc() {
        final Dist val = null;
        final Try<Dist> actual = Try.ofNullable(val, IllegalArgumentException::new);

        assertThat(actual).failureOfType(IllegalArgumentException.class);
    }

    @Test
    void ofNullable_shouldFail_whenNull_andNullSupplier() {
        Throwable t = catchThrowable(() -> Try.ofNullable(null, null));

        assertThat(t).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ofNullable_shouldReturnSuccess_whenNotNull() {
        final Supplier<IllegalArgumentException> alt = mock(Supplier.class);
        final Dist val = e0;
        final Try<Dist> actual = Try.ofNullable(val, alt);

        assertThat(actual).success().isSameAs(val);
        verify(alt, never()).get();
    }

    // ======================================================================================
    //
    // contracts
    //
    // ======================================================================================

    @Test
    void equals_shouldDeferToValue_onLeft() {
        final RuntimeException e = new IllegalStateException("mooh");
        final Try<?> one = Try.failure(e);
        final Try<?> bis = Try.failure(e);
        final Try<?> two = Try.failure(NONE);

        assertThat(one).isEqualTo(bis);
        assertThat(one).isNotEqualTo(two);
    }

    @Test
    void equals_shouldDeferToValue_onRight() {
        final Try<Long> one = Try.success(1L);
        final Try<Long> bis = Try.success(1L);
        final Try<Long> two = Try.success(2L);

        assertThat(one).isEqualTo(bis);
        assertThat(one).isNotEqualTo(two);
    }

    @Test
    void equals_shouldLookupContainer() {
        final Try<?> oneL = Try.failure(new NoSuchElementException("empty"));
        final Try<Long> oneR = Try.success(1L);

        assertThat(oneR).isNotEqualTo(oneL);
    }

    @Test
    void filter_shouldDoNothing_onFailure() {
        final Predicate<Dist> pred = mock(Predicate.class);
        final Try<Dist> base = Try.failure(NONE);

        final Try<Dist> filtered = base.filter(pred);

        assertThat(filtered).isSameAs(base);
        verify(pred, never()).test(any(Dist.class));
    }

    @Test
    void filter_shouldFail_onSuccess_andNullPredicate() {
        Try<String> base = Try.success("yeah");

        Throwable t = catchThrowable(() -> base.filter(null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("filtering a Success with a null Predicate");
    }

    @Test
    void filter_shouldFail_onSuccess_andNullRejectFunction() {
        Try<String> base = Try.success("yeah");

        Throwable t = catchThrowable(() -> base.filter(o -> false, null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("filtering a Success with a null rejection Function");
    }

    @Test
    void filter_shouldDoNothing_onMatchingSuccess() {
        final Try<String> base = Try.success("foo");

        final Try<String> filtered = base.filter(s -> s.length() > 1);

        assertThat(filtered).isSameAs(base);
    }

    @Test
    void filter_shouldReturnFailure_onNonmatchingSuccess() {
        final Try<String> base = Try.success("");

        final Try<String> filtered = base.filter(s -> s.length() > 1);

        assertThat(filtered).failureOfType(FailedPredicateException.class);
    }

    @Test
    void filter_shouldReturnSpecifiedFailure_onNonmatchingSuccess() {
        final Try<String> base = Try.success("");

        final Try<String> filtered = base.filter(s -> s.length() > 1, Oops::new);

        assertThat(filtered).failureOfType(Oops.class);
    }

    @Test
    void flatMap_shouldFail_onSuccess_andNullFunction() {
        Try<String> base = Try.success("yeah");

        Throwable t = catchThrowable(() -> base.flatMap(null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("mapper must exist");
    }

    @Test
    void flatMap_shouldDoNothing_onFailure() {
        final Try<Dist> base = Try.failure(NONE);

        final Try<Double> flatMapped = base.flatMap(Dbl::tryInvert);

        assertThat(flatMapped).isSameAs(base);
    }

    @Test
    void flatMap_shouldTransform_onSuccess() {
        final Try<Dist> base = Try.success(new Heading(1d, "m", Direction.W));
        final Heading expected = new Heading(1d, "m", Direction.N);

        final Try<Dist> flatMapped = base.flatMap(this::rotate);

        assertThat(flatMapped).success().isEqualTo(expected);
    }

    @Test
    void flatMap_couldReturnFailure_onSuccess() {
        final Try<Dist> base = Try.success(e0);

        final Try<Dist> flatMapped = base.flatMap(this::rotate);

        assertThat(flatMapped).failureOfType(IllegalArgumentException.class);
    }

    @Test
    void flatRecover_shouldDoNothing_onSuccess() {
        final Try<Dbl> base = Try.success(e0);

        final Try<Dbl> flatRecovered = base.flatRecover(IllegalArgumentException.class, this::twoMeters);

        assertThat(flatRecovered).isSameAs(base);
        assertThat(calledTwoMeters).isEqualTo(0);
    }

    @Test
    void flatRecover_shouldDoNothing_onNonmatchingFailure() {
        final Try<Dbl> base = Try.failure(NONE);

        final Try<Dbl> flatRecovered = base.flatRecover(NullPointerException.class, this::twoMeters);

        assertThat(flatRecovered).isSameAs(base);
        assertThat(calledTwoMeters).isEqualTo(0);
    }

    @Test
    void flatRecover_shouldTransform_onMatchingFailure() {
        final Try<Dist> base = Try.failure(NONE);

        final Try<Dist> flatRecovered = base.flatRecover(RuntimeException.class, this::twoMeters);

        assertThat(flatRecovered).success().isSameAs(e2);
    }

    @Test
    void flatRecover_shouldNotLooseOrigExc_onMatchingFailure_andFailingMapper() {
        final Try<Dist> base = Try.failure(NONE);

        final Try<Dist> flatRecovered = base.flatRecover(RuntimeException.class, this::trappedRecovery);

        assertThat(flatRecovered).failureOfType(FailedRecoveryException.class).satisfies(wrap -> {
            assertThat(wrap.getOriginalFailure()).isSameAs(NONE);
            assertThat(wrap).hasCauseInstanceOf(UnsupportedOperationException.class);
        });
    }

    @Test
    void flatRecover_shouldFail_onFailure_andNullContract() {
        final Try<Dist> base = Try.failure(NONE);

        Throwable t = catchThrowable(() -> base.flatRecover(null, this::twoMeters));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("failure cause type must exist");
    }

    @Test
    void flatRecover_shouldFail_onFailure_andNullMapper() {
        final Try<Dist> base = Try.failure(NONE);

        Throwable t = catchThrowable(() -> base.flatRecover(IllegalArgumentException.class, null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("recovery mapper must exist");
    }

    @Test
    void get_shouldThrowCause_onFailure() {
        final Try<Dist> base = Try.failure(NONE);

        Throwable t = catchThrowable(base::get);

        assertThat(t).isSameAs(NONE);
    }

    @Test
    void get_shouldReturnValue_onSuccess() {
        final String expected = "a";
        final String actual = Try.success(expected).get();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getCause_shouldReturnExc_onFailure() {
        final RuntimeException actual = Try.failure(NONE).getCause();

        assertThat(actual).isSameAs(NONE);
    }

    @Test
    void getCause_shouldFail_onSuccess() {
        Try<String> base = Try.success("a");

        Throwable t = catchThrowable(() -> base.getCause());

        assertThat(t).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("on Success");
    }

    @Test
    void hashCode_shouldDeferToValue_onLeft() {
        final Try<?> one = Try.failure(NONE);
        final Try<?> bis = Try.failure(NONE);
        final Try<?> two = Try.failure(new NullPointerException());

        assertThat(one.hashCode()).isEqualTo(bis.hashCode());
        assertThat(one.hashCode()).isNotEqualTo(two.hashCode());
    }

    @Test
    void hashCode_shouldDeferToValue_onRight() {
        final Try<Long> one = Try.success(1L);
        final Try<Long> bis = Try.success(1L);
        final Try<Long> two = Try.success(2L);

        assertThat(one.hashCode()).isEqualTo(bis.hashCode());
        assertThat(one.hashCode()).isNotEqualTo(two.hashCode());
    }

    @Test
    void hashCode_shouldLookupContainer() {
        final Try<?> oneL = Try.failure(NONE);
        final Try<Throwable> oneR = Try.success(NONE);

        assertThat(oneR.hashCode()).isNotEqualTo(oneL.hashCode());
    }

    @Test
    void map_shouldFail_onSuccess_andNullFunction() {
        final Throwable t = catchThrowable(() -> Try.success("yeah").map(null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("mapper must exist");
    }

    @Test
    void map_shouldDoNothing_onFailure() {
        final Function<Number, Heading> mapper = mock(Function.class);
        final Try<Integer> base = Try.failure(NONE);

        final Try<Dist> mapped = base.map(mapper);

        assertThat(mapped).isSameAs(base);
        verify(mapper, never()).apply(any());
    }

    @Test
    void map_shouldTransform_onSuccess() {
        final double expected = 1d;
        final Try<Double> base = Try.success(expected);

        final Try<Dbl> mapped = base.map(v -> new Dist(v, "m"));

        assertThat(mapped.isSuccess()).isTrue();
        assertThat(mapped.get().getVal()).isEqualTo(expected, within(0.01));
    }

    @Test
    void map_shouldReturnFailure_onSuccess_andNullTransformation() {
        final Try<Dist> base = Try.success(new Dist(1d, null));

        final Try<String> mapped = base.map(Dist::getDimension);

        assertThat((Try<?>) mapped).failureOfType(NullPointerException.class);
    }

    @Test
    void mapNullable_shouldReturnFailure_onSuccess_andNullTransformation() {
        final Try<Dist> base = Try.success(new Dist(1d, null));

        final Try<String> mapped = base.mapNullable(Dist::getDimension, () -> new Oops("no dimension!"));

        assertThat((Try<?>) mapped).failureOfType(Oops.class);
    }

    @Test
    void onFailure_shouldExecute_onMatchingFailure() {
        final Consumer<RuntimeException> alt1 = mock(Consumer.class);
        final Consumer<Object> alt2 = mock(Consumer.class);
        final Try<?> base = Try.failure(NONE);

        base.onFailure(NoSuchElementException.class, alt1).onFailure(RuntimeException.class, alt2);

        verify(alt1).accept(NONE);
        verify(alt2).accept(NONE);
    }

    @Test
    void onFailure_shouldDoNothing_onNonmatchingFailure() {
        final Consumer<Object> alt = mock(Consumer.class);
        final Try<?> base = Try.failure(NONE);

        base.onFailure(Oops.class, alt);

        verify(alt, never()).accept(any());
    }

    @Test
    void onFailure_shouldDoNothing_onSuccess() {
        final Consumer<Object> alt = mock(Consumer.class);
        final Try<Dist> base = Try.success(e0);

        base.onFailure(RuntimeException.class, alt);

        verify(alt, never()).accept(any());
    }

    @Test
    void onSuccess_shouldDoNothing_onFailure() {
        final Consumer<String> correct = mock(Consumer.class);
        final Try<String> base = Try.failure(NONE);

        base.onSuccess(correct);

        verify(correct, never()).accept(anyString());
    }

    @Test
    void onSuccess_shouldExecute_onSuccess() {
        final Consumer<String> correct1 = mock(Consumer.class);
        final Consumer<Object> correct2 = mock(Consumer.class);
        final String consumed = "f";
        final Try<String> base = Try.success(consumed);

        base.onSuccess(correct1).onSuccess(correct2);

        verify(correct1).accept(consumed);
        verify(correct2).accept(consumed);
    }

    @Test
    void orElse_shouldReturnOther_onFailure() {
        final Try<Dist> base = Try.failure(NONE);

        final Dist actual = base.orElse(e0);

        assertThat(actual).isSameAs(e0);
    }

    @Test
    void orElse_shouldReturnValue_onSuccess() {
        final Try<Dist> base = Try.success(neg);

        final Dist actual = base.orElse(e0);

        assertThat(actual).isSameAs(neg);
    }

    @Test
    void recover_shouldUseMapper_onFailure() {
        final String expected = "1";
        final Try<String> base = Try.failure(NONE);

        final Try<String> actual = base.recover(NoSuchElementException.class, x -> expected);

        assertThat(actual).success().isSameAs(expected);
    }

    @Test
    void recover_shouldDoNothing_onSuccess() {
        final Function<Exception, Dist> alt = mock(Function.class);
        final Try<Dist> base = Try.success(e0);

        final Try<Dist> actual = base.recover(RuntimeException.class, alt);

        assertThat(actual).success().isSameAs(e0);
        verify(alt, never()).apply(any());
    }

    @Test
    void recover_shouldFail_onFailure_andNullContract() {
        final Try<Dist> base = Try.failure(NONE);

        final Throwable t = catchThrowable(() -> base.recover(null, x -> e0));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("failure cause type must exist");
    }

    @Test
    void recover_shouldFail_onFailure_andNullMapper() {
        final Try<Dist> base = Try.failure(NONE);

        final Throwable t = catchThrowable(() -> base.recover(RuntimeException.class, null));

        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("recovery mapper must exist");
    }

    @Test
    void stream_souldWork() {
        final double expected = 18.4d;
        final double actual = dists.stream()
                .flatMap(Try::stream)
                .mapToDouble(Dbl::getVal)
                .filter(v -> v > 5d)
                .sum();
        assertThat(actual).isEqualTo(expected, within(0.01));
    }

    @Test
    void toEither_shouldReturnLeft_whenMatchingFailure() {
        Either<NoSuchElementException, ?> actual = Try.failure(NONE).toEither(NoSuchElementException.class);
        assertThat((Either<? extends RuntimeException, ?>) actual).left().isSameAs(NONE);
    }

    @Test
    void toEither_shouldFail_whenMatchingFailure() {
        final Throwable t = catchThrowable(() -> Try.failure(NONE).toEither(NullPointerException.class));
        assertThat(t).isInstanceOf(ClassCastException.class)
                .hasMessage("expected java.lang.NullPointerException but was java.util.NoSuchElementException");
    }

    @Test
    void toEither_shouldReturnRight_whenSuccess() {
        Either<IllegalArgumentException, Dist> actual = Try.success(e0).toEither(IllegalArgumentException.class);
        assertThat((Either<?, Dist>) actual).right().isSameAs(e0);
    }

    @Test
    void toOptional_shouldReturnEmpty_whenFailure() {
        final Try<Dist> base = Try.failure(NONE);
        final Optional<Dist> opt = base.toOptional();

        assertThat(opt).isEmpty();
    }

    @Test
    void toOptional_shouldReturnValue_whenSuccess() {
        final Try<Dist> base = Try.success(e0);
        final Optional<Dist> opt = base.toOptional();

        assertThat(opt.get()).isSameAs(e0);
    }

    @Test
    void toString_shouldWrap_onFailure() {
        final String expected = "Failure[java.lang.IllegalArgumentException: " + FAILED_ROTATION.getMessage() + "]";
        final Try<?> base = Try.failure(FAILED_ROTATION);

        final String actual = base.toString();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toString_shouldWrap_onSuccess() {
        final String expected = "Success[dist(0.22m)]";
        final Try<Dbl> base = Try.success(new Dist(0.217d, "m"));

        final String actual = base.toString();

        assertThat(actual).isEqualTo(expected);
    }
}