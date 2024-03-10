package com.gerefi.test;

import com.opensr5.ConfigurationImage;
import com.gerefi.core.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.gerefi.ConfigurationImageDiff.findDifferences;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Andrey Belomutskiy, (c) 2013-2020
 * 3/6/2015
 */
public class ConfigurationImageDiffTest {
    @Test
    public void testCompare() {
        {
            byte[] data1 = {1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6};
            byte[] data2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 1, 2, 3, 4, 5, 6};
            Pair<Integer, Integer> p = findDifferences(new ConfigurationImage(data1), new ConfigurationImage(data2), 0);
            assertNotNull(p);
            assertEquals(5, (int) p.first);
            assertEquals(15, (int) p.second);
            p = findDifferences(new ConfigurationImage(data1), new ConfigurationImage(data2), 15);
            assertNull(p);
        }
        {
            byte[] data1 = {1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6};
            byte[] data2 = {1, 2, 3, 4, 5, 6, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6};
            Pair<Integer, Integer> p = findDifferences(new ConfigurationImage(data1), new ConfigurationImage(data2), 0);
            assertNotNull(p);
            assertEquals(5, (int) p.first);
            assertEquals(9, (int) p.second);
        }
        {
            byte[] data1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 1, 0, 0, 0, 0, 0};
            byte[] data2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 1, 2, 3, 4, 5, 6};
            Pair<Integer, Integer> p = findDifferences(new ConfigurationImage(data1), new ConfigurationImage(data2), 0);
            assertEquals(13, (int) p.first);
            assertEquals(18, (int) p.second);
        }
    }
}
