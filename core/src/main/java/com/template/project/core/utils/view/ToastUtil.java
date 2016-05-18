package com.template.project.core.utils.view;

import android.content.Context;
import android.widget.Toast;

/**
 * Utility class for displaying toast message.
 */
public class ToastUtil {

    public static void showToast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    public static void showShortToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
