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

import com.vsct.testing.data.Dbl;
import com.vsct.testing.data.Dist;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VFunctionsTest {

    @Test
    void memoize_mock() {
        final Dist expected = new Dist(3.5, "cm");
        final Supplier<Dist> inner = mock(Supplier.class);
        when(inner.get()).thenReturn(expected);

        // type-widening
        final Supplier<Dbl> actual = VFunctions.memoize(inner);

        final Dbl call1 = actual.get();
        final Dbl call2 = actual.get();

        assertThat(call1).isSameAs(expected);
        assertThat(call2).isSameAs(expected);
        verify(inner).get(); // i.e. UNE fois
    }

    @Test
    void memoize_memoized() {
        final Supplier<String> inner = VFunctions.memoize(() -> "a");

        final Supplier<String> actual = VFunctions.memoize(inner);

        assertThat(actual).isSameAs(inner);
    }

    @Test
    void memoizeForDuration_nullDuration() {
        Throwable t = catchThrowable(() -> VFunctions.memoizeForDuration(() -> "a", null));

        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duration must be > 0 (got null)");
    }

    @Test
    void memoizeForDuration_negDuration() {
        Throwable t = catchThrowable(() -> VFunctions.memoizeForDuration(() -> "a", Duration.ofMillis(-20)));

        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duration must be > 0 (got PT-0.02S)");
    }

    @Test
    void memoizeForDuration_zeroRawDuration() {
        Throwable t = catchThrowable(() -> VFunctions.memoizeForDuration(() -> "a", 0, TimeUnit.MINUTES));

        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duration must be > 0 (got 0 MINUTES)");
    }

    @Test
    void memoizeForDuration_negRawDuration() {
        Throwable t = catchThrowable(() -> VFunctions.memoizeForDuration(() -> "a", -20, TimeUnit.MILLISECONDS));

        assertThat(t).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duration must be > 0 (got -20 MILLISECONDS)");
    }

    @Test
    void memoizeForDuration_incrementalVal() throws InterruptedException {
        AtomicInteger seed = new AtomicInteger(1);
        Supplier<Integer> actual = VFunctions.memoizeForDuration(seed::getAndIncrement, Duration.ofMillis(30));

        Integer call1 = actual.get();
        Integer call2 = actual.get();
        Integer call3 = actual.get();
        Thread.sleep(31);
        Integer callAfter = actual.get();

        assertThat(call1).isEqualTo(1);
        assertThat(call2).isEqualTo(1);
        assertThat(call3).isEqualTo(1);
        assertThat(callAfter).isEqualTo(2);
    }

    @Test
    void memoizeForDuration_incrementalVal_raw() throws InterruptedException {
        AtomicInteger seed = new AtomicInteger(0);
        Supplier<Integer> actual = VFunctions.memoizeForDuration(seed::getAndIncrement, 15, TimeUnit.MILLISECONDS);

        Integer callBefore = actual.get();
        Thread.sleep(16);
        Integer call1 = actual.get();
        Integer call2 = actual.get();
        Integer call3 = actual.get();

        assertThat(callBefore).isEqualTo(0);
        assertThat(call1).isEqualTo(1);
        assertThat(call2).isEqualTo(1);
        assertThat(call3).isEqualTo(1);
    }
}