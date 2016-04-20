package com.github.rutledgepaulv.monads.supporting;

import java.util.stream.Stream;

public interface SupportsStream<T> {

    Stream<T> stream();

}
