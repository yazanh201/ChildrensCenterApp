package com.example.childrenscenterapp2.data.sync;

import android.content.Context;
import android.util.Log;
import com.example.childrenscenterapp2.data.local.ActivityDatabaseHelper;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.firestore.*;

/**
 * ActivitySyncManager – מחלקה האחראית על סנכרון פעילויות בין Firestore למסד הנתונים המקומי (SQLite).
 * מאזינה לשינויים ב-Collection של activities ומעדכנת את SQLite בהתאם.
 */
public class ActivitySyncManager {

    private final FirebaseFirestore firestore;           // חיבור למסד Firestore בענן
    private final ActivityDatabaseHelper dbHelper;       // מסד נתונים מקומי SQLite
    private ListenerRegistration registration;           // אובייקט לניהול ההאזנה (Listener)

    /**
     * קונסטרקטור – אתחול חיבור למסדי הנתונים.
     *
     * @param context הקשר (Context) המשמש את מסד הנתונים המקומי.
     */
    public ActivitySyncManager(Context context) {
        firestore = FirebaseFirestore.getInstance();
        dbHelper = new ActivityDatabaseHelper(context);
    }

    /**
     * התחלת האזנה לשינויים ב-activities ב-Firestore.
     * ✅ כל שינוי (הוספה/עדכון/מחיקה) יתעדכן גם במסד הנתונים המקומי.
     */
    public void startListening() {
        registration = firestore.collection("activities")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("ActivitySync", "❌ שגיאה בהאזנה לשינויים: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) return;

                    // מעבר על כל שינוי במסמך (הוספה, עדכון, מחיקה)
                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        ActivityModel activity = doc.toObject(ActivityModel.class);

                        switch (change.getType()) {
                            case ADDED:
                                dbHelper.insertOrUpdateActivity(activity);
                                Log.d("ActivitySync", "➕ פעילות חדשה נוספה: " + activity.getId() + " - " + activity.getName());
                                break;
                            case MODIFIED:
                                dbHelper.insertOrUpdateActivity(activity);
                                Log.d("ActivitySync", "✏️ פעילות עודכנה: " + activity.getId() + " - " + activity.getName());
                                break;
                            case REMOVED:
                                dbHelper.deleteActivityById(activity.getId());
                                Log.d("ActivitySync", "🗑️ פעילות נמחקה: " + activity.getId());
                                break;
                        }
                    }

                    Log.d("ActivitySync", "✅ סנכרון פעילויות הושלם");
                });
    }

    /**
     * הפסקת האזנה לשינויים ב-Firestore.
     * ✅ שימושי כאשר יוצאים מהמסך/המערכת ואין צורך להמשיך לסנכרן ברקע.
     */
    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
