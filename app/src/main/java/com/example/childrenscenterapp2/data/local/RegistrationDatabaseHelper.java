package com.example.childrenscenterapp2.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.childrenscenterapp2.data.models.RegistrationModel;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ניהול מסד נתונים מקומי של הרשמות
 */
public class RegistrationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "registrations.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_REGISTRATIONS = "registrations";
    private static final String TAG = "RegistrationDB";

    public RegistrationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_REGISTRATIONS + " (" +
                "id TEXT PRIMARY KEY," +
                "activityId TEXT," +
                "childId TEXT," +
                "childName TEXT," +
                "parentComment TEXT," +
                "parentScore INTEGER," +
                "feedbackComment TEXT," +
                "feedbackScore INTEGER," +
                "registeredAt TEXT" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTRATIONS);
        onCreate(db);
    }

    /**
     * המרה של אובייקט Timestamp למחרוזת תאריך קריאה
     */
    private String formatTimestamp(Object ts) {
        if (ts instanceof Timestamp) {
            Date date = ((Timestamp) ts).toDate();
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
        } else if (ts instanceof String) {
            return (String) ts;
        } else {
            return null;
        }
    }

    /**
     * מוסיף או מעדכן הרשמה לפי id
     */
    public void insertOrUpdateRegistration(RegistrationModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", model.getId());
        values.put("activityId", model.getActivityId());
        values.put("childId", model.getChildId());
        values.put("childName", model.getChildName());
        values.put("parentComment", model.getParentComment());
        values.put("parentScore", model.getParentScore());
        values.put("feedbackComment", model.getFeedbackComment());
        values.put("feedbackScore", model.getFeedbackScore());

        // ✅ המרה בטוחה של registeredAt
        String formattedDate = formatTimestamp(model.getRegisteredAt());
        values.put("registeredAt", formattedDate);

        long result = db.insertWithOnConflict(TABLE_REGISTRATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        if (result == -1) {
            Log.e(TAG, "❌ נכשל בהוספה/עדכון של הרשמה: " + model.getId());
        } else {
            Log.d(TAG, "✅ הרשמה נשמרה: " + model.getId());
        }
    }

    /**
     * מחיקת הרשמה לפי מזהה
     */
    public void deleteRegistrationById(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_REGISTRATIONS, "id=?", new String[]{id});
        db.close();

        if (rows > 0) {
            Log.d(TAG, "🗑️ הרשמה נמחקה: " + id);
        } else {
            Log.e(TAG, "⚠️ לא נמצאה הרשמה למחיקה: " + id);
        }
    }
}
