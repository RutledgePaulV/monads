package com.github.rutledgepaulv.monads;

import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class LazyTest {


    @Test
    public void testFlattening() {
        Lazy<String> try1 = Lazy.of(() -> "badgers");

        Lazy<Lazy<String>> try2 = Lazy.of(() -> try1);
        assertEquals("badgers", Lazy.flatten2(try2).get());

        Lazy<Lazy<Lazy<String>>> try3 = Lazy.of(() -> try2);
        assertEquals("badgers", Lazy.flatten3(try3).get());

        Lazy<Lazy<Lazy<Lazy<String>>>> try4 = Lazy.of(() -> try3);
        assertEquals("badgers", Lazy.flatten4(try4).get());

        Lazy<Lazy<Lazy<Lazy<Lazy<String>>>>> try5 = Lazy.of(() -> try4);
        assertEquals("badgers", Lazy.flatten5(try5).get());

        Lazy<Lazy<Lazy<Lazy<Lazy<Lazy<String>>>>>> try6 = Lazy.of(() -> try5);
        assertEquals("badgers", Lazy.flatten6(try6).get());

        Lazy<Lazy<Lazy<Lazy<Lazy<Lazy<Lazy<String>>>>>>> try7 = Lazy.of(() -> try6);
        assertEquals("badgers", Lazy.flatten7(try7).get());

        Lazy<Lazy<Lazy<Lazy<Lazy<Lazy<Lazy<Lazy<String>>>>>>>> try8 = Lazy.of(() -> try7);
        assertEquals("badgers", Lazy.flatten8(try8).get());
    }


    @Test
    public void empty() throws Exception {
        Lazy<String> lazy = Lazy.empty();
        assertNull(lazy.get());
    }

    @Test
    public void of_Value() throws Exception {
        Lazy<String> lazy = Lazy.of("testing");
        assertEquals("testing", lazy.get());
    }

    @Test
    public void of_Supplier() throws Exception {
        Lazy<String> lazy = Lazy.of(() -> "testing");
        assertEquals("testing", lazy.get());
    }

    @Test
    public void map() throws Exception {
        Lazy<String> lazy = Lazy.of(() -> "testing");
        assertEquals("i am testing", lazy.map("i am "::concat).get());

        Lazy<String> empty = Lazy.empty();
        assertNull(empty.map("i am "::concat).get());
    }

    @Test
    public void flatMap() throws Exception {
        Lazy<String> lazy = Lazy.of(() -> "testing");
        assertEquals("i am testing", lazy.flatMap(term -> Lazy.of("i am ".concat(term))).get());

        Lazy<String> empty = Lazy.empty();
        assertNull(empty.flatMap(term -> Lazy.of("i am ".concat(term))).get());
    }

    @Test
    public void stream() throws Exception {
        assertEquals(0, Lazy.empty().stream().count());

        assertTrue(Lazy.of("testing").stream().findAny().isPresent());
        assertEquals("testing", Lazy.of("testing").stream().findAny().get());

        assertTrue(Lazy.of(() -> "testing").stream().findAny().isPresent());
        assertEquals("testing", Lazy.of(() -> "testing").stream().findAny().get());
    }

    @Test
    public void optional() throws Exception {
        assertFalse(Lazy.empty().optional().isPresent());
        assertTrue(Lazy.of("test").optional().isPresent());
        assertTrue(Lazy.of(() -> "test").optional().isPresent());
        assertEquals("test", Lazy.of("test").optional().get());
        assertEquals("test", Lazy.of(() -> "test").optional().get());
    }


    @Test
    public void allOperationsThatCanMaintainLazinessDoMaintainLaziness() {
        final AtomicInteger counter = new AtomicInteger(0);
        Supplier<String> countingSupplier = () -> Objects.toString(counter.incrementAndGet());

        Lazy<String> lazy = Lazy.of(countingSupplier);
        assertEquals(0, counter.get());

        lazy.stream().map(String::toUpperCase);
        assertEquals(0, counter.get());

        lazy = lazy.map(String::toUpperCase);
        assertEquals(0, counter.get());

        lazy = lazy.flatMap(Lazy::of);
        assertEquals(0, counter.get());

        String result = lazy.get();
        assertEquals("1", result);
        assertEquals(1, counter.get());

        result = lazy.get();
        assertEquals("1", result);
        assertEquals(1, counter.get());

        result = lazy.get();
        assertEquals("1", result);
        assertEquals(1, counter.get());

        counter.set(0);
        Lazy<String> forFlattening = Lazy.of(countingSupplier);
        Lazy<Lazy<String>> nested = Lazy.of(() -> forFlattening);
        Lazy<String> flattened = Lazy.flatten2(nested);

        assertEquals(0, counter.get());

        result = flattened.get();
        assertEquals("1", result);
        assertEquals(1, counter.get());

        result = flattened.get();
        assertEquals("1", result);
        assertEquals(1, counter.get());

        result = flattened.get();
        assertEquals("1", result);
        assertEquals(1, counter.get());
    }

}