package com.template.project.core.utils.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Utility class for conversion of dip to pixel and checking device screen size.
 */
public class ViewUtil {

    public static final String SCR_SIZE_SMALL = "small", SCR_SIZE_NORMAL = "normal",
            SCR_SIZE_LARGE = "large", SCR_SIZE_XLARGE = "xlarge";

    /**
     * Convert a DIP value to pixel.
     *
     * @param context
     * @param dip     dip value
     * @return pixel value which is calculated depending on your current device
     *         configuration
     */
    public static int dipToPixel(Context context, float dip) {
        return (int) (dip * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int dipToPixel(Resources res, float dp) {
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                res.getDisplayMetrics()
        );
        return px;
    }

    /**
     * Get the device size name.
     * @return String value of device size name(small, normal, large, xlarge).
     */
    public static String getSizeName(Context context) {
        int screenLayout = context.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return SCR_SIZE_SMALL;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return SCR_SIZE_NORMAL;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return SCR_SIZE_LARGE;
            case 4: // Configuration.SCREENLAYOUT_SIZE_XLARGE is API >= 9
                return SCR_SIZE_XLARGE;
            default:
                return "undefined";
        }
    }

    /**
     * Check if device screen size is small.
     * @param context The activity context or application context.
     * @return true if screen size is small, false if not.
     */
    public static boolean isScrSizeSmall(Context context) {
        return getSizeName(context).equals("small");
    }

    /**
     * Check if device screen screen size large.
     * @param context The activity context or application context.
     * @return true if screen size is large, false if not.
     */
    public static boolean isScrSizeLarge(Context context) {
        return getSizeName(context).equals("large");
    }

    /**
     * Check the orientation of device.
     * @return Values either Configuration.ORIENTATION_LANDSCAPE or Configuration.ORIENTATION_PORTRAIT
     */
    public static int getScreenOrientation(Context context) {
        int ot = context.getResources().getConfiguration().orientation;
        if(ot == Configuration.ORIENTATION_LANDSCAPE) {
            return Configuration.ORIENTATION_LANDSCAPE;
        }
        return Configuration.ORIENTATION_PORTRAIT;
    }

}
