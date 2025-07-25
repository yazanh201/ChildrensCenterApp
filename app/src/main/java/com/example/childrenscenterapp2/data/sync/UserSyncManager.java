package com.example.childrenscenterapp2.data.sync;

import android.content.Context;
import android.util.Log;

import com.example.childrenscenterapp2.data.local.UserDatabaseHelper;
import com.example.childrenscenterapp2.data.models.User;
import com.google.firebase.firestore.*;

/**
 * UserSyncManager – מחלקה לניהול סנכרון משתמשים בין Firestore למסד הנתונים המקומי (SQLite).
 * ✅ מאזינה בזמן אמת לכל שינוי בקולקציית "users".
 * ✅ מסנכרנת אוטומטית את הנתונים עם SQLite ומנהלת הוספה, עדכון ומחיקה.
 */
public class UserSyncManager {

    private final FirebaseFirestore firestore;         // חיבור למסד Firestore בענן
    private final UserDatabaseHelper dbHelper;         // מנהל מסד הנתונים המקומי של המשתמשים
    private ListenerRegistration registration;         // האזנה בזמן אמת לשינויים ב-Firestore

    /**
     * קונסטרקטור – אתחול סנכרון משתמשים.
     *
     * @param context הקונטקסט עבור יצירת מסד הנתונים המקומי.
     */
    public UserSyncManager(Context context) {
        firestore = FirebaseFirestore.getInstance();
        dbHelper = new UserDatabaseHelper(context);
    }

    /**
     * startListening – מתחיל האזנה בזמן אמת לשינויים בקולקציית המשתמשים ב-Firestore.
     * ✅ מוסיף או מעדכן משתמשים במסד המקומי במקרה של ADDED או MODIFIED.
     * ✅ מוחק מהמסד המקומי במקרה של REMOVED.
     */
    public void startListening() {
        registration = firestore.collection("users")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("UserSync", "❌ שגיאה בסנכרון משתמשים: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) return;

                    // מעבר על כל שינוי במסמכים
                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        User user = doc.toObject(User.class);

                        if (user == null || user.getUid() == null) {
                            Log.w("UserSync", "⚠️ משתמש לא תקין – דילוג");
                            continue;
                        }

                        String uid = user.getUid();
                        String name = user.getName();
                        String type = user.getType();

                        switch (change.getType()) {
                            case ADDED:
                                dbHelper.insertOrUpdateUser(user);
                                Log.d("UserSync", "➕ נוסף משתמש: " + name + " (uid: " + uid + ", סוג: " + type + ")");
                                logExtraFields(user);
                                break;

                            case MODIFIED:
                                dbHelper.insertOrUpdateUser(user);
                                Log.d("UserSync", "✏️ עודכן משתמש: " + name + " (uid: " + uid + ", סוג: " + type + ")");
                                logExtraFields(user);
                                break;

                            case REMOVED:
                                dbHelper.deleteUserByUid(uid);
                                Log.d("UserSync", "🗑️ נמחק משתמש: " + name + " (uid: " + uid + ")");
                                break;
                        }
                    }

                    Log.d("UserSync", "✅ סנכרון משתמשים הושלם");
                });
    }

    /**
     * logExtraFields – פונקציה עזר להדפסת מידע נוסף לפי סוג המשתמש.
     *
     * @param user אובייקט המשתמש שממנו נשלפים השדות הנוספים.
     */
    private void logExtraFields(User user) {
        switch (user.getType()) {
            case "מדריך":
                Log.d("UserSync", "📚 תחום התמחות: " + user.getSpecialization());
                break;
            case "הורה":
                Log.d("UserSync", "👨‍👧 תעודת זהות: " + user.getIdNumber());
                break;
        }
    }

    /**
     * stopListening – מפסיק האזנה לשינויים בקולקציית המשתמשים.
     * ✅ שימושי כאשר יוצאים מהמסך או עוצרים את הסנכרון.
     */
    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
            Log.d("UserSync", "🛑 הופסקה ההאזנה לעדכוני משתמשים");
        }
    }
}
