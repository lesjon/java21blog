# Building better Optional in Java 21
## What Optional should have been  
Since “new” java, starting with java 8, the java standard has been expanded to support more modern programming paradigms.
Optional was an obvious improvement over the existing null check / NullPointerException state.
However, after 9 years since Java 8 with the introduction of Optional, it is still not as widely used as in other languages.

## Why is Optional not used more?
Using Optional is not as easy as it should be. The syntax is verbose and the API is not as intuitive as it could be. This can be solved by record pattern matching:
```java
class testOptional{
    @Test
    void testNewOptional() {
        // Old style is still supported:
        Optional.of("ifPresent")
                .ifPresent(System.out::println);
        Optional.empty()
                .ifPresent((v) -> fail(format("Should not enter branch; value is '%s'", v)));
        Optional<String> ifPresentOrElse = present("ifPresentOrElse");
        if (ifPresentOrElse.isPresent()) {
            System.out.println(ifPresentOrElse.get());
        } else {
            fail("Should not enter branch");
        }
        // New style with pattern matching
        switch (Optional.of("Hello")) {
            case Present(var value) -> assertEquals("Hello", value);
            case Empty() -> fail("Should not enter branch");
        }
        if (Optional.of("test") instanceof Present(var value)) {
            assertEquals("test", value);
        } else {
            fail("Should not enter branch");
        }
    }
}
```
This pattern matching has been enabled by creating a sealed interface with two implementations:
```java
public sealed interface Optional<T> /* implicit permits Optional.Empty, Optional.Present*/ {
    static <T> Optional<T> empty() {
        return new Empty<>();
    }
    static <T> Optional<T> present(T value) {
        return new Present<>(value);
    }
    record Empty<T>() implements Optional<T> {
    }
    record Present<T>(T value) implements Optional<T> {
    }

    /*
     * Throws NoSuchElementException if this is None
     */
    default T get() {
        return switch (this) {
            case Present(var some) -> some;
            case Empty() -> throw new NoSuchElementException("Cannot get value from None");
        };
    }
    // part of the api implemented
    /**
     * Unwrap the value, if present, otherwise return other.
     */
    default T orElse(T other) {
        return switch (this) {
            case Present(var some) -> some;
            case Empty() -> other;
        };
    }
    /**
     * Filters the optional value, if the predicate returns false.
     */
    default Optional<T> filter(java.util.function.Predicate<? super T> predicate) {
        return switch (this) {
            case Empty() -> this;
            case Present(var some) when predicate.test(some) -> this;
            case Present(var ignored) -> empty();
        };
    }
}
```
All methods are declared in this interface, either as `static` or `default` methods,

## Why is this better?
The new syntax is more concise and easier to read.
## Implementing Result type
The Result type is a type that can hold either a value or an error. This is useful when you want to return a value, but also want to be able to return an error.
```java
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
```
## Future improvements
In the future we will be able to use value classes to make Optional even better, now the Optional might still cause a double indirection to access the actual data.
## Why not use enums like Rust?
```java
enum Optional<T> {
    Some(T),
    None()
}
```
In java enums are initialized at class load time, which means that the enum cannot be generic. And cannot hold any state, other than state set at compile time.
```java
enum OptionalEnum <T>{
    Some("Value set at compile time"), None();
    T value;
}
```
