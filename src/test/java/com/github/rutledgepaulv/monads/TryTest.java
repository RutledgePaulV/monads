package com.github.rutledgepaulv.monads;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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





}