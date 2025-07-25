package com.example.childrenscenterapp2.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import com.example.childrenscenterapp2.data.models.ActivityModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@code ActivityDatabaseHelper} – מחלקת עזר לניהול מסד נתונים מקומי (SQLite) עבור פעילויות.
 * <p>
 * תפקיד המחלקה:
 * <ul>
 *   <li>יצירה וניהול של טבלת פעילויות מקומית.</li>
 *   <li>ביצוע פעולות CRUD (יצירה, קריאה, עדכון ומחיקה) על פעילויות.</li>
 *   <li>סנכרון נתונים בין מסד הנתונים המקומי לבין Firebase Firestore.</li>
 * </ul>
 */
public class ActivityDatabaseHelper extends SQLiteOpenHelper {

    /** שם בסיס הנתונים */
    private static final String DATABASE_NAME = "activities.db";

    /** גרסת בסיס הנתונים */
    private static final int DATABASE_VERSION = 1;

    /** שם הטבלה */
    private static final String TABLE_NAME = "activities";

    /** שמות העמודות בטבלה */
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DOMAIN = "domain";
    private static final String COL_MIN_AGE = "min_age";
    private static final String COL_MAX_AGE = "max_age";
    private static final String COL_DAYS = "days"; // נשמר כמחרוזת מופרדת בפסיקים
    private static final String COL_MAX_PARTICIPANTS = "max_participants";

    /**
     * בנאי המחלקה – יוצר חיבור למסד הנתונים.
     *
     * @param context ההקשר (Context) של האפליקציה.
     */
    public ActivityDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * יצירת טבלה בבסיס הנתונים המקומי בפעם הראשונה.
     *
     * @param db מופע מסד הנתונים.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_NAME + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_DOMAIN + " TEXT, " +
                COL_MIN_AGE + " INTEGER, " +
                COL_MAX_AGE + " INTEGER, " +
                COL_DAYS + " TEXT, " +
                COL_MAX_PARTICIPANTS + " INTEGER)";
        db.execSQL(query);
    }

    /**
     * טיפול בעדכון מבנה הטבלה בעת שינוי גרסה.
     *
     * @param db         מופע מסד הנתונים.
     * @param oldVersion גרסה ישנה.
     * @param newVersion גרסה חדשה.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * הוספת פעילות חדשה למסד הנתונים המקומי או החלפתה אם כבר קיימת.
     *
     * @param activity אובייקט פעילות להוספה.
     */
    public void insertActivity(ActivityModel activity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, activity.getId());
        values.put(COL_NAME, activity.getName());
        values.put(COL_DESCRIPTION, activity.getDescription());
        values.put(COL_DOMAIN, activity.getDomain());
        values.put(COL_MIN_AGE, activity.getMinAge());
        values.put(COL_MAX_AGE, activity.getMaxAge());
        values.put(COL_DAYS, String.join(",", activity.getDays()));
        values.put(COL_MAX_PARTICIPANTS, activity.getMaxParticipants());

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /**
     * שליפת כל הפעילויות מהמסד המקומי.
     *
     * @return רשימת פעילויות.
     */
    public List<ActivityModel> getAllActivities() {
        List<ActivityModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String description = cursor.getString(2);
                String domain = cursor.getString(3);
                int minAge = cursor.getInt(4);
                int maxAge = cursor.getInt(5);
                List<String> days = Arrays.asList(cursor.getString(6).split(","));
                int maxParticipants = cursor.getInt(7);

                ActivityModel activity = new ActivityModel(
                        id, name, description, domain, minAge, maxAge, days, maxParticipants
                );

                list.add(activity);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    /**
     * מחיקת כל הפעילויות מהמסד המקומי (לצורך סנכרון מחדש).
     */
    public void clearActivities() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    /**
     * הוספה או עדכון פעילות לפי מזהה.
     *
     * @param activity פעילות להוספה או עדכון.
     */
    public void insertOrUpdateActivity(ActivityModel activity) {
        insertActivity(activity); // שימוש ב-CONFLICT_REPLACE כדי לעדכן אם קיים.
    }

    /**
     * מחיקת פעילות לפי מזהה.
     *
     * @param id מזהה הפעילות למחיקה.
     */
    public void deleteActivityById(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{id});
        db.close();
    }

    /**
     * עדכון פעילות קיימת לפי מזהה.
     *
     * @param activity פעילות לעדכון.
     * @return {@code true} אם העדכון הצליח, אחרת {@code false}.
     */
    public boolean updateActivity(ActivityModel activity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, activity.getName());
        values.put(COL_DESCRIPTION, activity.getDescription());
        values.put(COL_DOMAIN, activity.getDomain());
        values.put(COL_MIN_AGE, activity.getMinAge());
        values.put(COL_MAX_AGE, activity.getMaxAge());
        values.put(COL_DAYS, String.join(",", activity.getDays()));
        values.put(COL_MAX_PARTICIPANTS, activity.getMaxParticipants());

        int rowsAffected = db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{activity.getId()});
        db.close();
        return rowsAffected > 0; // ✅ מחזיר true אם לפחות שורה אחת עודכנה.
    }
}
