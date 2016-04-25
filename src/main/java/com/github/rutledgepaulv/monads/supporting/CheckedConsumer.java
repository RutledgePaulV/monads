package com.github.rutledgepaulv.monads.supporting;

public interface CheckedConsumer<T, E extends Exception> {

    void accept(T value) throws E;

}
