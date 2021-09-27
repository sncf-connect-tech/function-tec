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

package com.vsct.testing.data;

import static java.util.stream.Collectors.joining;
import java.util.*;

public class RefAndValues<C extends Collection<String>> {
    private final String ref;
    private final C values;

    private RefAndValues(String ref, C values) {
        this.ref = ref;
        this.values = values;
    }
    public static RefAndValues<List<String>> array(String ref) {
        return new RefAndValues<>(ref, new ArrayList<>());
    }
    public static RefAndValues<Set<String>> uniq(String ref, boolean ordered) {
        return new RefAndValues<>(ref, ordered ? new TreeSet<>() : new HashSet<>());
    }

    @Override
    public String toString() {
        return ref + ":[" + values.stream().collect(joining(",")) + "]";
    }

    public RefAndValues<C> with(String value) {
        values.add(value);
        return this;
    }

    public String getRef() {
        return ref;
    }

    public C getValues() {
        return values;
    }
}
