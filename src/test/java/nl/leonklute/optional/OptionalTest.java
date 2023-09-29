package nl.leonklute.optional;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static java.lang.String.format;
import static nl.leonklute.optional.Optional.*;
import static org.junit.jupiter.api.Assertions.*;


class OptionalTest {

    @Test
    void testOptional() {
        Optional<String> optional = Optional.of("Hello");
        Optional<String> emptyOptional = empty();
        assertDoesNotThrow(optional::get);
        assertThrows(NoSuchElementException.class, emptyOptional::get);
        switch (optional) {
            case Present(var some) -> assertEquals("Hello", some);
            case Empty() -> fail("Should not enter branch");
        }
    }

    @Test
    void testJavaOptional() {
        java.util.Optional<String> optional = java.util.Optional.of("Hello");
        java.util.Optional<String> emptyOptional = java.util.Optional.empty();
        assertDoesNotThrow(optional::get);
        assertThrows(NoSuchElementException.class, emptyOptional::get);
//        switch (optional) {
//            case Present(var value) -> System.out.println(value);
//            case Empty() -> System.out.println("Empty");
//        };
    }

    @Test
    void testNewOptional() {
        // Old style
        present("ifPresent")
                .ifPresent(System.out::println);
        empty()
                .ifPresent((v) -> fail(format("Should not enter branch; value is '%s'", v)));
        Optional<String> ifPresentOrElse = present("ifPresentOrElse");
        if (ifPresentOrElse.isPresent()) {
            System.out.println(ifPresentOrElse.get());
        } else {
            fail("Should not enter branch");
        }
        // New style
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

    @Test
    void testNullOptional() {
        switch (Optional.ofNullable(null)) {
            case Present(var value) -> fail(format("Should not enter branch; value is '%s'", value));
            case Empty() -> {
            }
        }
    }

    @Test
    void testNoneEqualsNone() {
        Optional<String> optional = empty();
        Optional<Integer> emptyOptional = empty();
        assertEquals(optional, emptyOptional);
    }

    @Test
    void testSomeNotEqualsSome() {
        Optional<String> optional = Optional.of("Hola");
        Optional<String> emptyOptional = Optional.of("Hello");
        assertNotEquals(optional, emptyOptional);
    }

    @Test
    void testSomeEqualsSome() {
        Optional<String> optional = Optional.of("Hello");
        Optional<String> emptyOptional = Optional.of("Hello");
        assertEquals(optional, emptyOptional);
    }

    @Test
    void testFilter() {
        Optional<String> optional = Optional.ofNullable("Hello");
        Optional<String> emptyOptional = empty();
        assertEquals(optional.filter(String::isEmpty), emptyOptional);
    }

    @Test
    void testWhile() {
        Optional<String> optional = Optional.ofNullable("Hello");
        while (optional instanceof Present(var move)) {
            assertEquals("Hello", move);
            System.out.println(move);
            optional = optional.filter(String::isEmpty);
        }
    }

    @Test
    void testIsPresent() {
        Optional<String> optional = Optional.ofNullable("Hello");
        assertTrue(optional.isPresent());
        optional = optional.filter(String::isEmpty);
        assertFalse(optional.isPresent());
    }

    @Test
    void testMap() {
        Optional<String> optional = Optional.ofNullable("Hello");
        Optional<String> emptyOptional = empty();
        assertEquals(optional.map(String::length), Optional.of(5));
        assertEquals(emptyOptional.map(String::length), empty());
        assertEquals(emptyOptional.map(String::length), emptyOptional);
    }
}