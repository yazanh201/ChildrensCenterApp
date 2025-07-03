package com.example.childrenscenterapp2.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.remote.AuthManager;
import com.example.childrenscenterapp2.ui.admin.AdminFragment;
import com.example.childrenscenterapp2.ui.coordinator.CoordinatorFragment;
import com.example.childrenscenterapp2.ui.guide.GuideFragment;
import com.example.childrenscenterapp2.ui.parent.ParentFragment;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etEmail = view.findViewById(R.id.etLoginEmail);
        etPassword = view.findViewById(R.id.etLoginPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        authManager = new AuthManager();

        btnLogin.setOnClickListener(v -> login());

        return view;
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.length() < 6) {
            Snackbar.make(requireView(), "נא להזין אימייל וסיסמה תקינים", Snackbar.LENGTH_LONG).show();
            return;
        }

        authManager.loginUser(email, password, new AuthManager.OnLoginCompleteListener() {
            @Override
            public void onSuccess(String userType) {
                Snackbar.make(requireView(), "התחברת בהצלחה כ־" + userType, Snackbar.LENGTH_SHORT).show();

                // ✅ שמירת מצב התחברות
                SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("userEmail", email)
                        .putString("userType", userType)
                        .apply();

                // טעינת הפרגמנט המתאים לפי סוג המשתמש
                Fragment destinationFragment = null;

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
                    default:
                        Snackbar.make(requireView(), "שגיאה בזיהוי תפקיד", Snackbar.LENGTH_SHORT).show();
                        return;
                }

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, destinationFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(requireView(), "שגיאה: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
