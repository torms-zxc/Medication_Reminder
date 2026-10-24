package com.example.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.HomeActivity;
import com.example.R;
import com.example.database.MedicationHistoryDatabaseHelper;
import com.example.model.MedicationModel;
import com.example.util.AlarmScheduler;

public class MedicationAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "MedAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int medId = intent.getIntExtra("med_id", -1);
        String medName = intent.getStringExtra("med_name");
        String dosage = intent.getStringExtra("med_dosage");
        String instructions = intent.getStringExtra("med_instructions");
        String scheduledTime = intent.getStringExtra("med_time");
        String userId = intent.getStringExtra("user_id");

        if (medName == null) medName = "Medication";
        if (dosage == null) dosage = "Prescribed dose";
        if (scheduledTime == null) scheduledTime = "Now";

        Log.d(TAG, "Alarm triggered for: " + medName + " (" + dosage + ")");

        // Ensure notification channel exists
        AlarmScheduler.createNotificationChannel(context);

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted, skipping notification display.");
                rescheduleNextDay(context, medId);
                return;
            }
        }

        // Main Tap Action: Open HomeActivity
        Intent openAppIntent = new Intent(context, HomeActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, medId, openAppIntent, flags);

        // Action 1: Mark as Taken
        Intent takenIntent = new Intent(context, NotificationActionReceiver.class);
        takenIntent.setAction("com.example.ACTION_MARK_TAKEN");
        takenIntent.putExtra("med_id", medId);
        takenIntent.putExtra("med_name", medName);
        takenIntent.putExtra("med_dosage", dosage);
        takenIntent.putExtra("scheduled_time", scheduledTime);
        takenIntent.putExtra("user_id", userId);
        takenIntent.putExtra("notification_id", medId);
        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                context,
                medId * 10 + 1,
                takenIntent,
                flags
        );

        // Action 2: Skip Dose
        Intent skipIntent = new Intent(context, NotificationActionReceiver.class);
        skipIntent.setAction("com.example.ACTION_SKIP");
        skipIntent.putExtra("med_id", medId);
        skipIntent.putExtra("med_name", medName);
        skipIntent.putExtra("med_dosage", dosage);
        skipIntent.putExtra("scheduled_time", scheduledTime);
        skipIntent.putExtra("user_id", userId);
        skipIntent.putExtra("notification_id", medId);
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(
                context,
                medId * 10 + 2,
                skipIntent,
                flags
        );

        String contentText = dosage + " at " + scheduledTime;
        if (instructions != null && !instructions.trim().isEmpty()) {
            contentText += " • " + instructions.trim();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AlarmScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("Time to take " + medName)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.ic_check, "Mark Taken", takenPendingIntent)
                .addAction(R.drawable.ic_skip, "Skip", skipPendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(medId > 0 ? medId : 1001, builder.build());
        }

        // Reschedule alarm for next day
        rescheduleNextDay(context, medId);
    }

    private void rescheduleNextDay(Context context, int medId) {
        if (medId > 0) {
            MedicationHistoryDatabaseHelper db = new MedicationHistoryDatabaseHelper(context);
            MedicationModel med = db.getMedicationById(medId);
            if (med != null && med.isActive()) {
                AlarmScheduler.scheduleMedicationReminder(context, med);
            }
        }
    }
}
