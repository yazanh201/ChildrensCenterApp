package com.example.childrenscenterapp2.data.remote;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.childrenscenterapp2.data.local.UserDatabaseHelper;
import com.example.childrenscenterapp2.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import android.util.Log;

/**
 * AuthManager – מחלקה לניהול הרשמה והתחברות של משתמשים מול Firebase
 * כולל שמירה במסד Firebase Firestore ובסיס נתונים לוקאלי (SQLite)
 */
public class AuthManager {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * ממשק קריאה חוזרת – הצלחה/כישלון ברישום משתמש חדש
     */
    public interface OnAuthCompleteListener {
        void onSuccess();
        void onFailure(@NonNull Exception e);
    }

    /**
     * ממשק קריאה חוזרת – הצלחה/כישלון בהתחברות משתמש
     */
    public interface OnLoginCompleteListener {
        void onSuccess(String userType);
        void onFailure(@NonNull Exception e);
    }

    /**
     * רושם משתמש חדש במערכת:
     * - מוסיף אותו ל-Firebase Authentication
     * - שומר את פרטי המשתמש ב-Firestore
     * - שומר את המשתמש גם ב-SQLite לצורך סנכרון מקומי
     */
    public void registerUser(String name, String email, String password, String type, Context context, OnAuthCompleteListener listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    User user = new User(uid, name, email, type);

                    // 🔥 שמירה ב-Firestore
                    firestore.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                // ✅ שמירה ב-SQLite
                                if (context != null) {
                                    UserDatabaseHelper localDb = new UserDatabaseHelper(context);
                                    localDb.insertUser(user);
                                    Log.d("SQLiteInsert", "נשמר ל-SQLite: " + user.name + " | " + user.email + " | " + user.type);

                                }

                                listener.onSuccess();
                            })
                            .addOnFailureListener(listener::onFailure);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("email address is already in use")) {
                        listener.onFailure(new Exception("האימייל כבר רשום במערכת"));
                    } else {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * מבצע התחברות של משתמש קיים באמצעות אימייל וסיסמה:
     * - בודק את פרטי המשתמש מול Firebase Authentication
     * - מחזיר את סוג המשתמש (type) מתוך Firestore
     */
    public void loginUser(String email, String password, OnLoginCompleteListener listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();

                        firestore.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists() && documentSnapshot.contains("type")) {
                                        String type = documentSnapshot.getString("type");
                                        listener.onSuccess(type);
                                    } else {
                                        listener.onFailure(new Exception("סוג המשתמש לא קיים במסד הנתונים"));
                                    }
                                })
                                .addOnFailureListener(listener::onFailure);
                    } else {
                        listener.onFailure(new Exception("המשתמש לא זוהה"));
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * מחזיר את UID של המשתמש הנוכחי המחובר
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }
}
