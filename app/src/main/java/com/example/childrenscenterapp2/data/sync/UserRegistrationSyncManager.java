package com.example.childrenscenterapp2.data.sync;

import android.content.Context;
import android.util.Log;

import com.example.childrenscenterapp2.data.local.UserRegistrationDatabaseHelper;
import com.example.childrenscenterapp2.data.models.RegistrationForUserModel;
import com.google.firebase.firestore.*;

/**
 * UserRegistrationSyncManager – מחלקה לניהול סנכרון ההרשמות של משתמש מסוים.
 * ✅ מאזינה בזמן אמת לקולקציית "registrations" של המשתמש ב-Firestore
 * ✅ מסנכרנת את הנתונים עם מסד הנתונים המקומי (SQLite).
 */
public class UserRegistrationSyncManager {

    private final FirebaseFirestore firestore;                  // חיבור למסד Firestore בענן
    private final UserRegistrationDatabaseHelper dbHelper;      // מסד נתונים מקומי לניהול הרשמות משתמש
    private ListenerRegistration registration;                  // האזנה בזמן אמת לשינויים
    private final String userId;                                // מזהה המשתמש עבורו מתבצע הסנכרון

    /**
     * קונסטרקטור – אתחול מנהל סנכרון הרשמות של משתמש.
     *
     * @param context הקונטקסט עבור מסד הנתונים המקומי.
     * @param userId  מזהה המשתמש (UID) עבורו נבצע את הסנכרון.
     */
    public UserRegistrationSyncManager(Context context, String userId) {
        this.firestore = FirebaseFirestore.getInstance();
        this.dbHelper = new UserRegistrationDatabaseHelper(context);
        this.userId = userId;
    }

    /**
     * startListening – מתחיל האזנה בזמן אמת לקולקציית ההרשמות של המשתמש ב-Firestore.
     * ✅ כל שינוי (הוספה, עדכון, מחיקה) נשמר גם ב-SQLite.
     */
    public void startListening() {
        registration = firestore.collection("users")
                .document(userId)
                .collection("registrations")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("UserRegSync", "❌ שגיאה בסנכרון הרשמות למשתמש " + userId + ": " + error.getMessage());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) return;

                    // מעבר על כל שינוי בהרשמות המשתמש
                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        RegistrationForUserModel model = doc.toObject(RegistrationForUserModel.class);

                        if (model == null) continue;

                        // ✅ חשוב להגדיר את ה-ID מתוך מזהה המסמך (Document ID)
                        model.setId(doc.getId());

                        switch (change.getType()) {
                            case ADDED:
                            case MODIFIED:
                                dbHelper.insertOrUpdateRegistration(model);
                                Log.d("UserRegSync", "✅ הרשמה נשמרה: " + model.getId());
                                break;
                            case REMOVED:
                                dbHelper.deleteRegistrationById(model.getId());
                                Log.d("UserRegSync", "🗑️ נמחקה הרשמה: " + model.getId());
                                break;
                        }
                    }
                });
    }

    /**
     * stopListening – מפסיק האזנה לשינויים.
     * ✅ שימושי כאשר יוצאים מהמסך או סוגרים את המערכת.
     */
    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
