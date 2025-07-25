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

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.ui.login.LoginFragment;
import com.example.childrenscenterapp2.ui.register.RegisterFragment;

/**
 * פרגמנט הבית הראשי של האפליקציה.
 * מאפשר למשתמש לבחור בין התחברות לבין הרשמה.
 */
public class HomeFragment extends Fragment {

    /**
     * יצירת ממשק המשתמש של הפרגמנט.
     * טוען את הכפתורים ומאזין לאירועים של התחברות/הרשמה.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // איתור כפתורים מה-Layout
        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnRegister = view.findViewById(R.id.btn_register);

        // מאזין לכפתור התחברות - טעינת LoginFragment
        btnLogin.setOnClickListener(v -> loadFragment(new LoginFragment()));

        // מאזין לכפתור הרשמה - טעינת RegisterFragment
        btnRegister.setOnClickListener(v -> loadFragment(new RegisterFragment()));

        return view;
    }

    /**
     * פונקציה לטעינת פרגמנט חדש לתוך המסך הראשי (fragment_container).
     *
     * @param fragment הפרגמנט שברצוננו להציג
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.fragment_container, fragment); // ודא ש־activity_main.xml מכיל FrameLayout עם id הזה
        transaction.addToBackStack(null); // מאפשר חזרה אחורה
        transaction.commit();
    }
}
