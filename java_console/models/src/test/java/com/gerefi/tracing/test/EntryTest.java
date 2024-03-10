package com.gerefi.tracing.test;

import com.gerefi.tracing.Entry;
import com.gerefi.tracing.Phase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntryTest {
    @Test
    public void testToString() {
        Entry e = new Entry("hello", Phase.E, 0.1, 0, 0);

        assertEquals("{\"name\":\"hello\",\"ph\":\"E\",\"tid\":0,\"pid\":0,\"ts\":0.1}", e.toString());

    }
}
