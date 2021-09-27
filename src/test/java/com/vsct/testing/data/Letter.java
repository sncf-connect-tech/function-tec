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

import com.vsct.component.util.immucoll.GroupingKey;

public enum Letter implements GroupingKey<Letter> {
    ODD, EVEN, UNDEFINED;

    public static Letter resolve(String challenge) {
        if (challenge.length() != 1) {
            return UNDEFINED;
        }

        final char c = challenge.toUpperCase().charAt(0);
        if (c < 'A' || c > 'Z') {
            return UNDEFINED;
        }

        // en tant que 1ère lettre de l'alphabet, on considère que A est "impaire"
        // raison pour laquelle l'enum commence par ODD
        return values()[(c - 'A') % 2];
    }
}
