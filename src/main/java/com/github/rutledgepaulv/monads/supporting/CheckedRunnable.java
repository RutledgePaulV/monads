package com.github.rutledgepaulv.monads.supporting;

public interface CheckedRunnable<E extends Throwable> {
    void run() throws E;
}
