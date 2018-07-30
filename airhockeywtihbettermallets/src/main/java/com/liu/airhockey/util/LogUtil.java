package com.liu.airhockey.util;

import android.util.Log;

public class LogUtil {
    public static final boolean ON = true;

    public static void w(String TAG, String message) {
        if (ON) {
            Log.w(TAG, message);
        }
    }

    public static void v(String TAG, String message) {
        if (ON) {
            Log.v(TAG, message);
        }
    }
}
