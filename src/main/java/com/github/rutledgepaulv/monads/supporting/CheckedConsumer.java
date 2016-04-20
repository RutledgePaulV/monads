package com.github.rutledgepaulv.monads.supporting;

public interface CheckedConsumer<T, E extends Throwable> {

    void accept(T value) throws E;

}
