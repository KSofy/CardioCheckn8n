package com.example.cardiocheck;

import android.app.AlarmManager;import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cardiocheck.utils.SharedPreferencesHelper;

/**
 * BroadcastReceiver para manejar recordatorios de mediciones de presión arterial
 */
public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "cardiocheck_reminders";
    public static final String CHANNEL_NAME = "Recordatorios de Medición";
    public static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SharedPreferencesHelper.isLoggedIn(context)) {
            return;
        }
        createNotificationChannel(context);
        showReminderNotification(context);
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Recordatorios para tomar mediciones de presión arterial");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showReminderNotification(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_heart_reminder)
                .setContentTitle("Recordatorio CardioCheck")
                .setContentText("Es hora de tomar tu medición de presión arterial")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("No olvides registrar tu presión arterial para mantener un seguimiento completo de tu salud cardiovascular."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 250, 250, 250});

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void scheduleReminder(Context context, int hourOfDay, int minute) {
        createNotificationChannel(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(java.util.Calendar.MINUTE, minute);
        calendar.set(java.util.Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }

        SharedPreferencesHelper.setReminderEnabled(context, true);
        SharedPreferencesHelper.setReminderTime(context, hourOfDay, minute);
    }

    public static void cancelReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        SharedPreferencesHelper.setReminderEnabled(context, false);
    }

    public static void sendCustomNotification(Context context, String title, String message, String subText, boolean highPriority) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // =====================================================================
        //  AQUÍ ESTÁ EL CAMBIO: Se añade '(int)' para convertir el long a int
        // =====================================================================
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) (System.currentTimeMillis() / 1000), intent, // Usar un requestCode único
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int priority = highPriority ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT;
        int notificationId = highPriority ? NOTIFICATION_ID + 1 : NOTIFICATION_ID;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_heart_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSubText(subText)
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (highPriority) {
            builder.setVibrate(new long[]{0, 500, 250, 500})
                    .setLights(android.graphics.Color.RED, 1000, 1000);
        } else {
            builder.setVibrate(new long[]{0, 250, 250, 250});
        }

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
