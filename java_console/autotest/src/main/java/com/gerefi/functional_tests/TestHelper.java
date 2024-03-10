package com.gerefi.functional_tests;

import com.gerefi.config.generated.Fields;
import com.gerefi.core.MessagesCentral;

import static com.gerefi.TestingUtils.assertNull;

public enum TestHelper {
    INSTANCE;

    private String criticalError;

    TestHelper() {
        MessagesCentral.getInstance().addListener((clazz, message) -> {
            if (message.startsWith(Fields.CRITICAL_PREFIX))
                criticalError = message;
        });
    }

    public void assertNotFatal() {
        assertNull("Fatal not expected: " + criticalError, criticalError);
    }
}
