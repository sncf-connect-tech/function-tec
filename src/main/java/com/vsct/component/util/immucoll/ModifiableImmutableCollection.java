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

import com.vsct.component.util.Chainable;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Définit les méthodes "de modification".
 *
 * <p>S'agissant de liste <em>immutable</em>, ce n'est bien sûr pas l'instance sur
 * laquelle on l'invoque qui est modifiée.
 * Ces méthodes renvoient une nouvelle instance, où la modification est appliquée
 *
 * @param <ELEM> type d'élément listés
 * @param <SELF> instance concrète courante
 *
 * @since 1.0
 */
public interface ModifiableImmutableCollection<ELEM, SELF extends ModifiableImmutableCollection<ELEM, SELF>>
        extends Chainable<SELF>, ImmutableCollection<ELEM> {

    /**
     * Ajout d'un élément unique.
     *
     * <p>L'instance courante n'est <strong>pas</strong> modifiée : une nouvelle instance
     * contiendra les éléments courants, plus celui spécifié.
     *
     * @param elem nouveau membre
     * @return nouvelle instance ne contenant tous les éléments courants, suivi de l'élément fourni.
     *
     * @see #plus(ModifiableImmutableCollection)
     * @see Builder#add(Object)
     */
    SELF add(ELEM elem);

    /**
     * Ajout d'éléments "en vrac".
     *
     * <p>L'instance courante n'est <strong>pas</strong> modifiée : une nouvelle instance
     * contiendra les éléments courants, plus ceux spécifiés.
     *
     * @param elems nouveaux membres
     * @return nouvelle instance ne contenant tous les éléments courants, suivi des éléments fournis.
     *
     * @see #plus(ModifiableImmutableCollection)
     * @see Builder#addAll(Collection)
     */
    SELF addAll(Collection<? extends ELEM> elems);

    /**
     * Restreint les éléments à conserver.
     *
     * <p>L'instance courante n'est <strong>pas</strong> modifiée : une nouvelle instance
     * contiendra les éléments valides.
     *
     * @param condition critère de sélection
     * @return l'instance courante si tous les éléments satisfont la condition,
     *         une nouvelle instance ne contenant que ceux-là sinon
     *
     * @see #minus(ModifiableImmutableCollection)
     */
    SELF filter(Predicate<ELEM> condition);

    /**
     * Restreint les éléments à conserver.
     *
     * <p>L'instance courante n'est <strong>pas</strong> modifiée : une nouvelle instance
     * contiendra la "soustraction" des éléments.
     *
     * @param unwanted conteneur des valeurs "interdites"
     * @return instance ne contenant que les éléments de cette instance qui n'étaient pas listé dans le paramètre
     *
     * @see #filter(Predicate)
     * @see #remove(Object)
     * @see #removeAll(Collection)
     */
    SELF minus(SELF unwanted);

    /**
     * Ajoute des éléments tiers à ceux connus de cette instance.
     *
     * <p>L'instance courante n'est <strong>pas</strong> modifiée : une nouvelle instance
     * contiendra "l'addition" des éléments.
     *
     * @param other autre instance à "combiner"
     * @return instance contenant les éléments de cette instance suivis de ceux de l'autre instance
     */
    SELF plus(SELF other);

    /**
     * Retrait d'un élément (de tous ses "représentants" s'il est répété).
     *
     * <p>L'instance courante n'est <strong>pas</strong> modifiée : une nouvelle instance
     * contiendra les éléments courants, moins celui spécifié.
     *
     * <p>Simple "sucre syntaxique" d'une action qu'on peut déjà réaliser via
     * <pre>{@code
     * .filter(e -> !unwanted.equals(e))
     * }</pre>
     *
     * @param unwanted membre à retirer
     * @return nouvelle instance ne contenant tous les éléments courants, privé de l'élément fourni.
     *
     * @see #filter(Predicate)
     * @see #minus(ModifiableImmutableCollection)
     */
    SELF remove(ELEM unwanted);

    /**
     * Retrait d'éléments "en vrac".
     *
     * <p>L'instance courante n'est <strong>pas</strong> modifiée : une nouvelle instance
     * contiendra les éléments courants, moins ceux spécifiés.
     *
     * @param unwanted membres à retirer
     * @return nouvelle instance ne contenant tous les éléments courants, privé de l'élément fourni.
     *
     * @see #minus(ModifiableImmutableCollection)
     */
    SELF removeAll(Collection<? extends ELEM> unwanted);
}
