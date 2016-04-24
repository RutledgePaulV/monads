package com.github.rutledgepaulv.monads.supporting;

public interface CheckedRunnable<E extends Exception> {
    void run() throws E;
}
