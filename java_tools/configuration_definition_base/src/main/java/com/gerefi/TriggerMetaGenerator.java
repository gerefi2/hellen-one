package com.gerefi;

import java.io.IOException;

public class TriggerMetaGenerator {
    public static void main(String[] a) throws IOException {
        ReaderStateImpl reader = new ReaderStateImpl();
        reader.setDefinitionInputFile("integration/gerefi_config_trigger.txt");
        reader.addCHeaderDestination("../unit_tests/test-framework/trigger_meta_generated.h");
        reader.addJavaDestination("../java_tools/trigger-image/src/main/java/com/gerefi/config/generated/TriggerMeta.java");

        reader.doJob();
    }
}
