package com.github.rutledgepaulv.monads.supporting;

public interface CheckedFunction<T,S,E extends Throwable> {

    S apply(T value) throws E;

}
