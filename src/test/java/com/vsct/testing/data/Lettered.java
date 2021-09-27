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

import com.vsct.component.util.immucoll.GroupedImmutables;
import com.vsct.component.util.immucoll.ImmutableCollection;

import java.util.Map;

public class Lettered<STRINGS extends ImmutableCollection<String>>
        extends GroupedImmutables<Letter, String, STRINGS> {

    public Lettered(Map<Letter, STRINGS> groups, STRINGS empty) {
        super(groups, empty);
    }

    public static Lettered<UniqueStrings> uniq(Map<Letter, UniqueStrings> groups) {
        return new Lettered<>(groups, UniqueStrings.of());
    }
}
