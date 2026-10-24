package com.example.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.database.MedicationHistoryDatabaseHelper;
import com.example.model.MedicationModel;
import com.example.receiver.MedicationAlarmReceiver;

import java.util.Calendar;
import java.util.List;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";
    public static final String CHANNEL_ID = "medication_reminder_channel";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Medication Reminders";
            String description = "Alerts for scheduled medication administration doses";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 400, 200, 400});

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void scheduleMedicationReminder(Context context, MedicationModel med) {
        if (!med.isActive()) {
            cancelMedicationReminder(context, med.getId());
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        intent.putExtra("med_id", med.getId());
        intent.putExtra("med_name", med.getName());
        intent.putExtra("med_dosage", med.getDosage());
        intent.putExtra("med_instructions", med.getInstructions());
        intent.putExtra("med_time", med.getTimeFormatted());
        intent.putExtra("user_id", med.getUserId());

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                med.getId(),
                intent,
                flags
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, med.getTimeHour());
        calendar.set(Calendar.MINUTE, med.getTimeMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If time already passed today, set for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long triggerAtMillis = calendar.getTimeInMillis();
        Log.d(TAG, "Scheduling alarm for " + med.getName() + " at " + calendar.getTime());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } catch (SecurityException se) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    public static void cancelMedicationReminder(Context context, int medId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                medId,
                intent,
                flags
        );

        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Cancelled alarm for medication ID: " + medId);
    }

    public static void rescheduleAllReminders(Context context) {
        SessionManager session = new SessionManager(context);
        if (!session.isLoggedIn()) return;

        String userId = session.getUserId();
        MedicationHistoryDatabaseHelper db = new MedicationHistoryDatabaseHelper(context);
        List<MedicationModel> meds = db.getMedicationsForUser(userId);
        for (MedicationModel med : meds) {
            if (med.isActive()) {
                scheduleMedicationReminder(context, med);
            }
        }
    }
}
