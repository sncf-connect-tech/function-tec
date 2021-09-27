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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ExpiringMemoizedSupplierTest {

    private static final String A = "a", B = "b";

    @Mock
    private Supplier<String> delegate;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);

        when(delegate.get()).thenReturn(A).thenReturn(B);
    }

    @Test
    void shouldDelegateAgain_afterDuration() throws InterruptedException {
        final Supplier<String> memoized = new ExpiringMemoizedSupplier<>(delegate, 300, TimeUnit.MILLISECONDS);
        final String call1 = memoized.get();
        Thread.sleep(400);
        final String call2 = memoized.get();
        final String call3 = memoized.get();

        assertThat(call1).isSameAs(A);
        assertThat(call2).isSameAs(B);
        assertThat(call3).isSameAs(B);
    }

    @Test
    void shouldDelegateAgain_afterDuration_javaTime() throws InterruptedException {
        final Supplier<String> memoized = new ExpiringMemoizedSupplier<>(delegate, Duration.ofMillis(200));
        final String call1 = memoized.get();
        final String call2 = memoized.get();
        Thread.sleep(250);
        final String call3 = memoized.get();

        assertThat(call1).isSameAs(A);
        assertThat(call2).isSameAs(A);
        assertThat(call3).isSameAs(B);
    }
}