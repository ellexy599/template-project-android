package com.template.project.core.request.spice;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;

import com.octo.android.robospice.UncachedSpiceService;

public class SpiceHttpService extends UncachedSpiceService {

    @TargetApi(16)
    @Override
    public Notification createDefaultNotification() {
        final Notification noti = super.createDefaultNotification();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){
            noti.priority = Notification.PRIORITY_MIN;
        }
        return noti;
    }

}
