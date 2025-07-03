package com.example.childrenscenterapp2;

import android.content.SharedPreferences;
import android.os.Bundle;

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


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ חיבור toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // שליפת מצב התחברות מה־SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String userType = prefs.getString("userType", "");

        // טען את הפרגמנט המתאים
        if (savedInstanceState == null) {
            if (isLoggedIn) {
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

                    case "ילד": // ✅ תפקיד חדש
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
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
