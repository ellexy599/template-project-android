package com.template.project.core.utils.file;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.template.project.core.utils.LogMe;

public class OsUtil {

    private static final String TAG = OsUtil.class.getSimpleName();

    /**
     * Get the OS
     * @return String value "Android"
     */
    public static String getOs() {
        return "Android";
    }

    /**
     * Get the OS API level
     * @return Api level in integer value which are in Build.VERSION_CODES
     */
    public static int getOsApiLevel() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * Get the OS version of the phone.
     * @return The os version of the phone or the API level. Ex. "2.2", "2.3", "4.4", etc.
     */
    public static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Get the application version. This is also what can be seen in AndroidManifest.xml file or
     * the Gradle configuration.
     * The default is "1.0.0"
     * @param ctx The application context
     * @return The application version. Ex. "1.0.0.0"
     */
    public static String getAppVersion(Context ctx) {
        String appVersion = "1.0.0.0";
        try {
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogMe.e(TAG, e.toString());
        }
        return appVersion;
    }

    /**
     * Get the manufacturer and model name of the phone
     */
    public static String getDeviceModel() {
        // Use MANUFACTURER and PRODUCT because MODEL can return
        // different value like a IME number or any String
        String deviceModel = Build.MANUFACTURER + " " + Build.PRODUCT;
        return deviceModel;
    }

}
