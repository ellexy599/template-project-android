package com.template.project.core.request.api;

import android.content.Context;
import android.util.Pair;

import com.template.project.core.utils.OsUtils;

public abstract class ApiHttpUtil {

    /**
     * Add X-TelefunMtv-Token to Content Header of the Request.
     */
    public static Pair getHeaderSession(String sessionToken) {
        Pair pair = Pair.create("Token", sessionToken);
        return pair;
    }

    /**
     * Add X-TelefunMtv-DeviceOS to Content Header of the Request,
     * by default value is "Android"
     */
    public static Pair getHeaderDeviceOs() {
        Pair pair = Pair.create("DeviceOS", OsUtils.getOs());
        return pair;
    }

    /**
     * Add X-TelefunMtv-DeviceVersion to Content Header of the Request,
     * this is the app version(versionName in AndroidManifest file)
     */
    public static Pair getHeaderDeviceVersion(Context context) {
        Pair pair = Pair.create("DeviceVersion", OsUtils.getAppVersion(context));
        return pair;
    }

}
