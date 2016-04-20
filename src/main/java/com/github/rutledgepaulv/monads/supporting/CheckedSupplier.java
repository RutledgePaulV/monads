package com.github.rutledgepaulv.monads.supporting;

public interface CheckedSupplier<T, E extends Throwable> {

    T get() throws E;

}
