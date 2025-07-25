package com.example.childrenscenterapp2.data.sync;

import android.content.Context;
import android.util.Log;

import com.example.childrenscenterapp2.data.local.RegistrationDatabaseHelper;
import com.example.childrenscenterapp2.data.models.RegistrationModel;
import com.google.firebase.firestore.*;

public class RegistrationSyncManager {

    private final FirebaseFirestore firestore;
    private final RegistrationDatabaseHelper dbHelper;
    private ListenerRegistration registration;
    private final String activityId;

    public RegistrationSyncManager(Context context, String activityId) {
        this.firestore = FirebaseFirestore.getInstance();
        this.dbHelper = new RegistrationDatabaseHelper(context);
        this.activityId = activityId;
    }

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

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        RegistrationModel model = doc.toObject(RegistrationModel.class);

                        // ** חשוב! להוסיף את מזהה המסמך ידנית **
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

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
