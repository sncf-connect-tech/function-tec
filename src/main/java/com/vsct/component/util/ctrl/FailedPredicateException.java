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
package com.vsct.component.util.ctrl;

import java.util.function.Predicate;

/**
 * Spécialisation d'un échec lors d'un appel à {@link Try#filter(java.util.function.Predicate)}.
 *
 * @since 1.0
 */
public class FailedPredicateException extends RuntimeException {
    public FailedPredicateException(Predicate<?> p, Object rejected) {
        super(toMessage(p, rejected));
    }

    private static String toMessage(Predicate<?> p, Object rejected) {
        final StringBuilder s = new StringBuilder().append(rejected).append(" was rejected by ");
        if (p == null) {
            s.append("undescribed predicate");
        } else {
            s.append(p.getClass().getName());
        }
        return s.toString();
    }
}
