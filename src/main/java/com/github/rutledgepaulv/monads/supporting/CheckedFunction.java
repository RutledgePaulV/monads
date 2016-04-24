package com.github.rutledgepaulv.monads.supporting;

public interface CheckedFunction<T,S,E extends Exception> {

    S apply(T value) throws E;

}
