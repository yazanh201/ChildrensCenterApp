package com.example.childrenscenterapp2.data.sync;

import android.content.Context;
import android.util.Log;
import com.example.childrenscenterapp2.data.local.ActivityDatabaseHelper;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.firestore.*;

import java.util.List;

/**
 * מחלקה שמאזינה לשינויים ב-Firestore ומסנכרנת ל-SQLite
 */
public class ActivitySyncManager {
    private final FirebaseFirestore firestore;
    private final ActivityDatabaseHelper dbHelper;
    private ListenerRegistration registration;

    public ActivitySyncManager(Context context) {
        firestore = FirebaseFirestore.getInstance();
        dbHelper = new ActivityDatabaseHelper(context);
    }

    /**
     * מאזין לשינויים בזמן אמת באוסף הפעילויות ומעדכן את SQLite בהתאם
     */
    public void startListening() {
        registration = firestore.collection("activities")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("ActivitySync", "❌ שגיאה בהאזנה לשינויים: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) return;

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        ActivityModel activity = doc.toObject(ActivityModel.class);

                        switch (change.getType()) {
                            case ADDED:
                            case MODIFIED:
                                dbHelper.insertOrUpdateActivity(activity);
                                break;
                            case REMOVED:
                                dbHelper.deleteActivityById(activity.getId());
                                break;
                        }
                    }

                    Log.d("ActivitySync", "✅ סנכרון בוצע מ-Firebase ל-SQLite");
                });
    }

    /**
     * הפסקת ההאזנה
     */
    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
