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
     * מדפיס שדות ייחודיים לפי סוג המשתמש
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

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
            Log.d("UserSync", "🛑 הופסקה ההאזנה לעדכוני משתמשים");
        }
    }
}