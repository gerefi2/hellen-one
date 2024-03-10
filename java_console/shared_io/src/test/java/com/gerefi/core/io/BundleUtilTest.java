package com.gerefi.core.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BundleUtilTest {
    @Test
    public void testExtractBundleTarget() {
        Assertions.assertEquals("proteus_f7", BundleUtil.getBundleTarget("gerefi.snapshot.proteus_f7"));
    }
}
