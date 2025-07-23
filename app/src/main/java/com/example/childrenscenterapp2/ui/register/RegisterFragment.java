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

public class RegisterFragment extends Fragment {

    private EditText etName, etEmail, etPassword, etIdNumber;
    private Spinner spinnerType, spinnerSpecialization;
    private Button btnRegister;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // אתחול רכיבי UI
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etIdNumber = view.findViewById(R.id.etIdNumber);
        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerSpecialization = view.findViewById(R.id.spinnerSpecialization);
        btnRegister = view.findViewById(R.id.btnRegister);
        authManager = new AuthManager();

        // Spinner סוגי משתמשים
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.user_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        // Spinner תחום התמחות
        ArrayAdapter<CharSequence> specializationAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.specializations, android.R.layout.simple_spinner_item);
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialization.setAdapter(specializationAdapter);
        spinnerSpecialization.setVisibility(View.GONE);
        etIdNumber.setVisibility(View.GONE); // מוסתר כברירת מחדל

        // שינוי תצוגה לפי סוג משתמש
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                if (selectedType.equals("מדריך")) {
                    spinnerSpecialization.setVisibility(View.VISIBLE);
                    etIdNumber.setVisibility(View.GONE);
                } else if (selectedType.equals("הורה") || selectedType.equals("ילד")) {
                    etIdNumber.setVisibility(View.VISIBLE);
                    spinnerSpecialization.setVisibility(View.GONE);
                } else {
                    spinnerSpecialization.setVisibility(View.GONE);
                    etIdNumber.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // האזנה לכפתור
        btnRegister.setOnClickListener(v -> register());

        return view;
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String specialization = "";
        String idNumber = etIdNumber.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.length() < 6) {
            Snackbar.make(requireView(), "יש למלא את כל השדות כראוי", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (type.equals("מדריך")) {
            specialization = spinnerSpecialization.getSelectedItem().toString();
            if (specialization.isEmpty()) {
                Snackbar.make(requireView(), "בחר תחום התמחות", Snackbar.LENGTH_LONG).show();
                return;
            }
        }

        if ((type.equals("הורה") || type.equals("ילד")) && idNumber.isEmpty()) {
            Snackbar.make(requireView(), "יש להזין תעודת זהות של הורה", Snackbar.LENGTH_LONG).show();
            return;
        }

        authManager.registerUser(
                name,
                email,
                password,
                type,
                specialization,
                idNumber,
                requireContext(),
                new AuthManager.OnAuthCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Snackbar.make(requireView(), "נרשמת בהצלחה!", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(requireView(), "שגיאה: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}
