package com.example.childrenscenterapp2.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.childrenscenterapp2.data.models.RegistrationForUserModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helper לניהול מסד הנתונים המקומי של הרשמות משתמשים.
 */
public class UserRegistrationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_registrations.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "user_registrations";

    private static final String COL_ID = "id";  // מזהה ייחודי
    private static final String COL_ACTIVITY_ID = "activityId";
    private static final String COL_DAYS = "days"; // נשמר כמחרוזת עם פסיקים
    private static final String COL_DOMAIN = "domain";
    private static final String COL_TIMESTAMP = "timestamp";

    private static final String TAG = "UserRegDB";

    public UserRegistrationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_ACTIVITY_ID + " TEXT, " +
                COL_DAYS + " TEXT, " +
                COL_DOMAIN + " TEXT, " +
                COL_TIMESTAMP + " TEXT" +
                ")";
        db.execSQL(createTable);
        Log.d(TAG, "Table created: " + TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // בגרסה חדשה מוחקים את הטבלה ומייצרים מחדש (לשדרוג עתידי)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        Log.d(TAG, "Table dropped: " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * מוסיף או מעדכן הרשמה של משתמש לפי מזהה ייחודי.
     */
    public void insertOrUpdateRegistration(RegistrationForUserModel model) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, model.getActivityId()); // חשוב! אם יש מזהה אחר יש לשנות בהתאם
        values.put(COL_ACTIVITY_ID, model.getActivityId());
        values.put(COL_DAYS, model.getDays() != null ? String.join(",", model.getDays()) : "");
        values.put(COL_DOMAIN, model.getDomain());
        values.put(COL_TIMESTAMP, model.getTimestamp() != null ? model.getTimestamp().toDate().toString() : "");

        long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        if (result == -1) {
            Log.e(TAG, "Failed to insert/update registration: " + model.getActivityId());
        } else {
            Log.d(TAG, "Inserted/updated registration: " + model.getActivityId());
        }
    }

    /**
     * מחיקת הרשמה לפי מזהה ייחודי.
     */
    public void deleteRegistrationById(String id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COL_ID + "=?", new String[]{id});
        db.close();

        if (rows > 0) {
            Log.d(TAG, "Deleted registration: " + id);
        } else {
            Log.w(TAG, "No registration found to delete with id: " + id);
        }
    }

    /**
     * שליפה של כל ההרשמות מהמסד המקומי.
     */
    public List<RegistrationForUserModel> getAllRegistrations() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) cursor.close();
            db.close();
            Log.d(TAG, "No registrations found in local DB.");
            return Collections.emptyList();
        }

        List<RegistrationForUserModel> list = new java.util.ArrayList<>();
        do {
            RegistrationForUserModel model = new RegistrationForUserModel();
            model.setActivityId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ACTIVITY_ID)));

            String daysString = cursor.getString(cursor.getColumnIndexOrThrow(COL_DAYS));
            if (daysString != null && !daysString.isEmpty()) {
                model.setDays(Arrays.asList(daysString.split(",")));
            }

            model.setDomain(cursor.getString(cursor.getColumnIndexOrThrow(COL_DOMAIN)));

            // לאט לאט ניתן להוסיף טיפול ב־timestamp אם תרצה

            list.add(model);
        } while (cursor.moveToNext());

        cursor.close();
        db.close();

        Log.d(TAG, "Retrieved " + list.size() + " registrations from local DB.");
        return list;
    }
}
