package com.github.rutledgepaulv.monads;

import com.github.rutledgepaulv.monads.supporting.*;

import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

public abstract class Try<T> implements SupportsOptional<T>, SupportsStream<T> {
    private Try() {
    }

    public static <S, E extends Throwable> Try<S> attempt(CheckedSupplier<S, E> supplier) {
        try {
            return Try.ok(supplier.get());
        } catch (Throwable e) {
            return Try.fail(e);
        }
    }


    public static <E extends Throwable> Try<Void> attempt(CheckedRunnable<E> runnable) {
        return attempt(() -> {
            runnable.run();
            return null;
        });
    }


    private static <S> Try<S> fail(String message) {
        return new Failure<>(new RuntimeException(message));
    }

    private static <S> Try<S> fail(Throwable e) {
        return new Failure<>(e);
    }

    private static <S> Try<S> ok(S value) {
        return new Success<>(value);
    }


    public abstract T get() throws RuntimeException;

    public abstract T getChecked() throws Throwable;

    public abstract boolean isSuccess();

    public abstract boolean isFailure();

    public abstract <E extends Throwable> Try<T> onSuccess(CheckedConsumer<T, E> action) throws E;

    public abstract <E extends Throwable> Try<T> onFailure(CheckedConsumer<Throwable, E> action) throws E;

    public abstract Try<T> filter(Predicate<T> pred);

    public abstract T orElse(T value);

    public abstract T orElseGet(Supplier<T> value);

    public abstract <E extends Throwable> Try<T> orElseTry(CheckedSupplier<T, E> f);

    public abstract <E extends Throwable> T orElseThrow(Supplier<? extends E> exceptionSupplier) throws E;

    public abstract <U, E extends Throwable> Try<U> map(CheckedFunction<? super T, ? extends U, E> f);

    public abstract <U, E extends Throwable> Try<U> flatMap(CheckedFunction<? super T, Try<U>, E> f);

    public abstract Try<T> recover(Function<? super Throwable, T> f);

    public abstract <E extends Throwable> Try<T> flatRecover(CheckedFunction<? super Throwable, Try<T>, E> f);


    private static class Success<S> extends Try<S> {

        private final S value;

        private Success(S value) {
            this.value = value;
        }

        @Override
        public S get() throws RuntimeException {
            return value;
        }

        @Override
        public S getChecked() throws Throwable {
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
        public <E extends Throwable> Try<S> onSuccess(CheckedConsumer<S, E> action) throws E {
            action.accept(value);
            return this;
        }

        @Override
        public <E extends Throwable> Try<S> onFailure(CheckedConsumer<Throwable, E> action) throws E {
            return this;
        }

        @Override
        public Try<S> filter(Predicate<S> pred) {
            if (pred.test(value)) {
                return this;
            } else {
                return fail("Predicate filter resulted in the successful result being dropped.");
            }
        }

        @Override
        public S orElse(S value) {
            return value;
        }

        @Override
        public S orElseGet(Supplier<S> value) {
            return this.value;
        }

        @Override
        public <E extends Throwable> Try<S> orElseTry(CheckedSupplier<S, E> f) {
            return this;
        }

        @Override
        public <X extends Throwable> S orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return this.value;
        }

        @Override
        public <U, E extends Throwable> Try<U> map(CheckedFunction<? super S, ? extends U, E> f) {
            try {
                return ok(f.apply(this.value));
            } catch (Throwable e) {
                return fail(e);
            }
        }

        @Override
        public <U, E extends Throwable> Try<U> flatMap(CheckedFunction<? super S, Try<U>, E> f) {
            try {
                return f.apply(this.value);
            } catch (Throwable e) {
                return fail(e);
            }
        }

        @Override
        public Try<S> recover(Function<? super Throwable, S> f) {
            return this;
        }

        @Override
        public <E extends Throwable> Try<S> flatRecover(CheckedFunction<? super Throwable, Try<S>, E> f) {
            return this;
        }

        @Override
        public Optional<S> optional() {
            return Optional.ofNullable(value);
        }

        @Override
        public Stream<S> stream() {
            return Stream.of(value);
        }
    }


    private static class Failure<S> extends Try<S> {

        private final Throwable throwable;

        private Failure(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public S get() throws RuntimeException {
            throw new RuntimeException(throwable);
        }

        @Override
        public S getChecked() throws Throwable {
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
        public <E extends Throwable> Try<S> onSuccess(CheckedConsumer<S, E> action) throws E {
            return this;
        }

        @Override
        public <E extends Throwable> Try<S> onFailure(CheckedConsumer<Throwable, E> action) throws E {
            action.accept(throwable);
            return this;
        }

        @Override
        public Try<S> filter(Predicate<S> pred) {
            return this;
        }

        @Override
        public S orElse(S value) {
            return value;
        }

        @Override
        public S orElseGet(Supplier<S> value) {
            return value.get();
        }

        @Override
        public <E extends Throwable> Try<S> orElseTry(CheckedSupplier<S, E> f) {
            try {
                return ok(f.get());
            } catch (Throwable e) {
                return fail(e);
            }
        }

        @Override
        public <X extends Throwable> S orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

        @Override
        public <U, E extends Throwable> Try<U> map(CheckedFunction<? super S, ? extends U, E> f) {
            return fail(throwable);
        }

        @Override
        public <U, E extends Throwable> Try<U> flatMap(CheckedFunction<? super S, Try<U>, E> f) {
            return fail(throwable);
        }

        @Override
        public Try<S> recover(Function<? super Throwable, S> f) {
            try {
                return ok(f.apply(throwable));
            } catch (Throwable e) {
                return fail(e);
            }
        }

        @Override
        public <E extends Throwable> Try<S> flatRecover(CheckedFunction<? super Throwable, Try<S>, E> f) {
            try {
                return f.apply(throwable);
            } catch (Throwable e) {
                return fail(e);
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
