package com.example.childrenscenterapp2.data.sync;

import android.content.Context;
import android.util.Log;

import com.example.childrenscenterapp2.data.local.UserDatabaseHelper;
import com.example.childrenscenterapp2.data.models.User;
import com.google.firebase.firestore.*;

public class UserSyncManager {
    private final FirebaseFirestore firestore;
    private final UserDatabaseHelper dbHelper;
    private ListenerRegistration registration;

    public UserSyncManager(Context context) {
        firestore = FirebaseFirestore.getInstance();
        dbHelper = new UserDatabaseHelper(context);
    }

    public void startListening() {
        registration = firestore.collection("users")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("UserSync", "❌ שגיאה בסנכרון משתמשים: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) return;

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        User user = doc.toObject(User.class);

                        switch (change.getType()) {
                            case ADDED:
                                dbHelper.insertOrUpdateUser(user);
                                Log.d("UserSync", "➕ נוסף משתמש: " + user.uid + " - " + user.name);
                                break;
                            case MODIFIED:
                                dbHelper.insertOrUpdateUser(user);
                                Log.d("UserSync", "✏️ עודכן משתמש: " + user.uid + " - " + user.name);
                                break;
                            case REMOVED:
                                dbHelper.deleteUserByUid(user.uid);
                                Log.d("UserSync", "🗑️ נמחק משתמש: " + user.uid + " - " + user.name);
                                break;
                        }
                    }

                    Log.d("UserSync", "✅ סנכרון משתמשים הושלם");
                });
    }

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
