package com.github.rutledgepaulv.monads.supporting;

public interface CheckedBiConsumer<T,S,E extends Exception> {

    void accept(T value1, S value2) throws E;

}
