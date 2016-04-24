package com.github.rutledgepaulv.monads;

import com.github.rutledgepaulv.monads.supporting.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

/**
 * A Try monad. Allows for wrapping executions with captures of the result
 * or the exception that occurred if any. Provides standard monadic mapping
 * and flat mapping as well as conversions to streams and applying conditional
 * logic.
 *
 * @param <T>
 */
@SuppressWarnings("unused")
public abstract class Try<T> implements ToOptional<T>, ToStream<T> {
    private Try() {
    }

    private static <S, T extends Try<?>> Try<S> flatten(Try<T> nested, Function<? super T, Try<S>> func) {
        return nested.optional().map(func).orElseGet(() -> (Failure<S>)nested);
    }

    public static <S> Try<S> flatten2(Try<Try<S>> nested) {
        return flatten(nested, identity());
    }

    public static <S> Try<S> flatten3(Try<Try<Try<S>>> nested) {
        return flatten(nested, Try::flatten2);
    }

    public static <S> Try<S> flatten4(Try<Try<Try<Try<S>>>> nested) {
        return flatten(nested, Try::flatten3);
    }

    public static <S> Try<S> flatten5(Try<Try<Try<Try<Try<S>>>>> nested) {
        return flatten(nested, Try::flatten4);
    }

    public static <S> Try<S> flatten6(Try<Try<Try<Try<Try<Try<S>>>>>> nested) {
        return flatten(nested, Try::flatten5);
    }

    public static <S> Try<S> flatten7(Try<Try<Try<Try<Try<Try<Try<S>>>>>>> nested) {
        return flatten(nested, Try::flatten6);
    }

    public static <S> Try<S> flatten8(Try<Try<Try<Try<Try<Try<Try<Try<S>>>>>>>> nested) {
        return flatten(nested, Try::flatten7);
    }




    public static <IN1, IN2, OUT> BiFunction<IN1, IN2, Try<OUT>> lift(CheckedBiFunction<IN1,IN2,OUT,?> func) {
        return (in1, in2) -> of(() -> func.apply(in1, in2));
    }

    public static <IN, OUT> Function<IN, Try<OUT>> lift(CheckedFunction<IN, OUT, ?> func) {
        return in -> of(() -> func.apply(in));
    }

    public static <OUT> Supplier<Try<OUT>> lift(CheckedSupplier<OUT, ?> func) {
        return () -> of(func);
    }

    public static <IN1, IN2> BiFunction<IN1, IN2, Try<Void>> liftVoid(CheckedBiConsumer<IN1, IN2,?> func) {
        return (in1, in2) -> of(() -> func.accept(in1,in2));
    }

    public static <IN> Function<IN, Try<Void>> liftVoid(CheckedConsumer<IN, ?> consumer) {
        return (in) -> of(() -> consumer.accept(in));
    }

    public static Supplier<Try<Void>> liftVoid(CheckedRunnable<?> runnable) {
        return () -> of(runnable);
    }


