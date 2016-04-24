package com.github.rutledgepaulv.monads.supporting;

public interface CheckedBiFunction<T,S,V,E extends Exception> {

    V apply(T value1, S value2) throws E;

}
