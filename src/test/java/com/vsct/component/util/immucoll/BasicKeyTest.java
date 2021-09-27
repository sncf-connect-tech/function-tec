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

package com.vsct.component.util.immucoll;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class BasicKeyTest {

    private final BasicKey tested = new BasicKey("some");

    @Test
    void new_should_fail_on_null() {
        // Act
        Throwable t = catchThrowable(() -> new BasicKey(null));

        // Assert
        assertThat(t).isInstanceOf(NullPointerException.class)
                .hasMessage("key value must exist");
    }

    @Test
    void compareTo_should_defer_to_value() {
        // Arrange
        BasicKey before = new BasicKey("before");
        BasicKey after = new BasicKey("zboui");
        BasicKey[] array = { after, before, tested };

        // Act
        Arrays.sort(array);

        // Assert
        assertThat(array).containsExactly(before, tested, after);
    }

    @Test
    void equals_should_defer_to_content() {
        // Arrange
        BasicKey other = new BasicKey("some");

        // Act & Assert
        assertThat(tested.equals(other)).as("value-object equality").isTrue();
    }

    @Test
    void hashCode_should_defer_to_content() {
        // Act & Assert
        assertThat(tested.hashCode()).isEqualTo("some".hashCode());
    }

    @Test
    void name_should_return_value() {
        // Act & Assert
        assertThat(tested.name()).isEqualTo("some");
    }

    @Test
    void toString_should_return_value() {
        // Act & Assert
        assertThat(tested.toString()).isEqualTo("some");
    }
}