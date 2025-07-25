package com.example.childrenscenterapp2.data.sync;

import android.content.Context;
import android.util.Log;

import com.example.childrenscenterapp2.data.local.RegistrationDatabaseHelper;
import com.example.childrenscenterapp2.data.models.RegistrationModel;
import com.google.firebase.firestore.*;

/**
 * RegistrationSyncManager – מחלקה האחראית על סנכרון הרשמות (Registrations)
 * בין Firestore לבין מסד הנתונים המקומי (SQLite).
 * ✅ מתמקדת בהרשמות של פעילות מסוימת לפי activityId.
 */
public class RegistrationSyncManager {

    private final FirebaseFirestore firestore;                 // חיבור ל-Firestore בענן
    private final RegistrationDatabaseHelper dbHelper;         // מסד נתונים מקומי (SQLite) לניהול הרשמות
    private ListenerRegistration registration;                 // Listener לניהול ההאזנה
    private final String activityId;                           // מזהה הפעילות שאותה נסנכרן

    /**
     * קונסטרקטור – אתחול מנהל סנכרון הרשמות לפעילות מסוימת.
     *
     * @param context    ההקשר (Context) עבור מסד הנתונים המקומי.
     * @param activityId מזהה הפעילות שאליה שייכות ההרשמות.
     */
    public RegistrationSyncManager(Context context, String activityId) {
        this.firestore = FirebaseFirestore.getInstance();
        this.dbHelper = new RegistrationDatabaseHelper(context);
        this.activityId = activityId;
    }

    /**
     * הפעלת ההאזנה בזמן אמת לשינויים בהרשמות של פעילות מסוימת.
     * ✅ כל שינוי (הוספה, עדכון, מחיקה) יתעדכן גם במסד הנתונים המקומי (SQLite).
     */
    public void startListening() {
        registration = firestore.collection("activities")
                .document(activityId)
                .collection("registrations")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("RegistrationSync", "❌ שגיאה בסנכרון הרשמות לפעילות " + activityId + ": " + error.getMessage());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) return;

                    // מעבר על כל שינוי בהרשמות
                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        RegistrationModel model = doc.toObject(RegistrationModel.class);

                        // ✅ חשוב להוסיף מזהה מסמך ידנית (Firestore לא עושה זאת אוטומטית)
                        model.setId(doc.getId());

                        switch (change.getType()) {
                            case ADDED:
                            case MODIFIED:
                                dbHelper.insertOrUpdateRegistration(model);
                                Log.d("RegistrationSync", "✅ נשמרה הרשמה: " + model.getId());
                                break;
                            case REMOVED:
                                dbHelper.deleteRegistrationById(model.getId());
                                Log.d("RegistrationSync", "🗑️ נמחקה הרשמה: " + model.getId());
                                break;
                        }
                    }
                });
    }

    /**
     * הפסקת ההאזנה לשינויים בהרשמות.
     * ✅ שימושי כאשר יוצאים מהמסך/המערכת ואין צורך להמשיך בסנכרון ברקע.
     */
    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
