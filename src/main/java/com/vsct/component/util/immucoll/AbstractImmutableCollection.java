package com.vsct.component.util.immucoll;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * Implémente les comportements communs pour un <em>Value Object</em> qui représente
 * <strong>uniquement</strong> une liste immuable d'éléments.
 *
 * <p>Alors certes, <em>composition over inheritance</em> et tout ça.
 * Mais c'est quand même bien pénible de se fader la redéfinition de toutes ces méthodes à chaque fois
 * qu'on veut implémenter une liste "read only".
 *
 * @param <ELEM> type d'élément collectés
 * @param <SELF> instance concrète courante
 *
 * @see <a href="https://williamdurand.fr/2013/06/03/object-calisthenics/#4-first-class-collections">calisthenic #4</a>
 * @since 1.0
 */
public abstract class AbstractImmutableCollection<ELEM, TYPE extends Collection<ELEM>, SELF extends AbstractImmutableCollection<ELEM, TYPE, SELF>>
        implements ModifiableImmutableCollection<ELEM, SELF> {

    final TYPE inner;
    private final Function<? super TYPE, SELF> newSelf;
    final TypeDelegate<ELEM, ? extends TYPE> typeDelegate;

    protected AbstractImmutableCollection(TYPE inner,
                                          Function<? super TYPE, SELF> newSelf,
                                          TypeDelegate<ELEM, ? extends TYPE> typeDelegate) {
        Objects.requireNonNull(inner, "inner collection must exist");
        Objects.requireNonNull(newSelf, "copy constructor must exist");
        Objects.requireNonNull(typeDelegate, "type delegate must exist");

        this.inner = inner.stream().collect(typeDelegate.collector()); // copie défensive
        this.newSelf = newSelf;
        this.typeDelegate = typeDelegate;
    }

    @Override
    public SELF addAll(Collection<? extends ELEM> elems) {
        typeDelegate.requireNoNull(elems, "elements to add");
        if (elems.isEmpty()) {
            return self();
        }

        TYPE combined = copyValues();
        combined.addAll(elems);
        return wrap(combined);
    }

    /* IDE-generated */
    @Override
    public boolean equals(Object o) {
        if (this == o)  return true;
        if (o == null || !getClass().equals(o.getClass())) return false;
        return inner.equals(((AbstractImmutableCollection<?, ?, ?>) o).inner);
    }

    @Override
    public SELF filter(Predicate<ELEM> condition) {
        typeDelegate.requireNonNull(condition, "filtering condition must exist");
        final TYPE filtered = inner.stream().filter(condition).collect(typeDelegate.collector());
        return filtered.size() == inner.size() ? self() : wrap(filtered);
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public Iterator<ELEM> iterator() {
        return new ReadOnlyIterator();
    }

    @Override
    public SELF minus(SELF unwanted) {
        Objects.requireNonNull(unwanted, "elements to remove must exist");
        return removeAll(unwanted.inner);
    }

    @Override
    public SELF plus(SELF other) {
        typeDelegate.requireNonNull(other, "elements to add must exist");
        // cas simples : un des 2 est vide, on renvoit l'autre
        if (other.isEmpty()) {
            return self();
        } else if (this.isEmpty()) {
            return other;
        }

        // concaténation
        TYPE combined = copyValues();
        combined.addAll(other.inner);
        return wrap(combined);
    }

    @Override
    public SELF remove(ELEM unwanted) {
        if (unwanted == null) {
            // comme on refuse de stocker `null` (cf. Builder, dans l'interface parente),
            // on sait qu'on ne le trouvera pas
            return self();
        }
        return filter(e -> !unwanted.equals(e));
    }

    @Override
    public SELF removeAll(Collection<? extends ELEM> unwanted) {
        // pas un problème d'avoir des "null" comme éléments à retirer : ils ne seront pas trouvés
        typeDelegate.requireNonNull(unwanted, "elements to remove must exist");

        // cas simple : intersection vide, on n'a rien à retirer
        if (stream().noneMatch(unwanted::contains)) {
            return self();
        }

        // restriction des valeurs
        TYPE retained = copyValues();
        retained.removeAll(unwanted);
        return wrap(retained);
    }

    @Override
    public Stream<ELEM> stream() {
        return inner.stream();
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public String toString() {
        return inner.toString();
    }

    //
    // limités aux "enfants"
    //

    /**
     * @return une <em>shallow copy</em> des valeurs maintenues par cette instance
     */
    protected TYPE copyValues() {
        return inner.stream().collect(typeDelegate.collector());
    }

    /**
     * Permet de grouper les valeurs courantes selon un critère libre, en conservant le type de
     * collection immuable courant pour chaque groupe.
     *
     * @param classifier       détermination de la clef de groupement à partir d'un élément à grouper
     * @param groupConstructor constructeur de l'instance concrète du conteneur des groupes
     * @param <KEY>            type de clef de groupement
     * @param <GROUP>          type concret de conteneur des groupes
     * @return nouvelle instance de groupe
     */
    protected <KEY extends GroupingKey<KEY>, GROUP extends GroupedImmutables<KEY, ELEM, SELF>> GROUP groupBy(
            Function<ELEM, KEY> classifier,
            Function<Map<KEY, SELF>, GROUP> groupConstructor
    ) {
        final Collector<ELEM, ?, SELF> valueCollector = new ImmCollector<>(newSelf, typeDelegate);
        final Map<KEY, SELF> groups = stream().collect(groupingBy(classifier, valueCollector));
        return groupConstructor.apply(groups);
    }

    /**
     * Permet de grouper les valeurs courantes selon un critère libre, puis de les transformer avant de
     * les stocker dans leur collection immuable dédiée.
     *
     * <p>par exemple :
     * <pre>{@code
     * // Contracts.java
     * public class Contracts extends AbstractImmutableList<Contract, Contracts> {
     *    public CustomersBy<State> groupCustomersByState() {
     *        return groupByThenMap(
     *                Contract::getState,
     *                contract -> contract.getCustomers().stream(),
     *                Customers::new,
     *                TypeDelegate.hashSet(),
     *                CustomersBy::new
     *        );
     *    }
     *    // + mandatory stuff
     * }
     *
     * // CustomersBy.java
     * public class CustomersBy<K> extends GroupedImmutable<K, Customer, Customers> { ... }
     *
     * // Customers.java
     * public class Customers implements ImmuableSet<Customer, Set<Customer>> { ... }
     * }</pre>
     * avec : <ul>
     * <li>{@code class Contract} une prestation avec un état et relative à des clients,
     * <li>{@code class Customer} un client,
     * <li>{@code enum State} l'état d'une prestation.
     * </ul>
     *
     * @param classifier       détermination de la clef de groupement à partir d'un élément de base
     * @param elementMapper    transformation de l'élément de base
     * @param valueConstructor constructeur de la collection immuable
     * @param typeDelegate     aide
     * @param groupConstructor constructeur de l'instance concrète du conteneur des groupes
     * @param <K>              type de clef du groupement
     * @param <E>              type des objets issus de la transformations
     * @param <T>              type de collection de ces ojets encapsulée dans une version immuable
     * @param <V>              type concret de la collection immuable
     * @param <G>              type concret de conteneur des groupes
     * @return nouvelle instance de groupe
     */
    protected <K extends GroupingKey<K>, E, T extends Collection<E>, V extends ImmutableCollection<E>, G extends GroupedImmutables<K, E, V>> G groupByThenMap(
            Function<ELEM, K> classifier,
            Function<? super ELEM, Stream<? extends E>> elementMapper,
            Function<? super T, V> valueConstructor,
            TypeDelegate<E, ? extends T> typeDelegate,
            Function<Map<K, V>, G> groupConstructor
    ) {
        final Collector<ELEM, ?, V> valueCollector = new MappingCollector<>(valueConstructor, typeDelegate, elementMapper);
        final Map<K, V> groups = stream().collect(groupingBy(classifier, valueCollector));
        return groupConstructor.apply(groups);
    }

    /**
     * @return {@code this}, typé fortement
     */
    @SuppressWarnings("unchecked")
    protected SELF self() {
        return (SELF) this;
    }

    protected SELF wrap(TYPE newInner) {
        return newSelf.apply(newInner);
    }

    /**
     * Redéfinition par encapsulation
     */
    private class ReadOnlyIterator implements Iterator<ELEM> {
        private final Iterator<ELEM> delegate = inner.iterator();

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }
        @Override
        public ELEM next() {
            return delegate.next();
        }
        @Override
        public void remove() {
            final String fqName = AbstractImmutableCollection.this.getClass().getName();
            final int lastPoint = fqName.lastIndexOf(".");
            String noPackage = lastPoint < 0 ? fqName : fqName.substring(lastPoint + 1);
            throw new UnsupportedOperationException("cannot remove element of " + noPackage);
        }
    }

}
