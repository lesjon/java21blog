package nl.leonklute.optional.result;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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