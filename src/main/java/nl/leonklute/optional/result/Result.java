package nl.leonklute.optional.result;

import java.util.concurrent.Callable;

public sealed interface Result<T> /*implicit permits in same package*/ {
    static <R> Result<R> from(Callable<R> callable) {
        try {
            return new Ok<>(callable.call());
        } catch (Exception e) {
            return new Error<>(e);
        }
    }

    record Ok<R>(R value) implements Result<R> {
    }

    record Error<R, E extends Throwable>(E throwable) implements Result<R> {
    }

}

