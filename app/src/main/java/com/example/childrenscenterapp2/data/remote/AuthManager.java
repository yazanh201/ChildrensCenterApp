package com.example.childrenscenterapp2.data.remote;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.childrenscenterapp2.data.local.UserDatabaseHelper;
import com.example.childrenscenterapp2.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public interface OnAuthCompleteListener {
        void onSuccess();
        void onFailure(@NonNull Exception e);
    }

    public interface OnLoginCompleteListener {
        void onSuccess(String userType);
        void onFailure(@NonNull Exception e);
    }

    public void registerUser(String name, String email, String password, String type,
                             String specialization, String idNumber, Context context,
                             OnAuthCompleteListener listener) {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // יצירת אובייקט משתמש לפי סוג
                    User user;
                    if (type.equals("מדריך")) {
                        user = new User(uid, name, email, type, specialization, "");
                    } else if (type.equals("הורה") || type.equals("ילד")) {
                        user = new User(uid, name, email, type, "", idNumber);
                    } else {
                        user = new User(uid, name, email, type);
                    }

                    // יצירת Map לשמירה ב-Firestore
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", user.getUid());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("type", user.getType());
                    userMap.put("specialization", user.getSpecialization() != null ? user.getSpecialization() : "");
                    userMap.put("idNumber", user.getIdNumber() != null ? user.getIdNumber() : "");

                    // 🔥 שמירה ב-Firestore
                    firestore.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener(unused -> {
                                // ✅ שמירה גם ב-SQLite
                                if (context != null) {
                                    UserDatabaseHelper localDb = new UserDatabaseHelper(context);
                                    localDb.insertUser(user);
                                    Log.d("SQLiteInsert", "נשמר ל-SQLite: " + user.getName() + " | " + user.getEmail() + " | " + user.getType());
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

    public void loginUser(String email, String password, Context context, OnLoginCompleteListener listener) {
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

                                        // ✅ שמור את סוג המשתמש ב־SharedPreferences
                                        if (context != null) {
                                            context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                                    .edit()
                                                    .putString("userType", type)
                                                    .apply();
                                        }

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


    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }
}
