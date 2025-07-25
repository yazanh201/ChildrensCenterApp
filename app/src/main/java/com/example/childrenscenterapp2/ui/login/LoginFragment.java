package com.example.childrenscenterapp2.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.remote.AuthManager;
import com.example.childrenscenterapp2.ui.admin.AdminFragment;
import com.example.childrenscenterapp2.ui.child.ChildFragment;
import com.example.childrenscenterapp2.ui.coordinator.CoordinatorFragment;
import com.example.childrenscenterapp2.ui.guide.GuideFragment;
import com.example.childrenscenterapp2.ui.parent.ParentFragment;
import com.google.android.material.snackbar.Snackbar;

/**
 * פרגמנט התחברות למשתמשים.
 * מאפשר התחברות לפי אימייל וסיסמה ומעביר את המשתמש לפרגמנט המתאים לפי תפקידו.
 */
public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private AuthManager authManager;

    /**
     * יצירת ממשק המשתמש של הפרגמנט והגדרת רכיבים
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // קישור רכיבי ממשק
        etEmail = view.findViewById(R.id.etLoginEmail);
        etPassword = view.findViewById(R.id.etLoginPassword);
        btnLogin = view.findViewById(R.id.btnLogin);

        authManager = new AuthManager();

        // מאזין לכפתור התחברות
        btnLogin.setOnClickListener(v -> login());

        return view;
    }

    /**
     * פונקציית התחברות: בודקת תקינות קלט, שולחת את הנתונים ל-AuthManager
     * ומטפלת בתוצאה בהתאם לסוג המשתמש.
     */
    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // בדיקה בסיסית של קלט
        if (email.isEmpty() || password.length() < 6) {
            Snackbar.make(requireView(), "נא להזין אימייל וסיסמה תקינים", Snackbar.LENGTH_LONG).show();
            return;
        }

        // קריאה למחלקת ניהול האימותים
        authManager.loginUser(email, password, requireContext(), new AuthManager.OnLoginCompleteListener() {
            @Override
            public void onSuccess(String userType) {
                Snackbar.make(requireView(), "התחברת בהצלחה כ־" + userType, Snackbar.LENGTH_SHORT).show();

                // טעינת הפרגמנט המתאים לפי סוג המשתמש
                Fragment destinationFragment;

                switch (userType) {
                    case "מנהל":
                        destinationFragment = new AdminFragment();
                        break;
                    case "רכז":
                        destinationFragment = new CoordinatorFragment();
                        break;
                    case "מדריך":
                        destinationFragment = new GuideFragment();
                        break;
                    case "הורה":
                        destinationFragment = new ParentFragment();
                        break;
                    case "ילד":
                        destinationFragment = new ChildFragment();
                        break;
                    default:
                        Snackbar.make(requireView(), "שגיאה בזיהוי תפקיד", Snackbar.LENGTH_SHORT).show();
                        return;
                }

                // טעינת הפרגמנט החדש
                FragmentTransaction transaction = requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction();

                transaction.replace(R.id.fragment_container, destinationFragment);
                transaction.addToBackStack(null); // שמירה לערימה לאפשר חזרה
                transaction.commit();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(requireView(), "שגיאה: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
