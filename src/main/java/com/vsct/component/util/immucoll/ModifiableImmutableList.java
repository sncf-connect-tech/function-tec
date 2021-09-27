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

import java.util.Comparator;

/**
 * Spécialisation d'{@link ModifiableImmutableCollection} pour une {@code List}.
 *
 * @since 1.0
 */
public interface ModifiableImmutableList<ELEM, SELF extends ModifiableImmutableList<ELEM, SELF>>
        extends ImmutableList<ELEM>, ModifiableImmutableCollection<ELEM, SELF> {

    /**
     * Réordonne les éléments.
     *
     * <p>L'instance courante n'est <strong>pas</strong> modifiée : une nouvelle instance
     * contiendra les éléments ordonnés selon le comparateur fourni.
     *
     * @param comparator critère de tri
     * @return l'instance courante si tous les éléments étaient déjà "dans le bon sens",
     *         une nouvelle instance sinon
     */
    SELF sort(Comparator<? super ELEM> comparator);
}
