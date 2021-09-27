/**
 * Contient les briques <strong>de base</strong> pour gérer une
 * {@link java.util.Collection} de manière immuable.
 * Il vous appartient de les étendre pour leur donner une identité propre
 * à votre domaine fonctionnel.
 *
 * <p>Une implémentation est proposée pour {@link java.util.List}
 * (basée sur {@link java.util.ArrayList}) et {@link java.util.Set}
 * (basée sur {@link java.util.HashSet} ou {@link java.util.TreeSet},
 * au choix de la classe fille)
 *
 * <p>Le mécanisme de « précision » utilisé pour {@code Set} est
 * extensible pour gérer tout type de {@code Collection} nécessaire à
 * votre besoin.
 */
package com.vsct.component.util.immucoll;