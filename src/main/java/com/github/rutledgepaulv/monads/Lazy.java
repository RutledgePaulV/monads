package com.github.rutledgepaulv.monads;

import com.github.rutledgepaulv.monads.supporting.SupportsOptional;
import com.github.rutledgepaulv.monads.supporting.SupportsStream;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A lazy evaluation monad that wraps a supplying function for one-time execution
 * and then caching of the generated value. Supports lazy mapping and flat mapping
 * over the contents.
 *
 * Supports conversion to an optional and a stream. An empty optional and empty stream
 * will be produced if the value is none. Getting as an optional immediately evaluates
 * the underlying supplier, however getting as a stream will not execute the supplier
 * until a terminal operation is performed on the stream. Either way, once the supplier
 * has been executed just once the value will be cached and the generating function
 * dereferenced.
 *
 * @param <T> The result type contained by the monad.
 */
public final class Lazy<T> implements Supplier<T>, SupportsOptional<T>, SupportsStream<T> {

    /**
     * @return Create a pre-evaluated lazy of null value.
     */
    public static <S> Lazy<S> empty() {
        Lazy<S> result = new Lazy<>(true);
        result.value = null;
        return result;
    }

    /**
     * @return Create a pre-evaluated lazy of the given value.
     */
    public static <S> Lazy<S> of(S value) {
        Lazy<S> result = new Lazy<>(true);
        result.value = value;
        return result;
    }

    /**
     * @return Create a lazy wrapping the generating function. The function
     *         will be executed at most once and then the value will cache as
     *         long as the lazy is referenced. The generating function is
     *         dereferenced after first execution.
     */
    public static <S> Lazy<S> of(Supplier<S> supplier) {
        Lazy<S> result = new Lazy<>(false);
        result.supplier = supplier;
        return result;
    }






    private T value;
    private Supplier<T> supplier;
    private final AtomicBoolean hasExecuted;

    private Lazy(boolean hasExecuted) {
        this.hasExecuted = new AtomicBoolean(hasExecuted);
    }


    /**
     * Map across the lazy by wrapping it as a new lazy who upon execution
     * will evaluate the original and apply the function to the result.
     *
     * @param mapper The mapping function.
     * @return The composed lazy
     */
    public final <S> Lazy<S> map(Function<? super T,? extends S> mapper) {
        return of(() -> mapper.apply(get()));
    }

    /**
     * Map across the lazy by wrapping it as a new lazy who upon execution
     * will evaluate the original and apply the function to the result and
     * then unwrap the function's lazy result.
     *
     * @param mapper The mapping function.
     * @return The composed lazy
     */
    public final <S> Lazy<S> flatMap(Function<? super T, Lazy<? extends S>> mapper) {
        return of(() -> mapper.apply(get()).get());
    }

    /**
     * @return Get the value contained herein.
     */
    @Override
    public final T get() {
        if(hasExecuted.compareAndSet(false, true)) {
            this.value = supplier.get();
            this.supplier = null;
        }
        return this.value;
    }

    /**
     * @return Get a stream that will lazily load the value
     *         contained herein.
     */
    @Override
    public final Stream<T> stream() {
        return Stream.of(this).map(Supplier::get);
    }

    /**
     * @return Get this lazy as an optional wrapping its value. If it
     *         was an empty lazy or the generating function evaluated
     *         to null, then the optional will be empty.
     */
    @Override
    public final Optional<T> optional() {
        return Optional.ofNullable(get());
    }

}
