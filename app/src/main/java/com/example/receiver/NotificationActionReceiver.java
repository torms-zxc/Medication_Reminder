package com.example.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.database.MedicationHistoryDatabaseHelper;
import com.example.model.HistoryLogModel;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        int medId = intent.getIntExtra("med_id", -1);
        String medName = intent.getStringExtra("med_name");
        String dosage = intent.getStringExtra("med_dosage");
        String scheduledTime = intent.getStringExtra("scheduled_time");
        String userId = intent.getStringExtra("user_id");
        int notificationId = intent.getIntExtra("notification_id", medId);

        if (userId != null && medName != null) {
            MedicationHistoryDatabaseHelper db = new MedicationHistoryDatabaseHelper(context);
            String status = "com.example.ACTION_MARK_TAKEN".equals(action) ? "TAKEN" : "SKIPPED";
            String note = "Logged from notification action";

            HistoryLogModel log = new HistoryLogModel(
                    0,
                    userId,
                    medId,
                    medName,
                    dosage != null ? dosage : "",
                    scheduledTime != null ? scheduledTime : "Scheduled",
                    System.currentTimeMillis(),
                    status,
                    note
            );

            db.insertLog(log);

            if ("TAKEN".equals(status)) {
                Toast.makeText(context, "Logged " + medName + " as Taken ✓", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Logged " + medName + " as Skipped", Toast.LENGTH_SHORT).show();
            }
        }

        // Dismiss notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(notificationId);
        }
    }
}
