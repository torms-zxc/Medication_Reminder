package com.example.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.model.AdherenceStats;
import com.example.model.HistoryLogModel;
import com.example.model.MedicationModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MedicationHistoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "MedHistoryDbHelper";
    private static final String DATABASE_NAME = "medication_history.db";
    private static final int DATABASE_VERSION = 1;

    // Table: medications
    public static final String TABLE_MEDICATIONS = "medications";
    public static final String COL_MED_ID = "_id";
    public static final String COL_MED_USER_ID = "user_id";
    public static final String COL_MED_NAME = "name";
    public static final String COL_MED_DOSAGE = "dosage";
    public static final String COL_MED_TIME_HOUR = "time_hour";
    public static final String COL_MED_TIME_MINUTE = "time_minute";
    public static final String COL_MED_TIME_FORMATTED = "time_formatted";
    public static final String COL_MED_INSTRUCTIONS = "instructions";
    public static final String COL_MED_CATEGORY = "category";
    public static final String COL_MED_IS_ACTIVE = "is_active";
    public static final String COL_MED_CREATED_AT = "created_at";

    // Table: history_logs
    public static final String TABLE_HISTORY = "history_logs";
    public static final String COL_HIST_ID = "_id";
    public static final String COL_HIST_USER_ID = "user_id";
    public static final String COL_HIST_MED_ID = "medication_id";
    public static final String COL_HIST_MED_NAME = "medication_name";
    public static final String COL_HIST_DOSAGE = "dosage";
    public static final String COL_HIST_SCHEDULED_TIME = "scheduled_time";
    public static final String COL_HIST_ACTION_TIME = "action_time";
    public static final String COL_HIST_STATUS = "status"; // "TAKEN", "SKIPPED", "MISSED"
    public static final String COL_HIST_NOTES = "notes";

    private static final String CREATE_TABLE_MEDICATIONS = "CREATE TABLE " + TABLE_MEDICATIONS + " ("
            + COL_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_MED_USER_ID + " TEXT NOT NULL, "
            + COL_MED_NAME + " TEXT NOT NULL, "
            + COL_MED_DOSAGE + " TEXT NOT NULL, "
            + COL_MED_TIME_HOUR + " INTEGER NOT NULL, "
            + COL_MED_TIME_MINUTE + " INTEGER NOT NULL, "
            + COL_MED_TIME_FORMATTED + " TEXT NOT NULL, "
            + COL_MED_INSTRUCTIONS + " TEXT, "
            + COL_MED_CATEGORY + " TEXT, "
            + COL_MED_IS_ACTIVE + " INTEGER DEFAULT 1, "
            + COL_MED_CREATED_AT + " INTEGER NOT NULL"
            + ");";

    private static final String CREATE_TABLE_HISTORY = "CREATE TABLE " + TABLE_HISTORY + " ("
            + COL_HIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_HIST_USER_ID + " TEXT NOT NULL, "
            + COL_HIST_MED_ID + " INTEGER, "
            + COL_HIST_MED_NAME + " TEXT NOT NULL, "
            + COL_HIST_DOSAGE + " TEXT NOT NULL, "
            + COL_HIST_SCHEDULED_TIME + " TEXT NOT NULL, "
            + COL_HIST_ACTION_TIME + " INTEGER NOT NULL, "
            + COL_HIST_STATUS + " TEXT NOT NULL, "
            + COL_HIST_NOTES + " TEXT"
            + ");";

    public MedicationHistoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEDICATIONS);
        db.execSQL(CREATE_TABLE_HISTORY);
        Log.d(TAG, "Medication & History SQLite database tables created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    // ==========================================
    // MEDICATION SCHEDULE OPERATIONS
    // ==========================================

    /**
     * Add a new medication schedule to SQLite
     */
    public long insertMedication(MedicationModel med) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MED_USER_ID, med.getUserId());
        values.put(COL_MED_NAME, med.getName().trim());
        values.put(COL_MED_DOSAGE, med.getDosage().trim());
        values.put(COL_MED_TIME_HOUR, med.getTimeHour());
        values.put(COL_MED_TIME_MINUTE, med.getTimeMinute());
        values.put(COL_MED_TIME_FORMATTED, med.getTimeFormatted());
        values.put(COL_MED_INSTRUCTIONS, med.getInstructions() != null ? med.getInstructions().trim() : "");
        values.put(COL_MED_CATEGORY, med.getCategory() != null ? med.getCategory() : "General");
        values.put(COL_MED_IS_ACTIVE, med.isActive() ? 1 : 0);
        values.put(COL_MED_CREATED_AT, System.currentTimeMillis());

        return db.insert(TABLE_MEDICATIONS, null, values);
    }

    /**
     * Update an existing medication schedule
     */
    public boolean updateMedication(MedicationModel med) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MED_NAME, med.getName().trim());
        values.put(COL_MED_DOSAGE, med.getDosage().trim());
        values.put(COL_MED_TIME_HOUR, med.getTimeHour());
        values.put(COL_MED_TIME_MINUTE, med.getTimeMinute());
        values.put(COL_MED_TIME_FORMATTED, med.getTimeFormatted());
        values.put(COL_MED_INSTRUCTIONS, med.getInstructions() != null ? med.getInstructions().trim() : "");
        values.put(COL_MED_CATEGORY, med.getCategory() != null ? med.getCategory() : "General");
        values.put(COL_MED_IS_ACTIVE, med.isActive() ? 1 : 0);

        int rows = db.update(
                TABLE_MEDICATIONS,
                values,
                COL_MED_ID + " = ? AND " + COL_MED_USER_ID + " = ?",
                new String[]{String.valueOf(med.getId()), med.getUserId()}
        );
        return rows > 0;
    }

    /**
     * Delete a medication schedule and associated logs
     */
    public boolean deleteMedication(int medId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_MEDICATIONS,
                COL_MED_ID + " = ? AND " + COL_MED_USER_ID + " = ?",
                new String[]{String.valueOf(medId), userId}
        );
        return rows > 0;
    }

    /**
     * Retrieve a single medication by ID
     */
    public MedicationModel getMedicationById(int medId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_MEDICATIONS,
                null,
                COL_MED_ID + " = ?",
                new String[]{String.valueOf(medId)},
                null, null, null
        );

        MedicationModel med = null;
        if (cursor != null && cursor.moveToFirst()) {
            med = parseMedicationCursor(cursor);
            cursor.close();
        }
        return med;
    }

    /**
     * Retrieve all medications for a user, sorted chronologically by time
     */
    public List<MedicationModel> getMedicationsForUser(String userId) {
        List<MedicationModel> list = new ArrayList<>();
        if (userId == null) return list;

        SQLiteDatabase db = this.getReadableDatabase();
        String orderBy = COL_MED_TIME_HOUR + " ASC, " + COL_MED_TIME_MINUTE + " ASC";
        Cursor cursor = db.query(
                TABLE_MEDICATIONS,
                null,
                COL_MED_USER_ID + " = ?",
                new String[]{userId},
                null, null, orderBy
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                MedicationModel med = parseMedicationCursor(cursor);
                // Check today's status
                med.setTodayStatus(getTodayStatusForMedication(med.getId(), userId));
                list.add(med);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private MedicationModel parseMedicationCursor(Cursor cursor) {
        MedicationModel med = new MedicationModel();
        med.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MED_ID)));
        med.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_USER_ID)));
        med.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_NAME)));
        med.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_DOSAGE)));
        med.setTimeHour(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MED_TIME_HOUR)));
        med.setTimeMinute(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MED_TIME_MINUTE)));
        med.setTimeFormatted(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_TIME_FORMATTED)));
        med.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_INSTRUCTIONS)));
        med.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_CATEGORY)));
        med.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MED_IS_ACTIVE)) == 1);
        med.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_MED_CREATED_AT)));
        return med;
    }

    // ==========================================
    // HISTORY LOGS OPERATIONS
    // ==========================================

    /**
     * Check if medication was already logged today
     */
    public String getTodayStatusForMedication(int medId, String userId) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_HISTORY,
                new String[]{COL_HIST_STATUS},
                COL_HIST_USER_ID + " = ? AND " + COL_HIST_MED_ID + " = ? AND " + COL_HIST_ACTION_TIME + " >= ?",
                new String[]{userId, String.valueOf(medId), String.valueOf(startOfDay)},
                null, null,
                COL_HIST_ACTION_TIME + " DESC",
                "1"
        );

        String status = "PENDING";
        if (cursor != null && cursor.moveToFirst()) {
            status = cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_STATUS));
            cursor.close();
        }
        return status;
    }

    /**
     * Insert a medication administration log
     */
    public long insertLog(HistoryLogModel log) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_HIST_USER_ID, log.getUserId());
        values.put(COL_HIST_MED_ID, log.getMedicationId());
        values.put(COL_HIST_MED_NAME, log.getMedicationName());
        values.put(COL_HIST_DOSAGE, log.getDosage());
        values.put(COL_HIST_SCHEDULED_TIME, log.getScheduledTime());
        values.put(COL_HIST_ACTION_TIME, log.getActionTime() > 0 ? log.getActionTime() : System.currentTimeMillis());
        values.put(COL_HIST_STATUS, log.getStatus());
        values.put(COL_HIST_NOTES, log.getNotes() != null ? log.getNotes() : "");

        return db.insert(TABLE_HISTORY, null, values);
    }

    /**
     * Retrieve administration history logs for a user with optional status filter
     */
    public List<HistoryLogModel> getLogsForUser(String userId, String statusFilter) {
        List<HistoryLogModel> logs = new ArrayList<>();
        if (userId == null) return logs;

        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_HIST_USER_ID + " = ?";
        List<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(userId);

        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
            selection += " AND " + COL_HIST_STATUS + " = ?";
            selectionArgsList.add(statusFilter.toUpperCase());
        }

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);
        String orderBy = COL_HIST_ACTION_TIME + " DESC";

        Cursor cursor = db.query(TABLE_HISTORY, null, selection, selectionArgs, null, null, orderBy);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                HistoryLogModel log = new HistoryLogModel();
                log.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIST_ID)));
                log.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_USER_ID)));
                log.setMedicationId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIST_MED_ID)));
                log.setMedicationName(cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_MED_NAME)));
                log.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_DOSAGE)));
                log.setScheduledTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_SCHEDULED_TIME)));
                log.setActionTime(cursor.getLong(cursor.getColumnIndexOrThrow(COL_HIST_ACTION_TIME)));
                log.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_STATUS)));
                log.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_NOTES)));
                logs.add(log);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return logs;
    }

    /**
     * Calculate user adherence statistics
     */
    public AdherenceStats getStatsForUser(String userId) {
        if (userId == null) return new AdherenceStats(0, 0, 0);

        SQLiteDatabase db = this.getReadableDatabase();
        int total = 0;
        int taken = 0;
        int skipped = 0;

        Cursor cursor = db.rawQuery(
                "SELECT " + COL_HIST_STATUS + ", COUNT(*) FROM " + TABLE_HISTORY
                        + " WHERE " + COL_HIST_USER_ID + " = ? GROUP BY " + COL_HIST_STATUS,
                new String[]{userId}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String status = cursor.getString(0);
                int count = cursor.getInt(1);
                total += count;
                if ("TAKEN".equalsIgnoreCase(status)) {
                    taken += count;
                } else if ("SKIPPED".equalsIgnoreCase(status)) {
                    skipped += count;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        return new AdherenceStats(total, taken, skipped);
    }

    /**
     * Clear all history logs for a user
     */
    public boolean clearLogsForUser(String userId) {
        if (userId == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_HISTORY, COL_HIST_USER_ID + " = ?", new String[]{userId});
        return rows >= 0;
    }
}
