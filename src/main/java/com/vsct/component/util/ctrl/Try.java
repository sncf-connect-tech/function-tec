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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Résultat d'une exécution pouvant échouer.
 * <p>
 * Là encore, pas une simple copie de ce que vavr (ex-javaslang) propose, mais une version
 * "simple" et respectueuse des standards.
 *
 * @param <E> type du résultat attendu
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public abstract class Try<E> {

    // ======================================================================================
    //
    // factory methods
    //
    // ======================================================================================

    public static <V> Try<V> failure(RuntimeException cause) {
        Objects.requireNonNull(cause, "failure cause must exist");
        return new Failure<>(cause);
    }
    public static <V> Try<V> success(V value) {
        Objects.requireNonNull(value, "value must exist");
        return new Success<>(value);
    }

    /**
     *
     * @param <V>    type de retour de la méthode encapsulée dans le {@code Supplier}
     * @param unsafe définition de l'exécution qui peut lever une exception.
     * @return résultat ou exception levée
     */
    public static <V> Try<V> exec(Supplier<V> unsafe) {
        Objects.requireNonNull(unsafe, "execution definition must exist");
        try {
            final V result = unsafe.get();
            return success(result);
        } catch (RuntimeException x) {
            return failure(x);
        }
    }

    /**
     *
     * @param <V>    type de donnée
     * @param opt    conteneur de la donnée, potentiellement vide
     * @param absent générateur de l'échec
     * @return nouvelle instance
     */
    public static <V> Try<V> fromOptional(Optional<V> opt, Supplier<? extends RuntimeException> absent) {
        return opt.isPresent() ? success(opt.get()) : failure(absent.get());
    }

    /**
     * "super Optional", pour faciliter les chaînages.
     * {@code null} produira un {@link Failure} contenant un {@code NullPointerException}.
     *
     * @param <V>    type de donnée
     * @param value  valeur correcte potentielle
     * @return nouvelle instance
     */
    public static <V> Try<V> ofNullable(V value) {
        return ofNullable(value, NullPointerException::new);
    }

    /**
     * "super Optional",  pour faciliter les chaînages.
     * évite d'instancier un {@code Optional} intermédiaire avant d'appeler {@link #fromOptional(Optional, Supplier)}.
     *
     * @param <V>    type de donnée
     * @param value  valeur correcte potentielle
     * @param absent générateur de l'échec
     * @return nouvelle instance
     * @see #ofNullable(Object) version "simple" où le générateur d'échec produit un NullPointer sans message.
     */
    public static <V> Try<V> ofNullable(V value, Supplier<? extends RuntimeException> absent) {
        return value != null ? success(value) : failure(absent.get());
    }

    // ======================================================================================
    //
    // contracts
    //
    // ======================================================================================

    @Override
    public abstract boolean equals(final Object o);
    @Override
    public abstract int hashCode();

    /**
     * Permet de passer d'un succès à une erreur si la condition n'est pas respectée.
     *
     * @param condition contrat à respecter pour le succès
     * @return même instance ou <code>Failure[{@link FailedPredicateException}]</code>
     * @see #filter(Predicate, Function) pour utiliser une exception spécifique
     */
    public Try<E> filter(final Predicate<? super E> condition) {
        return filter(condition, val -> new FailedPredicateException(condition, val));
    }

    /**
     * Permet de passer d'un succès à une erreur choisie si la condition n'est pas respectée.
     *
     * @param <X>       type de marqueur d'échec
     * @param condition contrat à respecter pour le succès
     * @param reject    générateur du marqueur d'échec
     * @return même instance ou {@code Failure[X]}
     */
    public <X extends RuntimeException> Try<E> filter(Predicate<? super E> condition, Function<? super E, X> reject) {
        if (isSuccess()) {
            Objects.requireNonNull(condition, "filtering a Success with a null Predicate");
            Objects.requireNonNull(reject, "filtering a Success with a null rejection Function");
            try {
                if (!condition.test(get())) {
                    return failure(reject.apply(get()));
                }
            } catch (RuntimeException predicateExc) {
                return failure(predicateExc);
            }
        }
        return this;
    }

    public <EE> Try<EE> flatMap(Function<? super E, Try<? extends EE>> mapper) {
        return (Try<EE>) this;
    }

    /**
     * Méthode de "récupération" isomorphe
     *
     * @param from   critère d'application
     * @param mapper transformation
     * @return résultat de la transformation si contenait une valeur alternative compatible, la même instance sinon.
     * @see #recover(Class, Function) pour une autre forme de "récupération"
     */
    public <X extends RuntimeException> Try<E> flatRecover(Class<X> from, Function<? super X, Try<? extends E>> mapper) {
        return this;
    }

    /**
     * Accesseur "dangereux" sur la valeur correcte.
     *
     * @return valeur correcte (jamais {@code null}), si elle existe
     * @throws RuntimeException l'exception caractérisant l'échec
     */
    public abstract E get() throws RuntimeException;

    /**
     * Accesseur "dangereux" sur la cause de l'échec.
     *
     * @return exception (jamais {@code null}), si elle existe
     * @throws UnsupportedOperationException sinon
     */
    public abstract RuntimeException getCause();

    public boolean isFailure() {
        return !isSuccess();
    }

    /**
     * @param type contrat pour la cause d'erreur
     * @return {@code true} ssi représente une erreur causée par le type d'exception fourni
     */
    public boolean isFailure(Class<? extends RuntimeException> type) {
        return false;
    }

    /**
     * @return {@code true} ssi contient une valeur "correcte".
     */
    public abstract boolean isSuccess();

    /**
     * @param <EE>   nouveau type de valeur correcte
     * @param mapper transformation à appliquer à une valeur correcte
     * @return nouvelle instance contenant le résultat de la transformation si contenait une valeur correcte, la même instance sinon.
     */
    public <EE> Try<EE> map(Function<? super E, ? extends EE> mapper) {
        return (Try<EE>) this;
    }

    /**
     * Spécialisation de l'exception lorsque la transformation d'une valeur correcte produit {@code null}.
     *
     * @param <EE>   nouveau type de valeur correcte
     * @param mapper transformation à appliquer à une valeur correcte
     * @param absent générateur de l'échec, lorsque la transformation produit {@code null}
     * @return nouvelle instance contenant le résultat de la transformation si contenait une valeur correcte, la même instance sinon.
     *
     * @see #map(Function) suffisant si une {@code Failure} contenant {@code NullPointerException} est acceptable
     */
    public <EE> Try<EE> mapNullable(Function<? super E, ? extends EE> mapper, Supplier<? extends RuntimeException> absent) {
        return (Try<EE>) this;
    }

    /**
     * @param consumer à appliquer à la cause de l'erreur, si elle existe
     * @param type     condition d'applicaction (sur la nature de l'exception)
     * @return instance, pour chainage
     */
    public <X extends RuntimeException> Try<E> onFailure(Class<X> type, Consumer<? super X> consumer) {
        if (isFailure(type)) {
            Objects.requireNonNull(consumer, "consumer must exist");
            consumer.accept((X) getCause());
        }
        return this;
    }
    /**
     * @param consumer à appliquer à la valeur correcte, si elle existe
     * @return instance, pour chainage
     */
    public Try<E> onSuccess(Consumer<? super E> consumer) {
        if (isSuccess()) {
            Objects.requireNonNull(consumer, "consumer must exist");
            consumer.accept(get());
        }
        return this;
    }

    /**
     * Renvoie la valeur correcte si elle existe, ou la valeur par defaut sinon.
     *
     * @param other valeur renvoyée lorsqu'il n'y a pas de valeur correcte
     * @return valeur correcte ou par défaut
     */
    public abstract E orElse(E other);

    /**
     * Méthode de "récupération" conditionnelle
     *
     * @param from   critère d'application
     * @param mapper transformation
     * @return résultat de la transformation si contenait une valeur alternative compatible, la même instance sinon.
     * @see #flatRecover(Class, Function) pour une autre forme de "récupération"
     */
    public <X extends RuntimeException> Try<E> recover(final Class<X> from, Function<? super X, ? extends E> mapper) {
        return this;
    }

    /**
     * @return un {@code Stream} contenant la valeur correcte (vide si représente un échec)
     */
    public abstract Stream<E> stream();

    public <X extends RuntimeException> Either<X, E> toEither(Class<X> failureType) {
        if (isSuccess()) {
            return Either.right(get());
        } else if (isFailure(failureType)) {
            return Either.left((X) getCause());
        } else {
            throw new ClassCastException("expected " + failureType.getName() + " but was " + getCause().getClass().getName());
        }
    }

    /**
     * Conversion vers java, avec perte d'information en cas de <em>Failure</em>.
     *
     * @return {@link Optional}, vide quand cette instance est une <em>Failure</em>
     */
    public abstract Optional<E> toOptional();

    // ======================================================================================
    //
    // implementations
    //
    // ======================================================================================

    /**
     * Valeur alternative ("erreur")
     */
    public static final class Failure<E> extends Try<E> {
        private final RuntimeException cause;
        private Failure(RuntimeException value) {
            this.cause = value;
        }

        @Override
        public boolean equals(final Object o) {
            return o == this || o instanceof Failure && ((Failure) o).cause.equals(cause);
        }
        @Override
        public <X extends RuntimeException> Try<E> flatRecover(final Class<X> from, final Function<? super X, Try<? extends E>> mapper) {
            Objects.requireNonNull(mapper, "recovery mapper must exist");
            if (isFailure(from)) {
                try {
                    return (Try<E>) mapper.apply((X) cause);
                } catch (RuntimeException ex) {
                    return failure(new FailedRecoveryException(ex, cause));
                }
            }
            return this;
        }
        @Override
        public final int hashCode() {
            return Objects.hash(getClass(), cause);
        }
        @Override
        public E get() throws RuntimeException {
            throw cause;
        }
        @Override
        public RuntimeException getCause() {
            return cause;
        }

        @Override
        public boolean isFailure(final Class<? extends RuntimeException> type) {
            Objects.requireNonNull(type, "failure cause type must exist");
            return type.isInstance(cause);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }
        @Override
        public E orElse(final E other) {
            return other;
        }

        @Override
        public <X extends RuntimeException> Try<E> recover(Class<X> from, Function<? super X, ? extends E> mapper) {
            Objects.requireNonNull(mapper, "recovery mapper must exist");
            if (isFailure(from)) {
                try {
                    return success(mapper.apply((X) cause));
                } catch (RuntimeException ex) {
                    return failure(new FailedRecoveryException(ex, cause));
                }
            }
            return this;
        }

        @Override
        public Stream<E> stream() {
            return Stream.empty();
        }
        @Override
        public Optional<E> toOptional() {
            return Optional.empty();
        }
        @Override
        public final String toString() {
            return String.format("Failure[%s]", cause);
        }
    }

    /**
     * Valeur "correcte"
     */
    public static final class Success<E> extends Try<E> {
        private final E value;
        private Success(E value) {
            this.value = value;
        }

        @Override
        public <EE> Try<EE> flatMap(final Function<? super E, Try<? extends EE>> mapper) {
            Objects.requireNonNull(mapper, "mapper must exist");
            try {
                return (Try<EE>) mapper.apply(value);
            } catch (RuntimeException ex) {
                return failure(ex);
            }
        }
        @Override
        public E get() {
            return value;
        }
        @Override
        public RuntimeException getCause() {
            throw new UnsupportedOperationException("on Success");
        }
        @Override
        public boolean equals(final Object o) {
            return o == this || o instanceof Success && ((Success) o).value.equals(value);
        }
        @Override
        public final int hashCode() {
            return Objects.hash(getClass(), value);
        }
        @Override
        public boolean isSuccess() {
            return true;
        }
        @Override
        public <EE> Try<EE> map(final Function<? super E, ? extends EE> mapper) {
            Objects.requireNonNull(mapper, "mapper must exist");
            try {
                return success(mapper.apply(value));
            } catch (RuntimeException ex) {
                return failure(ex);
            }
        }
        @Override
        public <EE> Try<EE> mapNullable(Function<? super E, ? extends EE> mapper, Supplier<? extends RuntimeException> absent) {
            Objects.requireNonNull(mapper, "mapper must exist");
            try {
                final EE mapped = mapper.apply(value);
                return ofNullable(mapped, absent);
            } catch (RuntimeException ex) {
                return failure(ex);
            }
        }
        @Override
        public E orElse(final E other) {
            return value;
        }
        @Override
        public Stream<E> stream() {
            return Stream.of(value);
        }
        @Override
        public Optional<E> toOptional() {
            return Optional.of(value);
        }
        @Override
        public final String toString() {
            return String.format("Success[%s]", value);
        }
    }
}
