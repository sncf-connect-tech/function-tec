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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Base pour un regroupement de valeurs dans des conteneurs immuables, avec une clef de répartition libre.
 *
 * @param <KEY>   type de la clef de groupement
 * @param <ELEM>  type d'élément groupé
 * @param <VALUE> type concret d'{@link ImmutableCollection} qui encapsule chaque groupe
 * @since 1.0
*/
public abstract class GroupedImmutables<KEY extends GroupingKey<KEY>, ELEM, VALUE extends ImmutableCollection<ELEM>> {

    private final VALUE empty;
    private final Map<KEY, VALUE> groups;
    private final Set<KEY> nonEmpty;

    protected GroupedImmutables(Map<KEY, VALUE> groups, VALUE empty) {
        Objects.requireNonNull(empty, "empty value must exist");
        Objects.requireNonNull(groups, "initial groups must exist");
        this.empty = empty;
        this.groups = Map.copyOf(groups);
        nonEmpty = groups.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return isSameAs((GroupedImmutables<?, ?, ?>) o);
    }

    /**
     * @param key indication du groupe demandé
     * @return le groupe demandé, ou {@link #empty} (donc jamais {@code null})
     */
    public VALUE get(KEY key) {
        return groups.getOrDefault(key, empty);
    }

    /**
     * @param key indication du groupe demandé
     * @return {@code true} ssi ce groupe contient au moins un élement
     */
    public boolean has(KEY key) {
        return nonEmpty.contains(key);
    }

    @Override
    public int hashCode() {
        return groups.hashCode();
    }

    /**
     * ATTENTION : les groupes vides <b>comptent</b>.
     * En conséquence, 2 instances considérées égales peuvent avoir des tailles différentes !
     *
     * @return nombre de groupes référencés
     */
    public int size() {
        return groups.size();
    }

    public Stream<Map.Entry<KEY, VALUE>> streamEntries() {
        return groups.entrySet().stream();
    }

    @Override
    public String toString() {
        return groups.toString();
    }

    private boolean hasSameValues(Map<?, ?> candidate) {
        for (KEY key : nonEmpty) {
            if (!groups.get(key).equals(candidate.get(key))) {
                return false;
            }
        }
        return true;
    }

    private boolean isSameAs(GroupedImmutables<?, ?, ?> other) {
        return nonEmpty.equals(other.nonEmpty) && hasSameValues(other.groups);
    }
}
