package com.example.childrenscenterapp2.ui.register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.remote.AuthManager;
import com.google.android.material.snackbar.Snackbar;

/**
 * Fragment לטופס רישום משתמש חדש
 * מבצע רישום משתמש ל-Firebase Auth, שמירה ב-Firestore וב- SQLite
 */
public class RegisterFragment extends Fragment {

    private EditText etName, etEmail, etPassword;
    private Spinner spinnerType;
    private Button btnRegister;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // איתחול רכיבי UI
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        spinnerType = view.findViewById(R.id.spinnerType);
        btnRegister = view.findViewById(R.id.btnRegister);
        authManager = new AuthManager();

        // הגדרת Spinner עם סוגי משתמשים
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.user_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        // מאזין לכפתור רישום
        btnRegister.setOnClickListener(v -> register());

        return view;
    }

    /**
     * מבצע רישום משתמש חדש לפי פרטי הטופס
     * רושם ל-Firebase Auth, שומר את המשתמש ב-Firestore וב-SQLite
     */
    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();

        // בדיקות תקינות קלט
        if (name.isEmpty() || email.isEmpty() || password.length() < 6) {
            Snackbar.make(requireView(), "יש למלא את כל השדות כראוי", Snackbar.LENGTH_LONG).show();
            return;
        }

        // קריאה למחלקת AuthManager לרישום + שמירה אוטומטית
        authManager.registerUser(name, email, password, type, requireContext(), new AuthManager.OnAuthCompleteListener() {
            @Override
            public void onSuccess() {
                Snackbar.make(requireView(), "נרשמת בהצלחה!", Snackbar.LENGTH_LONG).show();
                // 🧭 ניתן לנווט כאן למסך הראשי לפי סוג המשתמש
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(requireView(), "שגיאה: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
