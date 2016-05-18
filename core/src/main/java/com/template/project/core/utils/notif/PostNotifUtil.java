package com.template.project.core.utils.notif;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.template.project.core.receiver.NotifPubReceiver;
import com.template.project.core.utils.ntp.TimeSynchroniser;
import com.template.project.core.R;

/**
 * Setup notifcation manager to show notifacation
 */
public class PostNotifUtil {

    private static final String TAG = PostNotifUtil.class.getSimpleName();

    public static int NOTIF_ID = 20160228;

    /** Create instance of Notification object. */
    public static Notification createNotif(Context ctx, int notifIconResId, String title, String msg) {
        NotificationCompat.Style style = new NotificationCompat.BigTextStyle()
                .setBigContentTitle(title)
                .setSummaryText(msg)
                .bigText(msg);

        NotificationCompat.Builder notifBuilder  = new NotificationCompat.Builder(ctx)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentTitle(title)
                .setContentText(msg)
                .setSmallIcon(notifIconResId)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setStyle(style)
                .setWhen(TimeSynchroniser.getInstance().getSystemTime())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notifBuilder.setColor(Color.RED);
            notifBuilder.setSmallIcon(notifIconResId);
        }

        Notification notification = notifBuilder.build();

        return  notification;
    }

    /** Show notification instantly right away. */
    public static void showNotif(Context ctx, int notifIconResId, String title, String msg) {
        Notification notification = createNotif(ctx, notifIconResId, title, msg);
        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID, notification );
    }

    /** Schedule display of notification with the delay specified.*/
    public static void scheduleNotification(Context ctx, int notifId, int notifIconResId,
                                            String title, String msg, long timeToShow) {
        Notification notification = createNotif(ctx, notifIconResId, title, msg);
        Intent notificationIntent = new Intent(ctx, NotifPubReceiver.class);
        notificationIntent.putExtra(NotifPubReceiver.NOTIFICATION_ID, notifId);
        notificationIntent.putExtra(NotifPubReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, notifId, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeToShow, pendingIntent);
    }

    /** Remove any notificaton which have the same id for this app. */
    public static void removeNotif(Context ctx) {
        NotificationManager manager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIF_ID);
    }

}
