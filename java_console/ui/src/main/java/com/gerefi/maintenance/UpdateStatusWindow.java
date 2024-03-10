package com.gerefi.maintenance;

import com.gerefi.io.UpdateOperationCallbacks;
import com.gerefi.ui.StatusWindow;

public class UpdateStatusWindow extends StatusWindow implements UpdateOperationCallbacks {
    public UpdateStatusWindow(String title) {
        showFrame(title);
    }

    @Override
    public void log(String message) {
        append(message);
    }

    @Override
    public void done() {
        setSuccessState();
    }

    @Override
    public void error() {
        setErrorState();
    }
}
