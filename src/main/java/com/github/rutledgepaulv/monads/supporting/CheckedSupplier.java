package com.github.rutledgepaulv.monads.supporting;

public interface CheckedSupplier<T, E extends Exception> {

    T get() throws E;

}
