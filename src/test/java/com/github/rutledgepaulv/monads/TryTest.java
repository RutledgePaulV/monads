package com.github.rutledgepaulv.monads;

import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.function.*;

import static org.junit.Assert.*;

public class TryTest {

    @Test
    public void testFlattening() {
        Try<String> try1 = Try.of(() -> "badgers");

        Try<Try<String>> try2 = Try.of(() -> try1);
        assertEquals("badgers", Try.flatten2(try2).get());

        Try<Try<Try<String>>> try3 = Try.of(() -> try2);
        assertEquals("badgers", Try.flatten3(try3).get());

        Try<Try<Try<Try<String>>>> try4 = Try.of(() -> try3);
        assertEquals("badgers", Try.flatten4(try4).get());

        Try<Try<Try<Try<Try<String>>>>> try5 = Try.of(() -> try4);
        assertEquals("badgers", Try.flatten5(try5).get());

        Try<Try<Try<Try<Try<Try<String>>>>>> try6 = Try.of(() -> try5);
        assertEquals("badgers", Try.flatten6(try6).get());

        Try<Try<Try<Try<Try<Try<Try<String>>>>>>> try7 = Try.of(() -> try6);
        assertEquals("badgers", Try.flatten7(try7).get());

        Try<Try<Try<Try<Try<Try<Try<Try<String>>>>>>>> try8 = Try.of(() -> try7);
        assertEquals("badgers", Try.flatten8(try8).get());
    }

    @Test
    public void of() throws Exception {
        Try<String> it = Try.of(() -> "testing");
        assertEquals("testing", it.get());
    }

    @Test
    public void of1() throws Exception {
        Try<Void> it = Try.of(() -> System.out.println("testing"));
        assertTrue(it.isSuccess());
    }

    @Test
    public void failure() throws Exception {
        Try<String> it = Try.failure(new RuntimeException("bingo"));
        assertFalse(it.isSuccess());
        assertTrue(it.isFailure());
    }

    @Test
    public void failure1() throws Exception {
        Try<String> it = Try.failure("bingo");
        assertFalse(it.isSuccess());
        assertTrue(it.isFailure());
    }

    @Test
    public void success() throws Exception {
        Try<String> it = Try.success("testing");
        assertTrue(it.isSuccess());
        assertFalse(it.isFailure());
    }

    @Test
    public void success1() throws Exception {
        Try<Void> it = Try.success();
        assertTrue(it.isSuccess());
        assertFalse(it.isFailure());
    }

    @Test
    public void lift() throws Exception {
        Supplier<String> supplier = () -> "testing";
        Supplier<Try<String>> lifted = Try.lift(supplier::get);
        Try<String> result = lifted.get();
        assertEquals("testing", result.get());
    }

    @Test
    public void lift1() throws Exception {
        Function<String, String> function = value -> value + "thing";
        Function<String, Try<String>> lifted = Try.lift(function::apply);
        Try<String> result = lifted.apply("testing ");
        assertEquals("testing thing", result.get());
    }

    @Test
    public void lift2() throws Exception {
        BiFunction<String, String, Integer> function = (val1, val2) -> val1.length() + val2.length();
        BiFunction<String,String,Try<Integer>> lifted = Try.lift(function::apply);
        Try<Integer> result = lifted.apply("thing", "thing2");
        assertEquals((Integer)"thingthing2".length(), result.get());
    }

    @Test
    public void liftVoid() throws Exception {
        Runnable runnable = () -> System.out.println("stuff");
        Supplier<Try<Void>> lifted = Try.liftVoid(runnable::run);
        Try<Void> result = lifted.get();
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
    }

    @Test
    public void liftVoid1() throws Exception {
        Consumer<String> consumer = System.out::println;
        Function<String, Try<Void>> lifted = Try.liftVoid(consumer::accept);
        Try<Void> result = lifted.apply("stuff");
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
    }

    @Test
    public void liftVoid2() throws Exception {
        BiConsumer<String, String> consumer = (val1, val2) -> System.out.println(val1 + val2);
        BiFunction<String, String, Try<Void>> lifted = Try.liftVoid(consumer::accept);
        Try<Void> result = lifted.apply("stuff", "more stuff");
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
    }

