package com.template.project.core.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Utility class for scheduling Alarm
 */
public class AlarmScheduler {

    private static boolean sIsShowNotification;
    private static int sNotificationId;
    private static int sNotifIconResId;
    private static String sNotifTitle;
    private static String sNotifMessage;
    private static long sAlarmDate;

    private static AlarmScheduler sAlarmScheduler;

    public static AlarmScheduler getInstance() {
        if (sAlarmScheduler == null) {
            sAlarmScheduler = new AlarmScheduler();
        }
        return sAlarmScheduler;
    }

    /**
     * Create alarm details to be used for scheduling alarm.
     * @param isShowNotification true if Alarm will show notification.
     * @param notificationId The id of notification.
     * @param notifResId The notification resource drawable icon which will show in notification.
     * @param notifTitle The notification title to be shown.
     * @param notifMessage The notification message which will the notification will display.
     * @param tsAlarmDate The timestamp the alarm will trigger and show notification if
     *                    showNotification is set to true;
     * @return
     */
    public static AlarmScheduler createAlarm(boolean isShowNotification, int notificationId,
                 int notifResId, String notifTitle, String notifMessage, long tsAlarmDate) {
        sIsShowNotification = isShowNotification;
        sNotificationId = notificationId;
        sNotifIconResId = notifResId;
        sNotifTitle = notifTitle;
        sNotifMessage = notifMessage;
        sAlarmDate = tsAlarmDate;
        return sAlarmScheduler;
    }

    /**
     * Schedule the alarm details created with createAlarm.
     */
    public static void scheduleAlarm(Context context) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(AlarmBroadcastReceiver.EXTRAS_IS_SHOW_NOTIFICATION, sIsShowNotification);
        intent.putExtra(AlarmBroadcastReceiver.EXTRAS_NOTIFICATION_ID, sNotificationId);
        intent.putExtra(AlarmBroadcastReceiver.EXTRAS_DRAWABLE_NOTIF_ICON, sNotifIconResId);
        intent.putExtra(AlarmBroadcastReceiver.EXTRAS_NOTIFICATION_TITLE, sNotifTitle);
        intent.putExtra(AlarmBroadcastReceiver.EXTRAS_NOTIFICATION_MESSAGE, sNotifMessage);
        intent.putExtra(AlarmBroadcastReceiver.EXTRAS_NOTIFICATION_CONTENT, sNotifMessage);
        PendingIntent sender = PendingIntent.getBroadcast(context, sNotificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, sAlarmDate, sender);
    }
}
