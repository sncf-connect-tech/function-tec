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

import static java.util.Objects.requireNonNull;

/**
 * Implémentation minimale du contrat {@link GroupingKey} adossée à une
 * simple {@code String}, pour les cas où une {@code enum} ne répond pas
 * au besoin (e.g.: les valeurs sont dynamiques).
 *
 * <p>Cette classe n'admet pas d'enfant : elle n'est pas une « base
 * de travail » sur laquelle on peut greffer des comportements par héritage.
 * <br/>Un développeur ainsi incliné est invité à recopier cette classe
 * (contrainte {@code final class} incluse !) et lui donner le complément voulu.
 * <br/>Sa classe sera ainsi déclarée {@code GroupingKey} (et donc
 * {@link Comparable}) sur le « bon » type.
 *
 * @since 1.0
 */
public final class BasicKey implements GroupingKey<BasicKey> {

    private final String value;

    public BasicKey(String value) {
        this.value = requireNonNull(value, "key value must exist");
    }

    @Override
    public int compareTo(BasicKey other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return (o instanceof BasicKey) && value.equals(((BasicKey) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String name() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
