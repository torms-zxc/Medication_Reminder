package com.example.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.model.UserModel;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "UserDatabaseHelper";
    private static final String DATABASE_NAME = "user_auth.db";
    private static final int DATABASE_VERSION = 1;

    // Table and Columns
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_CREATED_AT = "created_at";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_ID + " TEXT UNIQUE NOT NULL, "
            + COLUMN_PASSWORD + " TEXT NOT NULL, "
            + COLUMN_FULL_NAME + " TEXT NOT NULL, "
            + COLUMN_CREATED_AT + " INTEGER NOT NULL"
            + ");";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        Log.d(TAG, "User auth database created successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Hash password with SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 algorithm not available", e);
            return password; // Fallback
        }
    }

    /**
     * Register a new user in the SQLite database
     */
    public boolean registerUser(String userId, String password, String fullName) {
        if (userId == null || password == null || fullName == null) {
            return false;
        }
        String trimmedUserId = userId.trim().toLowerCase();
        if (isUserExists(trimmedUserId)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, trimmedUserId);
        values.put(COLUMN_PASSWORD, hashPassword(password));
        values.put(COLUMN_FULL_NAME, fullName.trim());
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis());

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /**
     * Check if a user ID is already registered
     */
    public boolean isUserExists(String userId) {
        if (userId == null) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USER_ID + " = ?",
                new String[]{userId.trim().toLowerCase()},
                null, null, null
        );

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    /**
     * Validate user credentials against the SQLite database
     */
    public boolean checkUserCredentials(String userId, String password) {
        if (userId == null || password == null) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USER_ID + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{userId.trim().toLowerCase(), hashedPassword},
                null, null, null
        );

        boolean isValid = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return isValid;
    }

    /**
     * Retrieve user profile details by user ID
     */
    public UserModel getUser(String userId) {
        if (userId == null) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_USER_ID, COLUMN_FULL_NAME, COLUMN_CREATED_AT},
                COLUMN_USER_ID + " = ?",
                new String[]{userId.trim().toLowerCase()},
                null, null, null
        );

        UserModel user = null;
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int userIdIndex = cursor.getColumnIndexOrThrow(COLUMN_USER_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME);
            int createdIndex = cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT);

            user = new UserModel(
                    cursor.getInt(idIndex),
                    cursor.getString(userIdIndex),
                    cursor.getString(nameIndex),
                    cursor.getLong(createdIndex)
            );
            cursor.close();
        }
        return user;
    }
}
