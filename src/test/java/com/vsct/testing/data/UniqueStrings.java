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

import com.vsct.component.util.immucoll.AbstractImmutableSet;
import com.vsct.component.util.immucoll.TypeDelegate;

import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Implémentation basique, qui plus est sur un type "naturellement" immuable,
 * pour pouvoir tester tous les comportements de la classe abstraite.
 */
public class UniqueStrings extends AbstractImmutableSet<String, Set<String>, UniqueStrings> {

    public UniqueStrings(Set<String> inner) {
        super(inner, UniqueStrings::new, TypeDelegate.hashSet());
    }

    public static UniqueStrings of(String... values) {
        return new UniqueStrings(Arrays.stream(values).collect(toSet()));
    }
}
