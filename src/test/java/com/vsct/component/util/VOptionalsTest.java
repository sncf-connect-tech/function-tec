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

package com.vsct.component.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

import com.vsct.testing.tool.TrackingSupplier;

class VOptionalsTest {

    private static class LenGT1 implements Predicate<String> {
        private final AtomicInteger invokCount = new AtomicInteger(0);

        @Override
        public boolean test(final String s) {
            invokCount.incrementAndGet();
            return s != null && s.length() > 1;
        }

        int invoked() {
            return invokCount.get();
        }
    }

    private AtomicInteger index = new AtomicInteger(0);

    @Test
    void firstThat_shouldReturnEmpty_whenNoAlternate() {
        assertThat(VOptionals.firstThat(Objects::nonNull)).isEmpty();
    }

    @Test
    void firstThat_shouldFail_whenNoCondition() {
        final Supplier<String> os1 = () -> "foo";

        Throwable t = catchThrowable(() -> VOptionals.firstThat(null, os1));

        assertThat(t).isInstanceOf(NullPointerException.class);
    }

    @Test
    void firstThat_shouldInvokeAll_whenAllAlternateAreRejected() {
        final LenGT1 condition = new LenGT1();
        final TrackingSupplier<String> os1 = new TrackingSupplier<>("", index);
        final TrackingSupplier<String> os2 = new TrackingSupplier<>(null, index);
        final TrackingSupplier<String> os3 = new TrackingSupplier<>("a", index);

        assertThat(VOptionals.firstThat(condition, os1, os2, os1, os3)).isEmpty();
        os1.assertCalls(0, 2);
        os2.assertCalls(1);
        os3.assertCalls(3);
        assertThat(condition.invoked()).isEqualTo(4);
    }

    @Test
    void firstThat_shouldStopInvoking_whenAlternateReturnsPassing() {
        final String expected = "a";
        final TrackingSupplier<String> os1 = new TrackingSupplier<>(expected, index);
        final TrackingSupplier<String> os2 = new TrackingSupplier<>(null, index);
        final TrackingSupplier<String> os3 = new TrackingSupplier<>("b", index);

        assertThat(VOptionals.firstThat(Objects::nonNull, os1, os2, os3)).contains(expected);
        os1.assertCalls(0);
        os2.assertCalls();
        os3.assertCalls();
    }
}