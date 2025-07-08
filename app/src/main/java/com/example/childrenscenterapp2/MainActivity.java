package com.example.childrenscenterapp2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.ui.admin.AdminFragment;
import com.example.childrenscenterapp2.ui.coordinator.CoordinatorFragment;
import com.example.childrenscenterapp2.ui.guide.GuideFragment;
import com.example.childrenscenterapp2.ui.home.HomeFragment;
import com.example.childrenscenterapp2.ui.parent.ParentFragment;
import com.example.childrenscenterapp2.ui.child.ChildFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🌟 איפוס אוטומטי – רק לבדיקה!
        FirebaseAuth.getInstance().signOut();
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        setContentView(R.layout.activity_main);

        // חיבור toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // בדיקת התחברות ב-Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // בדיקת SharedPreferences
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String userType = prefs.getString("userType", "");

        // הדפסות לבדיקה
        Log.d(TAG, "currentUser: " + currentUser);
        Log.d(TAG, "isLoggedIn: " + isLoggedIn);
        Log.d(TAG, "userType: " + userType);

        if (savedInstanceState == null) {
            if (currentUser != null && isLoggedIn) {
                // המשתמש מחובר גם ב-Firebase וגם בזיכרון
                switch (userType) {
                    case "מנהל":
                        loadFragment(new AdminFragment());
                        break;
                    case "רכז":
                        loadFragment(new CoordinatorFragment());
                        break;
                    case "מדריך":
                        loadFragment(new GuideFragment());
                        break;
                    case "ילד":
                        loadFragment(new ChildFragment());
                        break;
                    case "הורה":
                        loadFragment(new ParentFragment());
                        break;
                    default:
                        loadFragment(new HomeFragment());
                        break;
                }
            } else {
                // המשתמש לא מחובר - טען מסך התחברות
                loadFragment(new HomeFragment());
            }
        }
    }

    /**
     * טוען Fragment חדש לתוך container.
     * @param fragment הפרגמנט שברצונך להציג
     */
    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
