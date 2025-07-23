package com.example.childrenscenterapp2.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import com.example.childrenscenterapp2.data.models.User;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 3; // ⬅️ העלאה מגרסה 2 ל־3
    private static final String TABLE_USERS = "users";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "uid TEXT PRIMARY KEY," +
                "name TEXT," +
                "email TEXT," +
                "type TEXT," +
                "specialization TEXT," + // ✅ חדש: שדה התמחות (למדריך)
                "idNumber TEXT" +        // ✅ חדש: שדה ת"ז (להורה)
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // מחיקה ובנייה מחדש של הטבלה בעת שינוי מבנה
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * מוסיף משתמש חדש ל־SQLite
     */
    public void insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createUserContentValues(user);
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    /**
     * מוסיף או מעדכן משתמש לפי uid
     */
    public void insertOrUpdateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createUserContentValues(user);
        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /**
     * מוחק משתמש לפי uid
     */
    public void deleteUserByUid(String uid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, "uid=?", new String[]{uid});
        db.close();
    }

    /**
     * יוצר אובייקט ContentValues מה־User
     */
    private ContentValues createUserContentValues(User user) {
        ContentValues values = new ContentValues();
        values.put("uid", user.getUid());
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("type", user.getType());
        values.put("specialization", user.getSpecialization() != null ? user.getSpecialization() : "");
        values.put("idNumber", user.getIdNumber() != null ? user.getIdNumber() : "");
        return values;
    }
}
