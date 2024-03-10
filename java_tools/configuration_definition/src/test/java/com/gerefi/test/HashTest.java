package com.gerefi.test;

import com.gerefi.output.HashUtil;
import org.junit.jupiter.api.Test;

import static com.gerefi.AssertCompatibility.assertEquals;

public class HashTest {
    @Test
    public void testdjb2() {
        assertEquals(HashUtil.djb2lowerCase("Hello1"), 30950378);
        assertEquals(HashUtil.djb2lowerCase("Hello2"), 30950379);
        assertEquals(HashUtil.djb2lowerCase("HELLO2"), 30950379);
    }

}
