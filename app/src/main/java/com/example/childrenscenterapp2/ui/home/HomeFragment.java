package com.example.childrenscenterapp2.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.MainActivity;
import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.ui.login.LoginFragment;
import com.example.childrenscenterapp2.ui.register.RegisterFragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnRegister = view.findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(v -> {
            // טעינת LoginFragment במקום ניווט
            loadFragment(new LoginFragment());
        });

        btnRegister.setOnClickListener(v -> {
            // טעינת RegisterFragment במקום ניווט
            loadFragment(new RegisterFragment());
        });

        return view;
    }

    // פונקציה לטעינת פרגמנט
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.fragment_container, fragment); // ודא ש־activity_main.xml מכיל FrameLayout עם id הזה
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
