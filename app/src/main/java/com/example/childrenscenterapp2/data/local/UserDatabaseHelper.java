package com.example.childrenscenterapp2.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import com.example.childrenscenterapp2.data.models.User;

/**
 * {@code UserDatabaseHelper} – מחלקת עזר לניהול מסד נתונים מקומי (SQLite) עבור משתמשים.
 * <p>
 * תפקיד המחלקה:
 * <ul>
 *   <li>יצירה וניהול טבלת משתמשים מקומית.</li>
 *   <li>שמירה, עדכון ומחיקה של נתוני משתמשים.</li>
 *   <li>סנכרון נתונים בין Firebase למסד הנתונים המקומי במידת הצורך.</li>
 * </ul>
 */
public class UserDatabaseHelper extends SQLiteOpenHelper {

    /** שם מסד הנתונים */
    private static final String DATABASE_NAME = "users.db";

    /** גרסת מסד הנתונים */
    private static final int DATABASE_VERSION = 1;

    /** שם הטבלה */
    private static final String TABLE_USERS = "users";

    /**
     * בנאי המחלקה – יוצר חיבור למסד הנתונים המקומי.
     *
     * @param context ההקשר (Context) של האפליקציה.
     */
    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * יצירת טבלת המשתמשים בבסיס הנתונים בפעם הראשונה.
     *
     * @param db מופע מסד הנתונים.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "uid TEXT PRIMARY KEY," +
                "name TEXT," +
                "email TEXT," +
                "type TEXT)";
        db.execSQL(createTable);
    }

    /**
     * עדכון מבנה הטבלה במקרה של שינוי גרסה.
     *
     * @param db         מופע מסד הנתונים.
     * @param oldVersion גרסה ישנה.
     * @param newVersion גרסה חדשה.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * מוסיף משתמש חדש למסד הנתונים המקומי.
     *
     * @param user אובייקט המשתמש להוספה.
     */
    public void insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", user.getUid());
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("type", user.getType());
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    /**
     * מוסיף או מעדכן משתמש במסד הנתונים לפי ה-UID שלו.
     * אם המשתמש כבר קיים – הנתונים מתעדכנים, אחרת מתבצעת הוספה חדשה.
     *
     * @param user אובייקט המשתמש לשמירה או עדכון.
     */
    public void insertOrUpdateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", user.getUid());
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("type", user.getType());

        db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /**
     * מוחק משתמש מהמסד לפי מזהה ה-UID שלו.
     *
     * @param uid המזהה הייחודי של המשתמש למחיקה.
     */
    public void deleteUserByUid(String uid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", "uid=?", new String[]{uid});
        db.close();
    }

    /**
     * טיפול בירידת גרסה (Downgrade) – מוחק את הטבלה ומייצר אותה מחדש.
     * ⚠️ הערה: פעולה זו מוחקת את כל הנתונים ולכן אינה מתאימה לפרודקשן ללא גיבוי.
     *
     * @param db         מופע מסד הנתונים.
     * @param oldVersion גרסה ישנה.
     * @param newVersion גרסה חדשה.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
