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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("rawtypes")
class KnownTypeDelegates {

    static final TypeDelegate ARRAY_LIST = new TypeDelegate() {
        @Override
        public Collector<?, ?, ? extends List> collector() {
            return toList();
        }

        @Override
        public List newHolder() {
            return new ArrayList<>();
        }
    };

    static final TypeDelegate HASH_SET = new TypeDelegate() {
        @Override
        public Collector<?, ?, ? extends Set> collector() {
            return toSet();
        }

        @Override
        public Set newHolder() {
            return new HashSet<>();
        }
    };

    static class TreeSetDelegate<E> implements TypeDelegate<E, NavigableSet<E>> {

        private final Comparator<E> order;
        TreeSetDelegate(Comparator<E> order) {
            this.order = order;
        }

        @Override
        public Collector<E, ?, ? extends NavigableSet<E>> collector() {
            return toCollection(() -> new TreeSet<>(order));
        }

        @Override
        public NavigableSet<E> newHolder() {
            return new TreeSet<>(order);
        }
    }
}
