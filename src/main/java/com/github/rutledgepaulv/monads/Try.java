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
 * @param <T> The type of the value contained by the monad.
 */
@SuppressWarnings("unused")
public abstract class Try<T> implements ToOptional<T>, ToStream<T> {
    private Try() {
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
            return success(supplier.get());
        } catch (Exception e) {
            return failure(e);
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

    /**
     * Flattens a 2-try into a 1-try.
     *
     * @param nested The nested try
     * @param <S> The type contained by the innermost try.
     * @return The flattened try.
     */
    public static <S> Try<S> flatten2(Try<Try<S>> nested) {
        return flatten(nested, identity());
    }

    /**
     * Flattens a 3-try into a 1-try.
     *
     * @param nested The nested try
     * @param <S> The type contained by the innermost try.
     * @return The flattened try.
     */
    public static <S> Try<S> flatten3(Try<Try<Try<S>>> nested) {
        return flatten(nested, Try::flatten2);
    }

    /**
     * Flattens a 4-try into a 1-try.
     *
     * @param nested The nested try
     * @param <S> The type contained by the innermost try.
     * @return The flattened try.
     */
    public static <S> Try<S> flatten4(Try<Try<Try<Try<S>>>> nested) {
        return flatten(nested, Try::flatten3);
    }

    /**
     * Flattens a 5-try into a 1-try.
     *
     * @param nested The nested try
     * @param <S> The type contained by the innermost try.
     * @return The flattened try.
     */
    public static <S> Try<S> flatten5(Try<Try<Try<Try<Try<S>>>>> nested) {
        return flatten(nested, Try::flatten4);
    }

    /**
     * Flattens a 6-try into a 1-try.
     *
     * @param nested The nested try
     * @param <S> The type contained by the innermost try.
     * @return The flattened try.
     */
    public static <S> Try<S> flatten6(Try<Try<Try<Try<Try<Try<S>>>>>> nested) {
        return flatten(nested, Try::flatten5);
    }

    /**
     * Flattens a 7-try into a 1-try.
     *
     * @param nested The nested try
     * @param <S> The type contained by the innermost try.
     * @return The flattened try.
     */
    public static <S> Try<S> flatten7(Try<Try<Try<Try<Try<Try<Try<S>>>>>>> nested) {
        return flatten(nested, Try::flatten6);
    }

    /**
     * Flattens a 8-try into a 1-try.
     *
     * @param nested The nested try
     * @param <S> The type contained by the innermost try.
     * @return The flattened try.
     */
    public static <S> Try<S> flatten8(Try<Try<Try<Try<Try<Try<Try<Try<S>>>>>>>> nested) {
        return flatten(nested, Try::flatten7);
    }

    /**
     * Lifts a bifunction into a bifunction returning a try.
     *
     * @param func The function to lift.
     * @param <IN1> The first input type.
     * @param <IN2> The second input type.
     * @param <OUT> The output type.
     * @return The lifted function.
     */
    public static <IN1, IN2, OUT> BiFunction<IN1, IN2, Try<OUT>> lift(CheckedBiFunction<IN1,IN2,OUT,?> func) {
        return (in1, in2) -> of(() -> func.apply(in1, in2));
    }

    /**
     * Lifts a function into a function returning a try.
     *
     * @param func The function to lift.
     * @param <IN> The input type.
     * @param <OUT> The output type.
     * @return The lifted function.
     */
    public static <IN, OUT> Function<IN, Try<OUT>> lift(CheckedFunction<IN, OUT, ?> func) {
        return in -> of(() -> func.apply(in));
    }

    /**
     * Lifts a supplier into a supplier returning a try.
     *
     * @param func The function to lift.
     * @param <OUT> The output type.
     * @return The lifted supplier.
     */
    public static <OUT> Supplier<Try<OUT>> lift(CheckedSupplier<OUT, ?> func) {
        return () -> of(func);
    }

    /**
     * Lifts a biconsumer into a bifunction returning a void try.
     *
     * @param consumer The consumer to lift.
     * @param <IN1> The first input type.
     * @param <IN2> The second input type.
     * @return The lifted consumer.
     */
    public static <IN1, IN2> BiFunction<IN1, IN2, Try<Void>> liftVoid(CheckedBiConsumer<IN1, IN2,?> consumer) {
        return (in1, in2) -> of(() -> consumer.accept(in1,in2));
    }

    /**
     * Lifts a consumer into a function returning a void try.
     *
     * @param consumer The consumer to lift.
     * @param <IN> The input type.
     * @return The lifted consumer.
     */
    public static <IN> Function<IN, Try<Void>> liftVoid(CheckedConsumer<IN, ?> consumer) {
        return (in) -> of(() -> consumer.accept(in));
    }

    /**
     * Lifts a runnable into a supplier returning a void try.
     *
     * @param runnable The runnable to lift.
     * @return The lifted runnable.
     */
    public static Supplier<Try<Void>> liftVoid(CheckedRunnable<?> runnable) {
        return () -> of(runnable);
    }

    /**
     * Get the value contained by the try, or throws the
     * exception that was encountered during execution (wrapped
     * as a runtime exception if it was not already).
     *
     *
     * @return The successful value.
     * @throws RuntimeException The exception encountered
     *                          during execution if any.
     */
    public abstract T get() throws RuntimeException;

    /**
     * Get the value contained by the try, or throws the
     * exception that was encountered during execution.
     *
     * @return The successful value.
     * @throws Exception The exception encountered
     *                          during execution if any.
     */
    public abstract T getChecked() throws Exception;

    /**
     * Whether the execution was successful or not.
     * @return True if success, otherwise false.
     */
    public abstract boolean isSuccess();

    /**
     * Whether the execution was failed or not.
     * @return True if failed, otherwise false.
     */
    public abstract boolean isFailure();

    /**
     * Executes a consumer of the success value if it
     * was indeed successful.
     *
     * @param action The consumer to execute.
     * @param <E> The type of exception thrown by the consumer, if any.
     * @return The same try instance.
     * @throws E The exception thrown by the consumer, if any.
     */
    public abstract <E extends Exception> Try<T>
    onSuccess(CheckedConsumer<? super T, E> action) throws E;

    /**
     * Executes a consumer of the exception if it
     * was indeed failed.
     *
     * @param action The consumer to execute.
     * @param <E> The type of exception thrown by the consumer, if any.
     * @return The same try instance.
     * @throws E The exception thrown by the consumer, if any.
     */
    public abstract <E extends Exception> Try<T>
    onFailure(CheckedConsumer<? super Exception, E> action) throws E;

    /**
     * Executes a consumer of the exception if it
     * was indeed failed and if the exception is an
     * instance of the provided type.
     *
     * @param clazz The type of exception to match on.
     * @param action The consumer to execute.
     * @param <E> The type of exception thrown by the consumer, if any.
     * @return The same try instance.
     * @throws E The exception thrown by the consumer, if any.
     */
    public abstract <G extends Exception, E extends Exception> Try<T>
    onFailure(Class<G> clazz, CheckedConsumer<? super G, E> action) throws E;

    /**
     * Gets the failed / exception case as an optional.
     *
     * @return An optional containing the exception produced
     *         during a failure, if any.
     */
    public abstract Optional<? extends Exception>
    contraOptional();

    /**
     * Gets the failed / exception case as an optional.
     *
     * @return An optional containing the exception produced
     *         during a failure, if any.
     */
    public abstract <G extends Exception> Optional<? extends G>
    contraOptional(Class<G> clazz);

    /**
     * Filters against the success result (if any) to
     * potentially narrow it to a failed result if the
     * predicate does not match.
     *
     * @param pred The predicate to use. If it returns false then the
     *             returned try will actually be a failure instance.
     *
     * @return The failure try if already failed, the success try if
     *         the predicate matches, or a failed try if it was a success
     *         case but the predicate did not match.
     */
    public abstract Try<T>
    filter(Predicate<? super T> pred);

    /**
     * Returns the value contained in the try if it was successful,
     * otherwise returns the passed in value.
     *
     * @param value The value to return if the try was unsuccessful.
     * @return The value contained within the try or else the provided value.
     */
    public abstract T
    orElse(T value);

    /**
     * Returns the value contained in the try if it was successful,
     * otherwise returns the first element generated from the supplier.
     *
     * @param supplier The generating function to return a value from
     *                 if the try was failed.
     * @return The value contained within the try or else the generated value.
     */
    public abstract T
    orElseGet(Supplier<? extends T> supplier);

    /**
     * An alias for {@link #get()}.
     *
     * @return The value contained within the try.
     * @throws RuntimeException Throws a runtime exception if the try was failed.
     */
    public abstract T
    orElseThrow() throws RuntimeException;

    /**
     * An alias for {@link #getChecked()}.
     *
     * @return The value contained within the try.
     * @throws RuntimeException Throws the exception if the try was failed.
     */
    public abstract T
    orElseThrowChecked() throws Exception;

    /**
     * Return the success value, or else throw the the first exception
     * generated from the provided supplier instead of whatever
     * exception is contained in the failed Try.
     *
     * @param exceptionSupplier The generating function to use to
     *                          get an exception to throw in case
     *                          of a failed Try.
     *
     * @param <E> The type of the exception to throw
     * @return The success value contained within the Try.
     * @throws E The provided exception if the Try was failed.
     */
    public abstract <E extends Exception> T
    orElseThrow(Supplier<E> exceptionSupplier) throws E;

    /**
     * If the original Try was failed, optionally provide another
     * supplier to try instead (but with the same return type).
     *
     * @param func The supplier to attempt to get a new value with
     *             in the case of a failed try.
     *
     * @param <E> The type of exception that might be thrown by the
     *            new supplying function, if any.
     *
     * @return The same Try instance if it was already successful,
     *         else a new Try instance wrapping the result of executing
     *         the provided supplier.
     */
    public abstract <E extends Exception> Try<T>
    orElseTry(CheckedSupplier<? extends T, E> func);

    /**
     * Maps against the success result of a Try if it
     * was successful. Otherwise the original failure
     * is maintained.  If the map function itself throws
     * an exception when applied to the success result it
     * will produce a failed Try with that exception.
     *
     * @param func The function to map against
     * @param <U> The type of the resulting value after the map
     * @param <E> The type of exception thrown by the map function, if any.
     *
     * @return The try containing the result of the map or the original exception.
     */
    public abstract <U, E extends Exception> Try<U>
    map(CheckedFunction<? super T, ? extends U, E> func);

    /**
     * Flat maps against the success result of a try if it
     * was successful. Otherwise the original failure is maintained.
     * If the function provided to flatMap throws an exception when
     * executed against the success result then it will produce a
     * failed try with that exception.
     *
     * @param func The function to flatMap against.
     * @param <U> The type of the Try value after the func is applied.
     * @param <E> The type of exception thrown by the map function, if any.
     *
     * @return The result of the provided function if it was a successful try
     *         and the function threw no errors. Otherwise a failed try containing
     *         either the original exception from a previous failure, or if it had
     *         been successful and the func failed, then containing the exception
     *         produced by the func.
     */
    public abstract <U, E extends Exception> Try<U>
    flatMap(CheckedFunction<? super T, ? extends Try<U>, E> func);

    /**
     * Contra-map is a variant of map that executes against the
     * failed state instead of the success state of a Try. Since
     * a try does not contain a first class value for the exception
     * case, this map does not modify the Try instance. Rather it maps
     * against a possibly existing exception and produces an optional.
     *
     * @param func The function to map with.
     * @param <S> The type produced by the map function.
     * @return An optional maybe containing the mapped value.
     */
    public abstract <S> Optional<S>
    contraMap(Function<? super Exception, ? extends S> func);

    /**
     * Contra-flatMap is a variant of map that executes against the
     * failed state instead of the success state of a Try. Since
     * a try does not contain a first class value for the exception
     * case, this map does not modify the Try instance. Rather it maps
     * against a possibly existing exception and produces an optional.
     *
     * @param func The function to map with.
     * @param <S> The type produced by the map function.
     * @return An optional maybe containing the mapped value.
     */
    public abstract <S> Optional<S>
    flatContraMap(Function<? super Exception, Optional<S>> func);

    /**
     * Provides a mechanism to convert a failed Try back into a
     * successful try by handling an exception and producing a
     * success value that matches the type of the original Try.
     * If the recovery function throws an exception then that exception will
     * become the exception contained within the Try. Note that this can be
     * confusing if you're chaining many recover calls knowing that some of
     * them may fail. For this reason it's best to use recovery functions that
     * do not have a possibility of failing. If the try was already successful
     * then the provided function will never be executed.
     *
     * @param func The function to use to recover with.
     * @param <E> The type of the exception thrown by the recovery function, if any.
     * @return The (hopefully) recovered Try. If the func through an exception it may
     *         still be a failed Try containing that exception.
     */
    public abstract <E extends Exception> Try<T>
    recover(CheckedFunction<? super Exception,? extends T, E> func);

    /**
     * Provides a mechanism to convert a failed Try back into a
     * successful try by handling an exception and producing a
     * Try. If the recovery function throws an exception then that exception will
     * become the exception contained within the Try. Note that this can be
     * confusing if you're chaining many recover calls knowing that some of
     * them may fail. For this reason it's best to use recovery functions that
     * do not have a possibility of failing. If the try was already successful
     * then the provided function will never be executed.
     *
     * @param func The function to use to recover with.
     * @param <E> The type of the exception thrown by the recovery function, if any.
     * @return The result Try. If the func through an exception it may
     *         still be a failed Try containing that exception.
     */
    public abstract <E extends Exception> Try<T>
    flatRecover(CheckedFunction<? super Exception, ? extends Try<T>, E> func);

    /**
     * Provides a mechanism to convert a failed Try back into a
     * successful try by handling an exception and producing a
     * success value that matches the type of the original Try.
     * If the recovery function throws an exception then that exception will
     * become the exception contained within the Try. Note that this can be
     * confusing if you're chaining many recover calls knowing that some of
     * them may fail. For this reason it's best to use recovery functions that
     * do not have a possibility of failing. If the try was already successful
     * then the provided function will never be executed. The provided class
     * is used to filter the recovery operation against the exception type contained
     * by the failed try (if it's failed). In this way you can recover differently
     * for different kinds of exceptions (or only recover for certain expected exceptions).
     *
     * @param clazz The exception class to filter the recovery operation to.
     * @param func The function to use to recover with.
     * @param <E> The type of the exception thrown by the recovery function, if any.
     * @return The (hopefully) recovered Try. If the func through an exception it may
     *         still be a failed Try containing that exception.
     */
    public abstract <G extends Exception, E extends Exception> Try<T>
    recover(Class<G> clazz, CheckedFunction<? super Exception,? extends T, E> func);

    /**
     * Provides a mechanism to convert a failed Try back into a
     * successful try by handling an exception and producing a
     * Try. If the recovery function throws an exception then that exception will
     * become the exception contained within the Try. Note that this can be
     * confusing if you're chaining many recover calls knowing that some of
     * them may fail. For this reason it's best to use recovery functions that
     * do not have a possibility of failing. If the try was already successful
     * then the provided function will never be executed. The provided class
     * is used to filter the recovery operation against the exception type contained
     * by the failed try (if it's failed). In this way you can recover differently
     * for different kinds of exceptions (or only recover for certain expected exceptions).
     *
     * @param clazz The exception class to filter the recovery operation to.
     * @param func The function to use to recover with.
     * @param <E> The type of the exception thrown by the recovery function, if any.
     * @return The result Try. If the func through an exception it may
     *         still be a failed Try containing that exception.
     */
    public abstract <G extends Exception, E extends Exception> Try<T>
    flatRecover(Class<G> clazz, CheckedFunction<? super Exception, ? extends Try<T>, E> func);



    private static class Success<S> extends Try<S> {

        private final S value;

        private Success(S value) {
            this.value = value;
        }

        @Override
        public final S get() throws RuntimeException {
            return value;
        }

        @Override
        public final S getChecked() throws Exception {
            return value;
        }

        @Override
        public final boolean isSuccess() {
            return true;
        }

        @Override
        public final boolean isFailure() {
            return false;
        }

        @Override
        public final <E extends Exception> Try<S> onSuccess(CheckedConsumer<? super S, E> action) throws E {
            action.accept(value);
            return this;
        }

        @Override
        public final <E extends Exception> Try<S> onFailure(CheckedConsumer<? super Exception, E> action) throws E {
            return this;
        }

        @Override
        public final <G extends Exception, E extends Exception> Try<S>
        onFailure(Class<G> clazz, CheckedConsumer<? super G, E> action) throws E {
            return this;
        }

        @Override
        public final Try<S> filter(Predicate<? super S> pred) {
            if (pred.test(value)) {
                return this;
            } else {
                return failure("Predicate filter resulted in the successful result being dropped.");
            }
        }

        @Override
        public final S orElse(S value) {
            return get();
        }

        @Override
        public final S orElseGet(Supplier<? extends S> value) {
            return get();
        }

        @Override
        public final S orElseThrow() throws RuntimeException {
            return get();
        }

        @Override
        public final S orElseThrowChecked() throws Exception {
            return getChecked();
        }

        @Override
        public final <E extends Exception> Try<S> orElseTry(CheckedSupplier<? extends S, E> func) {
            return this;
        }

        @Override
        public final <X extends Exception> S orElseThrow(Supplier<X> exceptionSupplier) throws X {
            return get();
        }

        @Override
        public final <U, E extends Exception> Try<U> map(CheckedFunction<? super S, ? extends U, E> func) {
            try {
                return success(func.apply(this.value));
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public final Optional<? extends Exception> contraOptional() {
            return Optional.empty();
        }

        @Override
        public final <G extends Exception> Optional<? extends G> contraOptional(Class<G> clazz) {
            return Optional.empty();
        }

        @Override
        public final <S1> Optional<S1> contraMap(Function<? super Exception, ? extends S1> func) {
            return Optional.empty();
        }

        @Override
        public final <S1> Optional<S1> flatContraMap(Function<? super Exception,  Optional<S1>> func) {
            return Optional.empty();
        }

        @Override
        public final <U, E extends Exception> Try<U> flatMap(CheckedFunction<? super S, ? extends Try<U>, E> func) {
            try {
                return func.apply(this.value);
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public final <E extends Exception> Try<S> recover(CheckedFunction<? super Exception, ? extends S, E> func) {
            return this;
        }

        @Override
        public final <E extends Exception> Try<S> flatRecover(CheckedFunction<? super Exception, ? extends Try<S>, E> func) {
            return this;
        }

        @Override
        public final <G extends Exception, E extends Exception> Try<S> recover(Class<G> clazz,
                CheckedFunction<? super Exception, ? extends S, E> func) {
            return this;
        }

        @Override
        public final <G extends Exception, E extends Exception> Try<S> flatRecover(Class<G> clazz,
                CheckedFunction<? super Exception, ? extends Try<S>, E> func) {
            return this;
        }

        @Override
        public final Optional<S> optional() {
            return Optional.of(value);
        }

        @Override
        public final Stream<S> stream() {
            return Stream.of(value);
        }

        @Override
        public final String toString() {
            return String.format("Try.Success[%s]", Objects.toString(value));
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Success)) {
                return false;
            }
            Success<?> success = (Success<?>) o;
            return Objects.equals(value, success.value);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(value);
        }
    }


    private static class Failure<S> extends Try<S> {

        private final Exception throwable;

        private Failure(Exception throwable) {
            this.throwable = throwable;
        }

        @Override
        public final S get() throws RuntimeException {
            throw Optional.of(throwable)
                    .filter(RuntimeException.class::isInstance)
                    .map(RuntimeException.class::cast)
                    .orElseGet(() -> new RuntimeException(throwable));
        }

        @Override
        public final S getChecked() throws Exception {
            throw throwable;
        }

        @Override
        public final boolean isSuccess() {
            return false;
        }

        @Override
        public final boolean isFailure() {
            return true;
        }

        @Override
        public final <E extends Exception> Try<S> onSuccess(CheckedConsumer<? super S, E> action) throws E {
            return this;
        }

        @Override
        public final <E extends Exception> Try<S> onFailure(CheckedConsumer<? super Exception, E> action) throws E {
            action.accept(throwable);
            return this;
        }

        @Override
        public final <G extends Exception, E extends Exception> Try<S>
        onFailure(Class<G> clazz, CheckedConsumer<? super G, E> action) throws E {
            if(clazz.isInstance(throwable)) {
                action.accept(clazz.cast(throwable));
            }
            return this;
        }

        @Override
        public final Try<S> filter(Predicate<? super S> pred) {
            return this;
        }

        @Override
        public final S orElse(S value) {
            return value;
        }

        @Override
        public final S orElseGet(Supplier<? extends S> value) {
            return value.get();
        }

        @Override
        public final S orElseThrow() throws RuntimeException {
            throw Optional.of(throwable)
                    .filter(RuntimeException.class::isInstance)
                    .map(RuntimeException.class::cast)
                    .orElseGet(() -> new RuntimeException(throwable));
        }

        @Override
        public final S orElseThrowChecked() throws Exception {
            throw throwable;
        }

        @Override
        public final <E extends Exception> Try<S> orElseTry(CheckedSupplier<? extends S, E> func) {
            try {
                return success(func.get());
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public final <X extends Exception> S orElseThrow(Supplier<X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

        @Override
        public final <U, E extends Exception> Try<U> map(CheckedFunction<? super S, ? extends U, E> func) {
            return failure(throwable);
        }

        @Override
        public final Optional<? extends Exception> contraOptional() {
            return Optional.of(throwable);
        }

        @Override
        public final <G extends Exception> Optional<? extends G> contraOptional(Class<G> clazz) {
            return Optional.of(throwable).filter(clazz::isInstance).map(clazz::cast);
        }

        @Override
        public final <S1> Optional<S1> contraMap(Function<? super Exception, ? extends S1> func) {
            return Optional.of(throwable).map(func);
        }

        @Override
        public final <S1> Optional<S1> flatContraMap(Function<? super Exception, Optional<S1>> func) {
            return Optional.of(throwable).flatMap(func);
        }

        @Override
        public final <U, E extends Exception> Try<U> flatMap(CheckedFunction<? super S, ? extends Try<U>, E> func) {
            return failure(throwable);
        }

        @Override
        public final <E extends Exception> Try<S> recover(CheckedFunction<? super Exception, ? extends S, E> func) {
            try {
                return success(func.apply(throwable));
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public final <E extends Exception> Try<S> flatRecover(CheckedFunction<? super Exception,? extends Try<S>, E> func) {
            try {
                return func.apply(throwable);
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public final <G extends Exception, E extends Exception> Try<S> recover(Class<G> clazz,
                CheckedFunction<? super Exception, ? extends S, E> func) {
            if(clazz.isInstance(throwable)) {
                return recover(func);
            } else {
                return this;
            }
        }

        @Override
        public final <G extends Exception, E extends Exception> Try<S> flatRecover(Class<G> clazz,
                CheckedFunction<? super Exception, ? extends Try<S>, E> func) {
            if(clazz.isInstance(throwable)) {
                return flatRecover(func);
            } else {
                return this;
            }
        }

        @Override
        public final Optional<S> optional() {
            return Optional.empty();
        }

        @Override
        public final Stream<S> stream() {
            return Stream.empty();
        }

        @Override
        public final String toString() {
            return String.format("Try.Failure[%s]", Objects.toString(throwable));
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Failure)) {
                return false;
            }
            Failure<?> failure = (Failure<?>) o;
            return Objects.equals(throwable, failure.throwable);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(throwable);
        }

    }



    private static <S, T extends Try<?>> Try<S> flatten(Try<T> nested, Function<? super T, Try<S>> func) {
        return nested.optional().map(func).orElseGet(() -> (Failure<S>)nested);
    }



}
