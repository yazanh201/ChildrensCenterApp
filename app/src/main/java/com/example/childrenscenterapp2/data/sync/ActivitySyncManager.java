package com.example.childrenscenterapp2.data.sync;

import android.content.Context;
import android.util.Log;
import com.example.childrenscenterapp2.data.local.ActivityDatabaseHelper;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.firestore.*;

public class ActivitySyncManager {
    private final FirebaseFirestore firestore;
    private final ActivityDatabaseHelper dbHelper;
    private ListenerRegistration registration;

    public ActivitySyncManager(Context context) {
        firestore = FirebaseFirestore.getInstance();
        dbHelper = new ActivityDatabaseHelper(context);
    }

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

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}