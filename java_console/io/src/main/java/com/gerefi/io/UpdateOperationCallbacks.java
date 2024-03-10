package com.gerefi.io;

public interface UpdateOperationCallbacks {
    void log(String message);

    default void append(String message) {
      log(message);
    }

    void done();
    void error();

    class UpdateOperationDummy implements UpdateOperationCallbacks {
        @Override
        public void log(String message) {
        }

        @Override
        public void done() {
        }

        @Override
        public void error() {
        }
    }

    public static UpdateOperationCallbacks DUMMY = new UpdateOperationDummy();
}
