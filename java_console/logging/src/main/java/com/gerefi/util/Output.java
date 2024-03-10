package com.gerefi.util;

import java.io.IOException;

public interface Output extends AutoCloseable {
    void write(String line);

    void close() throws IOException;
}
