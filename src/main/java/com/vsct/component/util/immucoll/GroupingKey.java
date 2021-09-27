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

/**
 * Contrat d'une clef de groupement : porte un nom et permet un tri.
 *
 * <p>Les classes qui implémentent ce contrat devraient<ol>
 *     <li>être immuables : il n'est pas acceptable qu'une clef "change",
 *     <li>se comporter en <i>Value Object</i> : leur égalité ne devrait dépendre
 *     <b>que</b> de leur contenu (raison de plus d'être immuable ;] ).
 * </ol>
 *
 * <p>La forme choisie permet de l'appliquer directement sur une {@code enum}, sans
 * autre ajout que la clause d'implémentation :
 * <pre>{@code public enum Foo implements GroupingKey<Foo> {
 *     D, B, Z
 * }}</pre>
 *
 * @since 1.0
 */
public interface GroupingKey<SELF extends GroupingKey<SELF>> extends Comparable<SELF> {
    String name();
}
