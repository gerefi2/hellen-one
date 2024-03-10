package com.gerefi.app.util;

import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

public class AndroidUtil {
    private AndroidUtil() {
    }

    public static void turnScreenOn(Activity gerefi) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            gerefi.setTurnScreenOn(true);
        } else {
            Window window = gerefi.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }
}
