# Building better Optional in Java 21
## What Optional should have been  
Since "new" java, starting with java 8, the java standard has been expanded to support more modern programming paradigms.
Optional was an obvious improvement over the existing null check / NullPointerException state.
However, after 9 years since Java 8 with the introduction of Optional, it is still not as widely used as in other languages.

## Why is Optional not used more?
Using Optional is not as easy as it should be. The syntax is verbose and the API is not as intuitive as it could be. 
```java
class testOptional{
    @Test
    void testNewOptional() {
        // Old style is still supported:
        Optional.of("ifPresent")
                .ifPresent(System.out::println);
        Optional.empty()
                .ifPresent((v) -> fail(format("Should not enter branch; value is '%s'", v)));
        Optional<String> stringOpt = present("ifPresentOrElse");
        if (stringOpt.isPresent()) {
            System.out.println(stringOpt.get());
        } else {
            fail("Should not enter branch");
        }
    }
}
```
This can be solved by record pattern matching:
```java
class testOptional{
    @Test
    void testNewOptional() {
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
All methods are declared in this interface, either as `static` or `default` methods.
To get more control over the access modifiers it might be better to use an abstract class rather than an interface. However, records cant extend any classes, so this is not possible.

For whole implementation see [Optional.java](https://github.com/lesjon/java21blog/blob/main/src/main/java/nl/leonklute/optional/Optional.java)

## Why is this better?
The new syntax is more concise when unwrapping the value.
## Implementing Result type
This Syntax could also be used to make other parts of java more functional. For example, the Result type.
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
This can be used to return a value or an error:
```java

class ResultTest {
    @Test
    void testResult() {
        Callable<String> callable = () -> "Hello";
        switch (Result.from(callable)) {
            case Result.Ok(var value) -> assertEquals("Hello", value);
            case Result.Error(var throwable) -> fail("Should not enter branch", throwable);
        }
    }

    @Test
    void testThrowable() {
        Callable<String> throwingCallable = () -> {throw new RuntimeException("this should not actually fail");};
        switch (Result.from(throwingCallable)) {
            case Result.Ok(var value) -> fail(format("Should not enter branch; value is '%s'", value));
            case Result.Error(var throwable) ->
                    assertEquals(throwable.getMessage(), "this should not actually fail");
        }
    }

}
```
## Future improvements
In the future we will be able to use value classes to make Optional even better, now the Optional might still cause a double indirection to access the actual data.
With value classes from project valhalla, all data of an object can be stored on the stack instead of the heap. This is perfect for Optional, since it is a wrapper around a value.
## Conclusion
While this is a nice possibility, I don't think Java should actually change the current implementation of Optional. This could break some backwards compatibility and would not be worth it.
