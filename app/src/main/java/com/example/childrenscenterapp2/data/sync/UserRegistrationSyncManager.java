package com.example.childrenscenterapp2.data.sync;

import android.content.Context;
import android.util.Log;

import com.example.childrenscenterapp2.data.local.UserRegistrationDatabaseHelper;
import com.example.childrenscenterapp2.data.models.RegistrationForUserModel;
import com.google.firebase.firestore.*;

public class UserRegistrationSyncManager {

    private final FirebaseFirestore firestore;
    private final UserRegistrationDatabaseHelper dbHelper;
    private ListenerRegistration registration;
    private final String userId;

    public UserRegistrationSyncManager(Context context, String userId) {
        this.firestore = FirebaseFirestore.getInstance();
        this.dbHelper = new UserRegistrationDatabaseHelper(context);
        this.userId = userId;
    }

    /**
     * מתחיל להאזין לשינויים בקולקציית ההרשמות של המשתמש ב-Firebase
     * ומסנכרן עם SQLite.
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

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        RegistrationForUserModel model = doc.toObject(RegistrationForUserModel.class);

                        if (model == null) continue;

                        // חשוב: להגדיר את ה-id מהמסמך
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
     * מפסיק להאזין לשינויים
     */
    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