    @Test
    public void get() throws Exception {
        Try<String> success = Try.success("testing");
        assertEquals("testing", success.get());

        Try<String> runtime = Try.failure(new IllegalArgumentException("testing"));

        try {
            runtime.get();
            fail();
        }catch(RuntimeException e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }

        Try<String> checked = Try.failure(new IOException("Encountered IO exception"));
        try {
            checked.get();
            fail();
        }catch(RuntimeException e) {
            assertTrue(RuntimeException.class.isInstance(e));
            assertTrue(IOException.class.isInstance(e.getCause()));
        }

    }

    @Test
    public void getChecked() throws Exception {
        Try<String> success = Try.success("testing");
        assertEquals("testing", success.get());

        Try<String> runtime = Try.failure(new IllegalArgumentException("testing"));

        try {
            runtime.getChecked();
            fail();
        }catch(Exception e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }

        Try<String> checked = Try.failure(new IOException("Encountered IO exception"));
        try {
            checked.getChecked();
            fail();
        }catch(Exception e) {
            assertTrue(IOException.class.isInstance(e));
        }
    }

    @Test
    public void onSuccess() throws Exception {
        String[] values = new String[]{null};
        Try.of(() -> "stuff")
                .onSuccess(value -> values[0] = value)
                .onFailure(ex -> values[0] = null);

        assertEquals("stuff", values[0]);
    }

    @Test
    public void onFailure() throws Exception {
        Exception[] values = new Exception[]{null};
        Try<String> test = Try.of(() -> Integer.toString(5 / 0))
                .onSuccess(value -> values[0] = null)
                .onFailure(ex -> values[0] = ex);

        assertTrue(ArithmeticException.class.isInstance(values[0]));
    }

    @Test
    public void onFailure1() throws Exception {
        Exception[] values = new Exception[]{null, null};
        Try<String> test = Try.of(() -> Integer.toString(5 / 0))
                .onSuccess(value -> values[0] = null)
                .onFailure(NullPointerException.class, ex -> values[1] = new RuntimeException("testing"))
                .onFailure(ArithmeticException.class, ex -> values[0] = ex);

        assertNull(values[1]);
        assertTrue(ArithmeticException.class.isInstance(values[0]));
    }

    @Test
    public void contraOptional() throws Exception {
        Optional<? extends Exception> exception = Try.of(() -> {throw new IOException("Testing");}).contraOptional();
        assertTrue(exception.isPresent());
        assertTrue(IOException.class.isInstance(exception.get()));
    }

    @Test
    public void contraOptional1() throws Exception {
        Optional<? extends IOException> exception = Try.of(() ->
        {throw new IOException("Testing");}).contraOptional(IOException.class);

        assertTrue(exception.isPresent());
        assertTrue(IOException.class.isInstance(exception.get()));
    }

    @Test
    public void filter() throws Exception {
        Try<String> it = Try.of(() -> "testing");
        Try<String> filtered = it.filter(val -> val.length() > 100);
        assertTrue(filtered.isFailure());
        assertFalse(filtered.isSuccess());
        filtered.onFailure(ex ->
                assertEquals("Predicate filter resulted in the successful result being dropped.", ex.getMessage()));
    }

    @Test
    public void orElse() throws Exception {
        Try<String> it = Try.of(() -> { throw new RuntimeException("testing");});
        String value = it.orElse("badgers");
        assertEquals("badgers", value);
    }

    @Test
    public void orElseGet() throws Exception {
        Try<String> it = Try.of(() -> { throw new RuntimeException("testing");});
        String value = it.orElseGet(() -> "badgers");
        assertEquals("badgers", value);
    }

    @Test
    public void orElseThrow() throws Exception {
        Try<String> success = Try.success("testing");
        assertEquals("testing", success.get());

        Try<String> runtime = Try.failure(new IllegalArgumentException("testing"));

        try {
            runtime.orElseThrow();
            fail();
        }catch(RuntimeException e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }

        Try<String> checked = Try.failure(new IOException("Encountered IO exception"));
        try {
            checked.orElseThrow();
            fail();
        }catch(RuntimeException e) {
            assertTrue(RuntimeException.class.isInstance(e));
            assertTrue(IOException.class.isInstance(e.getCause()));
        }


        Try<String> test = Try.of(() -> "test");
        assertEquals("test", test.orElseThrow());
    }

