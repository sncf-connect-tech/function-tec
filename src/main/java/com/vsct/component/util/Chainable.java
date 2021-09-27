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

import java.util.function.UnaryOperator;

/**
 * Trait permettant d'appliquer une modification isomorphe à un objet.
 *
 * @param <SELF> type d'objet
 * @since 1.0
 */
public interface Chainable<SELF extends Chainable<SELF>> {

    /**
     * Pour faciliter les chaînages d'invocations.
     *
     * @param transform modification souhaitée
     * @return l'instance courante ou une nouvelle, selon la transformation spécifiée
     */
    @SuppressWarnings("unchecked")
    default SELF then(UnaryOperator<SELF> transform) {
        return transform.apply((SELF) this);
    }
}
