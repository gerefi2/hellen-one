package com.gerefi.test;

import com.gerefi.ReaderStateImpl;
import com.gerefi.output.TSProjectConsumer;

public class TestTSProjectConsumer extends TSProjectConsumer {
    public TestTSProjectConsumer(String tsPath, ReaderStateImpl state) {
        super(tsPath, state);
    }

    @Override
    public void endFile() {
    }
}
