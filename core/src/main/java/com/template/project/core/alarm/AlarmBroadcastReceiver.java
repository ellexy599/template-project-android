package com.template.project.core.alarm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.template.project.core.utils.file.OsUtil;

/**
 * Broadcast receiver of set alarm.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    public static String EXTRAS_IS_SHOW_NOTIFICATION = "is_show_notification";
    public static String EXTRAS_DRAWABLE_NOTIF_ICON = "notif_icon";
    public static String EXTRAS_NOTIFICATION_ID = "alarm_id";
    public static String EXTRAS_NOTIFICATION_MESSAGE = "alarm_message";
    public static String EXTRAS_NOTIFICATION_TITLE = "alarm_title";
    public static String EXTRAS_NOTIFICATION_CONTENT = "alarm_content";

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        boolean isShowNotif = bundle.getBoolean(EXTRAS_IS_SHOW_NOTIFICATION);
        int id = bundle.getInt(EXTRAS_NOTIFICATION_ID);
        int iconId = bundle.getInt(EXTRAS_DRAWABLE_NOTIF_ICON);
        String message = bundle.getString(EXTRAS_NOTIFICATION_MESSAGE);
        String title = bundle.getString(EXTRAS_NOTIFICATION_TITLE);
        String content = bundle.getString(EXTRAS_NOTIFICATION_CONTENT);

        if(isShowNotif) {
            NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            // Creating PendingIntent with normal Intent
            /*Intent resIntent = new Intent(context, HomeActivity.class);
            resIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
            resIntent.putExtra("fromNotification", true);
            resIntent.putExtra("message", message);
            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    (int) System.currentTimeMillis(), resIntent, 0);*/

            // Creating PendingIntent using TaskStackBuilder
            /*Intent resultIntent = new Intent(this, HomeActivity.class);
            resIntent.putExtra("fromNotification", true);
            resIntent.putExtra("message", message);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack
            stackBuilder.addParentStack(HomeActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);*/

            // Create Intent with notification launching no activity when clicked
            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    (int) System.currentTimeMillis(), new Intent(), 0);

            Notification notif;
            if(OsUtil.getOsApiLevel() <= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                notif =new Notification();
                notif.icon = iconId;
                notif.tickerText= message;
                notif.when = System.currentTimeMillis();
                notif.setLatestEventInfo(context, title, content, contentIntent);
                notif.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
            } else {
                notif = new Notification.Builder(context)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(iconId)
                    .setContentIntent(contentIntent)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .build();
            }
            nm.notify(id, notif);
        }
    }

}