    /**
     * Attempt to perform an operation that produces a result. Capture the result
     * of the function or else capture the exception if there was one.
     *
     * @param supplier The supplier to execute.
     * @param <S> The type of the value produced by the supplier.
     * @param <E> The type of the exception thrown by the supplier (if any).
     * @return A try capturing the result of the execution of the supplier.
     */
    public static <S, E extends Exception> Try<S> of(CheckedSupplier<S, E> supplier) {
        try {
            return Try.success(supplier.get());
        } catch (Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * Attempt to perform an operation that produces a result. Capture the
     * exception if there was one during the execution of the runnable.
     *
     * @param runnable The runnable to execute.
     * @param <E> The type of the exception thrown by the runnable (if any).
     * @return A try capturing the result of the execution of the runnable.
     */
    public static <E extends Exception> Try<Void> of(CheckedRunnable<E> runnable) {
        return of((CheckedSupplier<Void, ?>)() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Produces a failed try with the provided message.
     *
     * @param message The message to explain the failure.
     * @param <S> The type of the value of the try.
     * @return The failed try.
     */
    public static <S> Try<S> failure(String message) {
        return new Failure<>(new RuntimeException(message));
    }

    /**
     * Produces a failed try with the provided exception.
     *
     * @param e The exception to explain the failure.
     * @param <S> The type of the value of the try.
     * @return The failed try.
     */
    public static <S> Try<S> failure(Exception e) {
        return new Failure<>(e);
    }

    /**
     * Produces a successful try with the provided value.
     *
     * @param value The value contained by the successful try.
     * @param <S> The type of the value contained in the try.
     * @return The successful try.
     */
    public static <S> Try<S> success(S value) {
        return new Success<>(value);
    }

    /**
     * Produces a successful try with a null value.
     *
     * @return The successful try.
     */
    public static Try<Void> success() {
        return new Success<>(null);
    }

    public abstract T get() throws RuntimeException;

    public abstract T getChecked() throws Exception;

    public abstract boolean isSuccess();

    public abstract boolean isFailure();

    public abstract <E extends Exception> Try<T>
    onSuccess(CheckedConsumer<? super T, E> action) throws E;

    public abstract <E extends Exception> Try<T>
    onFailure(CheckedConsumer<? super Exception, E> action) throws E;

    public abstract <G extends Exception, E extends Exception> Try<T>
    onFailure(Class<G> clazz, CheckedConsumer<? super G, E> action) throws E;

    public abstract Optional<? extends Exception>
    contraOptional();

    public abstract <G extends Exception> Optional<? extends G>
    contraOptional(Class<G> clazz);

    public abstract Try<T>
    filter(Predicate<? super T> pred);

    public abstract T
    orElse(T value);

    public abstract T
    orElseGet(Supplier<? extends T> value);

    public abstract T
    orElseThrow() throws RuntimeException;

    public abstract T
    orElseThrowChecked() throws Exception;

    public abstract <E extends Exception> T
    orElseThrow(E throwable) throws E;

    public abstract <E extends Exception> T
    orElseThrow(Supplier<? extends E> exceptionSupplier) throws E;

    public abstract <E extends Exception> Try<T>
    orElseTry(CheckedSupplier<? extends T, E> f);

    public abstract <U, E extends Exception> Try<U>
    map(CheckedFunction<? super T, ? extends U, E> f);

    public abstract <U, E extends Exception> Try<U>
    flatMap(CheckedFunction<? super T, ? extends Try<U>, E> f);

    public abstract <S> Optional<S>
    contraMap(Function<? super Exception, ? extends S> func);

    public abstract <S> Optional<S>
    flatContraMap(Function<? super Exception, Optional<S>> func);

    public abstract <E extends Exception> Try<T>
    recover(CheckedFunction<? super Exception,? extends T, E> f);

    public abstract <E extends Exception> Try<T>
    flatRecover(CheckedFunction<? super Exception, ? extends Try<T>, E> f);

    public abstract <G extends Exception, E extends Exception> Try<T>
    recover(Class<G> clazz, CheckedFunction<? super Exception,? extends T, E> f);

    public abstract <G extends Exception, E extends Exception> Try<T>
    flatRecover(Class<G> clazz, CheckedFunction<? super Exception, ? extends Try<T>, E> f);


    private static class Success<S> extends Try<S> {

        private final S value;

        private Success(S value) {
            this.value = Objects.requireNonNull(value, "You cannot capture a null value.");
        }

        @Override
        public S get() throws RuntimeException {
            return value;
        }

        @Override
        public S getChecked() throws Exception {
            return value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public <E extends Exception> Try<S> onSuccess(CheckedConsumer<? super S, E> action) throws E {
            action.accept(value);
            return this;
        }

        @Override
        public <E extends Exception> Try<S> onFailure(CheckedConsumer<? super Exception, E> action) throws E {
            return this;
        }

        @Override
        public <G extends Exception, E extends Exception> Try<S>
        onFailure(Class<G> clazz, CheckedConsumer<? super G, E> action) throws E {
            return this;
        }

        @Override
        public Try<S> filter(Predicate<? super S> pred) {
            if (pred.test(value)) {
                return this;
            } else {
                return failure("Predicate filter resulted in the successful result being dropped.");
            }
        }

        @Override
        public S orElse(S value) {
            return get();
        }

        @Override
        public S orElseGet(Supplier<? extends S> value) {
            return get();
        }

        @Override
        public S orElseThrow() throws RuntimeException {
            return get();
        }

        @Override
        public S orElseThrowChecked() throws Exception {
            return getChecked();
        }

        @Override
        public <E extends Exception> S orElseThrow(E throwable) throws E {
            return get();
        }

        @Override
        public <E extends Exception> Try<S> orElseTry(CheckedSupplier<? extends S, E> f) {
            return this;
        }

        @Override
        public <X extends Exception> S orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return get();
        }

        @Override
        public <U, E extends Exception> Try<U> map(CheckedFunction<? super S, ? extends U, E> f) {
            try {
                return success(f.apply(this.value));
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public Optional<? extends Exception> contraOptional() {
            return Optional.empty();
        }

        @Override
        public <G extends Exception> Optional<? extends G> contraOptional(Class<G> clazz) {
            return Optional.empty();
        }

        @Override
        public <S1> Optional<S1> contraMap(Function<? super Exception, ? extends S1> func) {
            return Optional.empty();
        }

        @Override
        public <S1> Optional<S1> flatContraMap(Function<? super Exception,  Optional<S1>> func) {
            return Optional.empty();
        }

        @Override
        public <U, E extends Exception> Try<U> flatMap(CheckedFunction<? super S, ? extends Try<U>, E> f) {
            try {
                return f.apply(this.value);
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public <E extends Exception> Try<S> recover(CheckedFunction<? super Exception, ? extends S, E> f) {
            return this;
        }

        @Override
        public <E extends Exception> Try<S> flatRecover(CheckedFunction<? super Exception, ? extends Try<S>, E> f) {
            return this;
        }

        @Override
        public <G extends Exception, E extends Exception> Try<S> recover(Class<G> clazz,
                CheckedFunction<? super Exception, ? extends S, E> f) {
            return this;
        }

        @Override
        public <G extends Exception, E extends Exception> Try<S> flatRecover(Class<G> clazz,
                CheckedFunction<? super Exception, ? extends Try<S>, E> f) {
            return this;
        }

        @Override
        public Optional<S> optional() {
            return Optional.of(value);
        }

        @Override
        public Stream<S> stream() {
            return Stream.of(value);
        }
    }


    private static class Failure<S> extends Try<S> {

        private final Exception throwable;

        private Failure(Exception throwable) {
            this.throwable = throwable;
        }

        @Override
        public S get() throws RuntimeException {
            throw Optional.of(throwable)
                    .filter(RuntimeException.class::isInstance)
                    .map(RuntimeException.class::cast)
                    .orElseGet(() -> new RuntimeException(throwable));
        }

        @Override
        public S getChecked() throws Exception {
            throw throwable;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public <E extends Exception> Try<S> onSuccess(CheckedConsumer<? super S, E> action) throws E {
            return this;
        }

        @Override
        public <E extends Exception> Try<S> onFailure(CheckedConsumer<? super Exception, E> action) throws E {
            action.accept(throwable);
            return this;
        }

        @Override
        public <G extends Exception, E extends Exception> Try<S>
        onFailure(Class<G> clazz, CheckedConsumer<? super G, E> action) throws E {
            if(clazz.isInstance(throwable)) {
                action.accept(clazz.cast(throwable));
            }
            return this;
        }

        @Override
        public Try<S> filter(Predicate<? super S> pred) {
            return this;
        }

        @Override
        public S orElse(S value) {
            return value;
        }

        @Override
        public S orElseGet(Supplier<? extends S> value) {
            return value.get();
        }

        @Override
        public S orElseThrow() throws RuntimeException {
            throw Optional.of(throwable)
                    .filter(RuntimeException.class::isInstance)
                    .map(RuntimeException.class::cast)
                    .orElseGet(() -> new RuntimeException(throwable));
        }

        @Override
        public S orElseThrowChecked() throws Exception {
            throw throwable;
        }

        @Override
        public <E extends Exception> S orElseThrow(E throwable) throws E {
            throw throwable;
        }

        @Override
        public <E extends Exception> Try<S> orElseTry(CheckedSupplier<? extends S, E> f) {
            try {
                return success(f.get());
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public <X extends Exception> S orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

        @Override
        public <U, E extends Exception> Try<U> map(CheckedFunction<? super S, ? extends U, E> f) {
            return failure(throwable);
        }

        @Override
        public Optional<? extends Exception> contraOptional() {
            return Optional.of(throwable);
        }

        @Override
        public <G extends Exception> Optional<? extends G> contraOptional(Class<G> clazz) {
            return Optional.of(throwable).filter(clazz::isInstance).map(clazz::cast);
        }

        @Override
        public <S1> Optional<S1> contraMap(Function<? super Exception, ? extends S1> func) {
            return Optional.of(throwable).map(func);
        }

        @Override
        public <S1> Optional<S1> flatContraMap(Function<? super Exception, Optional<S1>> func) {
            return Optional.of(throwable).flatMap(func);
        }

        @Override
        public <U, E extends Exception> Try<U> flatMap(CheckedFunction<? super S, ? extends Try<U>, E> f) {
            return failure(throwable);
        }

        @Override
        public <E extends Exception> Try<S> recover(CheckedFunction<? super Exception, ? extends S, E> f) {
            try {
                return success(f.apply(throwable));
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public <E extends Exception> Try<S> flatRecover(CheckedFunction<? super Exception,? extends Try<S>, E> f) {
            try {
                return f.apply(throwable);
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public <G extends Exception, E extends Exception> Try<S> recover(Class<G> clazz,
                CheckedFunction<? super Exception, ? extends S, E> f) {
            if(clazz.isInstance(throwable)) {
                return recover(f);
            } else {
                return this;
            }
        }

        @Override
        public <G extends Exception, E extends Exception> Try<S> flatRecover(Class<G> clazz,
                CheckedFunction<? super Exception, ? extends Try<S>, E> f) {
            if(clazz.isInstance(throwable)) {
                return flatRecover(f);
            } else {
                return this;
            }
        }

        @Override
        public Optional<S> optional() {
            return Optional.empty();
        }

        @Override
        public Stream<S> stream() {
            return Stream.empty();
        }
    }


}
