package com.template.project.core.utils;

import android.util.Log;

import com.template.project.core.AppConfiguration;

/**
 * Custom logger to be used for controlling overall 
 * enabling and disabling application log.
 */
public class LogMe {

    private static final String APP_TAG = "TemplateProject";

    public static void d(String TAG, String message) {
        if (AppConfiguration.ENABLE_LOG) {
            Log.d(APP_TAG + TAG, message);
        }
    }

    public static void e(String TAG, String message) {
        if (AppConfiguration.ENABLE_LOG) {
            Log.e(APP_TAG + TAG, message);
        }
    }

    public static void w(String TAG, String message) {
        if (AppConfiguration.ENABLE_LOG) {
            Log.w(APP_TAG + TAG, message);
        }
    }

    public static void v(String TAG, String message) {
        if (AppConfiguration.ENABLE_LOG) {
            Log.v(APP_TAG + TAG, message);
        }
    }

    public static void i(String TAG, String message) {
        if (AppConfiguration.ENABLE_LOG) {
            Log.i(APP_TAG + TAG, message);
        }
    }
}
