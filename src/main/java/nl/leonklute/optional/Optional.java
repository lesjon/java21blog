package nl.leonklute.optional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public sealed interface Optional<T> /* implicit permits Optional.Empty, Optional.Present*/ {

    static <T> Optional<T> present(T value) {
        return new Present<>(value);
    }
    static <T> Empty<T> empty() {
        return Empty.getInstance();
    }
    static <T> Optional<T> ofNullable(T value) {
        return value == null ? empty() : new Present<>(value);
    }

    static <T> Optional<T> of(T value) {
        Objects.requireNonNull(value);
        return present(value);
    }

    default Optional<T> filter(java.util.function.Predicate<? super T> predicate) {
        return switch (this) {
            case Empty<T> ignored -> this;
            case Present(var some) when predicate.test(some) -> this;
            case Present(var ignored) -> empty();
        };
    }

    /*
     * Throws NoSuchElementException if this is None
     */
    default T get() {
        return switch (this) {
            case Present(var some) -> some;
            case Empty<T> ignored -> throw new NoSuchElementException("Cannot get value from None");
        };
    }

    default boolean isPresent() {
        return this instanceof Optional.Present;
    }

    default boolean isEmpty() {
        return this instanceof Optional.Empty;
    }


    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Present(var some) -> some;
            case Empty<T> ignored -> defaultValue;
        };
    }

    default void ifPresent(Consumer<? super T> consumer) {
        switch (this) {
            case Present(var some) -> consumer.accept(some);
            case Empty<T> ignored -> {}
        }
    }

    default void ifPresentOrElse(Consumer<? super T> consumer, Runnable runnable) {
        switch (this) {
            case Present(var some) -> consumer.accept(some);
            case Empty<T> ignored -> runnable.run();
        }
    }


    default <U> Optional<U> map(Function<T, U> mapper) {
        Objects.requireNonNull(mapper);
        return switch (this) {
            case Present(var some) -> ofNullable(mapper.apply(some));
            case Empty<T> ignored -> empty();
        };
    }

    default <U> Optional<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        return switch (this) {
            case Present(var some) -> {
                @SuppressWarnings("unchecked")
                Optional<U> r = (Optional<U>) mapper.apply(some);
                yield Objects.requireNonNull(r);
            }
            case Empty<T> ignored -> empty();
        };
    }

    default Optional<T> or(Supplier<? extends Optional<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        return switch (this) {
            case Present(var ignored) -> this;
            case Empty<T> ignored -> {
                @SuppressWarnings("unchecked")
                Optional<T> r = (Optional<T>) supplier.get();
                yield Objects.requireNonNull(r);
            }
        };

    }

    default Stream<T> stream() {
        return switch (this) {
            case Present(var some) -> Stream.of(some);
            case Empty<T> ignored -> Stream.empty();
        };
    }

    default T orElse(T other) {
        return switch (this) {
            case Present(var some) -> some;
            case Empty<T> ignored -> other;
        };
    }

    default T orElseGet(Supplier<? extends T> supplier) {
        return switch (this) {
            case Present(var some) -> some;
            case Empty<T> ignored -> supplier.get();
        };
    }

    default T orElseThrow() {
        return switch (this) {
            case Present(var some) -> some;
            case Empty<T> ignored -> throw new NoSuchElementException("No value present");
        };
    }


    final class Empty<T> implements Optional<T> {
        private final static Empty<?> INSTANCE = new Empty<>();
        
        private Empty() {}

        public static <T> Empty<T> getInstance() {
            @SuppressWarnings("unchecked")
            Empty<T> instance = (Empty<T>) INSTANCE;
            return instance;
        }
    }
    record Present<T>(T value) implements Optional<T> {
        public Present {
            Objects.requireNonNull(value, "Value cannot be null");
        }
    }
}