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
 * Union disjointe
 * <p>
 * Implementation "supposée correcte", c'est à dire que les méthodes {@code get}, {@code map}, {@code flatMap}
 * et {@code stream} (sans précision) s'appliquent sur les instances {@link Right} et qu'il faut préciser sinon
 * (respectivement {@code getLeft}, {@code mapLeft}, {@code flatMapLeft} et {@code streamLeft})
 * <p>
 * Inspiré de ce que javaslang (les frustrés du scala :P) propose, mais en plus simple :
 * pas de projection, d'itération custom, ...
 *
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public abstract class Either<L, R> {

    // ======================================================================================
    //
    // factory methods
    //
    // ======================================================================================

    public static <A, V> Either<A, V> left(A alternate) {
        Objects.requireNonNull(alternate, "alternate value must exist");
        return new Left<>(alternate);
    }
    public static <A, V> Either<A, V> right(V value) {
        Objects.requireNonNull(value, "right value must exist");
        return new Right<>(value);
    }

    /**
     *
     * @param <A>       type de donnée alternatif
     * @param <V>       type de donnée "correcte"
     * @param opt       conteneur de la valeur correcte potentielle
     * @param alternate valeur alternative
     * @return nouvelle instance
     */
    public static <A, V> Either<A, V> fromOptionalOr(Optional<V> opt, A alternate) {
        return opt.isPresent() ? right(opt.get()) : left(alternate);
    }

    /**
     *
     * @param <A>           type de donnée alternatif
     * @param <V>           type de donnée "correcte"
     * @param opt           conteneur de la valeur correcte potentielle
     * @param lazyAlternate valeur alternative, évaluée uniquement si {@code opt} est vide
     * @return nouvelle instance
     */
    public static <A, V> Either<A, V> fromOptionalOrGet(Optional<V> opt, Supplier<? extends A> lazyAlternate) {
        return opt.isPresent() ? right(opt.get()) : left(lazyAlternate.get());
    }

    /**
     * "super Optional",
     * évite d'instancier un {@code Optional} intermédiaire avant d'appeler {@link #fromOptionalOr(Optional, Object)}.
     *
     * @param <A>       type de donnée alternatif
     * @param <V>       type de donnée "correcte"
     * @param value     valeur correcte potentielle
     * @param alternate valeur alternative
     * @return nouvelle instance
     */
    public static <A, V> Either<A, V> ofNullableOr(V value, A alternate) {
        return value != null ? right(value) : left(alternate);
    }

    /**
     * "super Optional",
     * évite d'instancier un {@code Optional} intermédiaire avant d'appeler {@link #fromOptionalOrGet(Optional, Supplier)}.
     *
     * @param <A>           type de donnée alternatif
     * @param <V>           type de donnée "correcte"
     * @param value         valeur correcte potentielle
     * @param lazyAlternate valeur alternative, évaluée uniquement si {@code opt} est {@code null}
     * @return nouvelle instance
     */
    public static <A, V> Either<A, V> ofNullableOrGet(V value, Supplier<? extends A> lazyAlternate) {
        return value != null ? right(value) : left(lazyAlternate.get());
    }

    /**
     *
     * @param <V>    type de retour de la méthode encapsulée dans le {@code Supplier}
     * @param unsafe définition de l'exécution qui peut lever une exception.
     * @return résultat ou exception levée
     */
    public static <V> Either<RuntimeException, V> trying(Supplier<V> unsafe) {
        Objects.requireNonNull(unsafe, "execution definition must exist");
        try {
            final V result = unsafe.get();
            return right(result);
        } catch (RuntimeException r) {
            return left(r);
        }
    }

    // ======================================================================================
    //
    // contracts
    //
    // ======================================================================================

    /**
     *
     * @param <LL>        nouveau type de valeur alternative
     * @param <RR>        nouveau type de valeur correcte
     * @param leftMapper  transformation de valeur alternative
     * @param rightMapper transformation de valeur correcte
     * @return nouvelle instance contenant le résultat de la transformation adequate.
     */
    public <LL,RR> Either<LL,RR> bimap(Function<? super L, ? extends LL> leftMapper, Function<? super R, ? extends RR> rightMapper) {
        if (isRight()) {
            Objects.requireNonNull(rightMapper, "rightMapper must exist");
            return right(rightMapper.apply(get()));
        } else {
            Objects.requireNonNull(leftMapper, "leftMapper must exist");
            return left(leftMapper.apply(getLeft()));
        }
    }

    protected abstract String descr();

    @Override
    public abstract boolean equals(final Object o);

    /**
     * Permet de passer d'une valeur correcte à une alternative si la condition n'est pas respectée.
     * Un appel à {@link #flatMap(Function)} permet de faire la même chose, mais peut être verbeux pour les cas les plus simples.
     *
     * @param condition contrat à respecter pour la valeur correcte
     * @param otherwise valeur alternative à retourner si la condition n'est pas respectée.
     * @return même instance ou {@link Left}.
     */
    public Either<L, R> filter(final Predicate<? super R> condition, final L otherwise) {
        if (isRight() && !condition.test(get())) {
            return left(otherwise);
        }
        return this;
    }

    public <RR> Either<L, RR> flatMap(Function<? super R, Either<L, ? extends RR>> mapper) {
        return (Either<L, RR>) this;
    }

    /**
     * Méthode de "récupération" isomorphe,
     * puisqu'elle permet de renvoyer une valeur correcte à partir d'une alternative :
     * <pre>
     * private Either&lt;DateTime, T> retry(DateTime backoffTTL) {
     *     sleep(10);
     *     if (backoffTTL.isBeforeNow()) {
     *         return businessProcess(); // qui peut réussir cette fois
     *     } else {
     *         return left(backoffTTL);
     *     }
     * }
     * ...
     *    // ici, le type d'alternative ne change pas
     *    Either&lt;DateTime, T> polite = businessProcess().flatMapLeft(this::retry);
     * </pre>
     *
     * @param <LL>   nouveau type de valeur alternative
     * @param mapper
     * @return résultat de la transformation si contenait une valeur alternative, la même instance sinon.
     * @see #orElseRecover(Function) pour une autre forme de "récupération"
     */
    public <LL> Either<LL, R> flatMapLeft(Function<? super L, Either<? extends LL, R>> mapper) {
        return (Either<LL, R>) this;
    }

    public <O> O fold(Function<? super L, ? extends O> leftMapper, Function<? super R, ? extends O> rightMapper) {
        if (isRight()) {
            Objects.requireNonNull(rightMapper, "rightMapper must exist");
            return rightMapper.apply(get());
        } else {
            Objects.requireNonNull(leftMapper, "leftMapper must exist");
            return leftMapper.apply(getLeft());
        }
    }

    /**
     * Accesseur "dangereux" sur la valeur correcte.
     *
     * @return valeur correcte (jamais {@code null}), si elle existe
     * @throws UnsupportedOperationException sinon
     */
    public abstract R get();

    /**
     * Accesseur "dangereux" sur la valeur alternative.
     *
     * @return valeur alternative, si elle existe
     * @throws UnsupportedOperationException sinon
     */
    public abstract L getLeft();

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), isRight() ? get() : getLeft());
    }

    /**
     * @return {@code true} ssi contient une valeur "correcte".
     */
    public abstract boolean isRight();

    /**
     * @param <RR>   nouveau type de valeur correcte
     * @param mapper transformation à appliquer à une valeur correcte
     * @return nouvelle instance contenant le résultat de la transformation si contenait une valeur correcte, la même instance sinon.
     */
    public <RR> Either<L, RR> map(Function<? super R, ? extends RR> mapper) {
        return (Either<L, RR>) this;
    }
    /**
     * @param <LL>   nouveau type de valeur alternative
     * @param mapper transformation à appliquer à une valeur alternative
     * @return nouvelle instance contenant le résultat de la transformation si contenait une valeur alternative, la même instance sinon.
     */
    public <LL> Either<LL, R> mapLeft(Function<? super L, ? extends LL> mapper) {
        return (Either<LL, R>) this;
    }

    /**
     * "Raccourci" syntaxique pour un cas simple (au lieu d'utiliser {@link #flatMap(Function)})
     *
     * @param <RR>      nouveau type de valeur correcte
     * @param mapper    transformation à appliquer à une valeur correcte
     * @param alternate valeur alternative lorsque que le résultat de la transformation vaut {@code null}
     * @return nouvelle instance ou réutilisation si déjà incorrecte
     * .
     */
    public <RR> Either<L, RR> mapNullable(Function<? super R, ? extends RR> mapper, L alternate) {
        return (Either<L, RR>) this;
    }

    /**
     * @param consumer à appliquer à la valeur alternative, si elle existe
     * @return instance, pour chainage
     */
    public Either<L, R> onLeft(Consumer<? super L> consumer) {
        if (!isRight()) {
            Objects.requireNonNull(consumer, "consumer must exist");
            consumer.accept(getLeft());
        }
        return this;
    }
    /**
     * @param consumer à appliquer à la valeur correcte, si elle existe
     * @return instance, pour chainage
     */
    public Either<L, R> onRight(Consumer<? super R> consumer) {
        if (isRight()) {
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
    public abstract R orElse(R other);

    /**
     * Renvoie la valeur correcte si elle existe, ou la transformation de la valeur alternative sinon.
     *
     * @param recoveryMapper "traducteur" de valeur alternative vers valeur correcte par défaut
     * @return valeur correcte ou transformation de valeur alternative
     * @throws NullPointerException si valeur alternative et {@code recoveryMapper} est null
     */
    public abstract R orElseRecover(Function<? super L, ? extends R> recoveryMapper);

    /**
     * Renvoie la valeur correcte si elle existe, ou jette l'exception fourni par la fonction sinon.
     *
     * @param <X> Type de l'exception à lever
     * @param exceptionMapper transformation valeur alternative -> exception
     * @return valeur correcte
     * @throws X si valeur alternative
     * @throws NullPointerException si valeur alternative et {@code exceptionMapper} est null
     */
    public abstract <X extends Throwable> R orElseThrow(Function<? super L, ? extends X> exceptionMapper) throws X;

    /**
     * @return un {@code Stream} contenant la valeur correcte (vide si représente une alternative)
     */
    public Stream<R> stream() {
        return Stream.empty();
    }
    /**
     * @return un {@code Stream} contenant la valeur alternative (vide si représente une correcte)
     */
    public Stream<L> streamLeft() {
        return Stream.empty();
    }

    /**
     * @return instance "inverse"
     */
    public Either<R, L> swap() {
        return isRight() ? new Left<>(get()) : new Right<>(getLeft());
    }

    /**
     * Conversion vers java, avec perte d'information en cas de <em>Left</em>.
     *
     * @return {@link Optional}, vide quand cette instance est une <em>Left</em>
     */
    public abstract Optional<R> toOptional();

    @Override
    public final String toString() {
        return String.format("%s[%s]", descr(), isRight() ? get() : getLeft());
    }

    /**
     * Changement de nature, avec conversion en cas de <em>Left</em>.
     *
     * @param exceptionMapper conversion de la valeur alternative en {@code RuntimeException}
     * @return {@link Try}, vide quand cette instance est une <em>Left</em>
     */
    public abstract Try<R> toTry(Function<? super L, ? extends RuntimeException> exceptionMapper);

    // ======================================================================================
    //
    // implementations
    //
    // ======================================================================================

    /**
     * Valeur alternative ("erreur")
     */
    public static final class Left<L, R> extends Either<L, R> {
        private final L value;
        private Left(L value) {
            this.value = value;
        }

        @Override
        protected String descr() {
            return "Left";
        }
        @Override
        public <LL> Either<LL, R> flatMapLeft(final Function<? super L, Either<? extends LL, R>> mapper) {
            Objects.requireNonNull(mapper, "mapper must exist");
            return (Either<LL, R>) mapper.apply(value);
        }
        @Override
        public boolean equals(final Object o) {
            return o == this || o instanceof Left && ((Left) o).value.equals(value);
        }
        @Override
        public R get() {
            throw new UnsupportedOperationException("on Left");
        }
        @Override
        public L getLeft() {
            return value;
        }
        @Override
        public boolean isRight() {
            return false;
        }
        @Override
        public <LL> Either<LL, R> mapLeft(final Function<? super L, ? extends LL> mapper) {
            Objects.requireNonNull(mapper, "mapper must exist");
            return left(mapper.apply(value));
        }
        @Override
        public R orElse(final R other) {
            return other;
        }
        @Override
        public R orElseRecover(final Function<? super L, ? extends R> recoveryMapper) {
            Objects.requireNonNull(recoveryMapper, "recoveryMapper must exist");
            return recoveryMapper.apply(value);
        }
        @Override
        public <X extends Throwable> R orElseThrow(final Function<? super L, ? extends X> exceptionMapper) throws X {
            Objects.requireNonNull(exceptionMapper, "exceptionMapper must exist");
            throw exceptionMapper.apply(value);
        }
        @Override
        public Stream<L> streamLeft() {
            return Stream.of(value);
        }
        @Override
        public Optional<R> toOptional() {
            return Optional.empty();
        }

        @Override
        public Try<R> toTry(Function<? super L, ? extends RuntimeException> exceptionMapper) {
            Objects.requireNonNull(exceptionMapper, "exceptionMapper must exist");
            return Try.failure(exceptionMapper.apply(value));
        }
    }

    /**
     * Valeur "correcte"
     */
    public static final class Right<L, R> extends Either<L, R> {
        private final R value;
        private Right(R value) {
            this.value = value;
        }

        @Override
        protected String descr() {
            return "Right";
        }
        @Override
        public <RR> Either<L, RR> flatMap(final Function<? super R, Either<L, ? extends RR>> mapper) {
            Objects.requireNonNull(mapper, "mapper must exist");
            return (Either<L, RR>) mapper.apply(value);
        }
        @Override
        public R get() {
            return value;
        }
        @Override
        public L getLeft() {
            throw new UnsupportedOperationException("on Right");
        }
        @Override
        public boolean equals(final Object o) {
            return o == this || o instanceof Right && ((Right) o).value.equals(value);
        }
        @Override
        public boolean isRight() {
            return true;
        }
        @Override
        public <RR> Either<L, RR> map(final Function<? super R, ? extends RR> mapper) {
            Objects.requireNonNull(mapper, "mapper must exist");
            return right(mapper.apply(value));
        }
        @Override
        public <RR> Either<L, RR> mapNullable(Function<? super R, ? extends RR> mapper, L alternate) {
            Objects.requireNonNull(mapper, "mapper must exist");
            return Either.ofNullableOr(mapper.apply(value), alternate);
        }
        @Override
        public R orElse(final R other) {
            return value;
        }
        @Override
        public R orElseRecover(final Function<? super L, ? extends R> recoveryMapper) {
            return value;
        }
        @Override
        public <X extends Throwable> R orElseThrow(final Function<? super L, ? extends X> exceptionMapper) throws X {
            return value;
        }
        @Override
        public Stream<R> stream() {
            return Stream.of(value);
        }
        @Override
        public Optional<R> toOptional() {
            return Optional.of(value);
        }

        @Override
        public Try<R> toTry(Function<? super L, ? extends RuntimeException> exceptionMapper) {
            return Try.success(value);
        }
    }
}
