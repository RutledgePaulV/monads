package com.github.rutledgepaulv.monads.supporting;

import java.util.stream.Stream;

public interface ToStream<T> {

    Stream<T> stream();

}