    @Test
    public void orElseThrowChecked() throws Exception {
        Try<String> success = Try.success("testing");
        assertEquals("testing", success.get());

        Try<String> runtime = Try.failure(new IllegalArgumentException("testing"));

        try {
            runtime.orElseThrowChecked();
            fail();
        }catch(Exception e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
        }

        Try<String> checked = Try.failure(new IOException("Encountered IO exception"));
        try {
            checked.orElseThrowChecked();
            fail();
        }catch(Exception e) {
            assertTrue(IOException.class.isInstance(e));
        }


        Try<String> test = Try.of(() -> "test");
        assertEquals("test", test.orElseThrowChecked());
    }

    @Test
    public void orElseThrow2() throws Exception {
        Try<String> success = Try.success("testing");
        assertEquals("testing", success.get());

        Try<String> runtime = Try.failure(new IllegalArgumentException("testing"));

        try {
            runtime.orElseThrow(() -> new IllegalArgumentException("badgers"));
            fail();
        }catch(RuntimeException e) {
            assertTrue(IllegalArgumentException.class.isInstance(e));
            assertEquals("badgers", e.getMessage());
        }

        Try<String> checked = Try.failure(new IOException("Encountered IO exception"));
        try {
            checked.orElseThrow(() -> new IOException("testing"));
            fail();
        }catch(IOException e) {
            assertTrue(IOException.class.isInstance(e));
            assertEquals("testing", e.getMessage());
        }


        Try<String> test = Try.of(() -> "test");
        assertEquals("test", test.orElseThrow(() -> new RuntimeException("cats cats cats")));
    }

    @Test
    public void orElseTry() throws Exception {
        Try<String> val = Try.of(() -> {throw new IOException("cattts");});
        Try<String> result = val.orElseTry(() -> "testing");
        assertEquals("testing", result.get());
    }

    @Test
    public void map() throws Exception {
        Try<String> stuff = Try.of(() -> "demonstration");
        assertEquals("demonstration stuff", stuff.map(value -> value + " stuff").get());
    }

    @Test
    public void flatMap() throws Exception {
        Try<String> stuff = Try.of(() -> "demonstration");
        assertEquals("demonstration stuff", stuff.flatMap(value -> Try.success(value + " stuff")).get());
    }

    @Test
    public void contraMap() throws Exception {
        Try<String> it = Try.of(() -> {throw new IOException("boom");});
        Optional<RuntimeException> result = it.contraMap(RuntimeException::new);
        assertTrue(result.isPresent());
        assertTrue(RuntimeException.class.isInstance(result.get()));
    }

    @Test
    public void flatContraMap() throws Exception {
        Try<String> it = Try.of(() -> {throw new IOException("boom");});
        Optional<RuntimeException> result = it.flatContraMap(ex -> Optional.of(new RuntimeException(ex)));
        assertTrue(result.isPresent());
        assertTrue(RuntimeException.class.isInstance(result.get()));
    }

    @Test
    public void recover() throws Exception {
        Try<String> it = Try.of(() -> {throw new IOException("boom");});
        it = it.recover(Throwable::getMessage);
        assertEquals("boom", it.get());
    }

    @Test
    public void flatRecover() throws Exception {
        Try<String> it = Try.of(() -> {throw new IOException("boom");});
        it = it.flatRecover(ex -> Try.success(ex.getMessage()));
        assertEquals("boom", it.get());
    }

    @Test
    public void recover1() throws Exception {
        Try<String> it = Try.of(() -> {throw new IOException("boom");});
        it = it.recover(IOException.class, Throwable::getMessage);
        assertEquals("boom", it.get());
    }

    @Test
    public void flatRecover1() throws Exception {
        Try<String> it = Try.of(() -> {throw new IOException("boom");});
        it = it.flatRecover(IOException.class, ex -> Try.success(ex.getMessage()));
        assertEquals("boom", it.get());
    }

}