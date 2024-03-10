package com.gerefi.test;

import com.gerefi.VariableRegistry;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static com.gerefi.VariableRegistry.*;
import static com.gerefi.AssertCompatibility.assertEquals;

/**
 * 3/30/2015
 */
public class VariableRegistryTest {
    @Test
    public void testReplace() {
        VariableRegistry registry = new VariableRegistry();
        registry.register("var", 256);
        assertEquals(3, registry.size());

        // trivial key-value substitution
        assertEquals("256", registry.applyVariables("@@var@@"));
        assertEquals("ab256", registry.applyVariables("ab@@var@@"));
        assertEquals("ab256cd", registry.applyVariables("ab@@var@@cd"));
        // both decimal and hex values here
        assertEquals("aa256qwe100fff", registry.applyVariables("aa@@var@@qwe@@var" + _HEX_SUFFIX + "@@fff"));

        assertEquals("\\x01\\x00", registry.applyVariables("@@var" + _16_HEX_SUFFIX + "@@"));
    }

    @Test
    public void testCharHexUsage() {
        VariableRegistry registry = new VariableRegistry();
        registry.register("SD_r", "'r'");

        assertEquals(4, registry.size());
        assertEquals("ab72", registry.applyVariables("ab@@SD_r" + CHAR_SUFFIX + _HEX_SUFFIX +
                "@@"));
        assertEquals("ab'r'", registry.applyVariables("ab@@SD_r@@"));
        assertEquals("abr", registry.applyVariables("ab@@SD_r" + CHAR_SUFFIX + "@@"));
    }

    @Test
    public void testPrepend() {
        VariableRegistry registry = new VariableRegistry();
        String stringKey = "key_string";
        registry.register(stringKey, "value_1");
        assertEquals("value_1", registry.get(stringKey));

        registry.register(stringKey, "value_2");
        assertEquals("value_1", registry.get(stringKey));

        String intKey = "key_int";
        registry.register(intKey, 1);
        assertEquals("1", registry.get(intKey));
        assertEquals("1", registry.get(intKey + _HEX_SUFFIX));

        registry.register(intKey, 2);
        assertEquals("1", registry.get(intKey));
        assertEquals("1", registry.get(intKey + _HEX_SUFFIX));
    }

    @Test
    public void testHumanSorted() {
        Map<Integer, String> input = new HashMap<>();
        input.put(0, "NONE");
        input.put(1, "A");
        input.put(2, "Z");
        input.put(3, "N");
        assertEquals("0=\"NONE\",1=\"A\",3=\"N\",2=\"Z\"", VariableRegistry.getHumanSortedTsKeyValueString(input));
    }

    @Test
    public void testDefineAndQuotes() throws IOException {
        VariableRegistry registry = new VariableRegistry();
        registry.readPrependValues(new StringReader("#define SINGLE 'L'\n" +
                "#define DOUBLE \"R\""));
        assertEquals("hello L R 'L' \"R\"", registry.applyVariables("hello @#SINGLE#@ @#DOUBLE#@ @@SINGLE@@ @@DOUBLE@@"));
    }
}
