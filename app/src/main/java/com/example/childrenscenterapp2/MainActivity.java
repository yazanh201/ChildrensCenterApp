package com.example.childrenscenterapp2;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.data.sync.ActivitySyncManager;
import com.example.childrenscenterapp2.data.sync.UserSyncManager;
import com.example.childrenscenterapp2.ui.admin.AdminFragment;
import com.example.childrenscenterapp2.ui.coordinator.CoordinatorFragment;
import com.example.childrenscenterapp2.ui.guide.GuideFragment;
import com.example.childrenscenterapp2.ui.home.HomeFragment;
import com.example.childrenscenterapp2.ui.parent.ParentFragment;
import com.example.childrenscenterapp2.ui.child.ChildFragment;

public class MainActivity extends AppCompatActivity {

    private ActivitySyncManager activitySyncManager;
    private UserSyncManager userSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ חיבור toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✅ סנכרון Firebase → SQLite
        activitySyncManager = new ActivitySyncManager(this);
        activitySyncManager.startListening();

        userSyncManager = new UserSyncManager(this);
        userSyncManager.startListening();

        // ✅ טעינת התפקיד השמור
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", true);
        String userType = prefs.getString("userType", "");

        if (savedInstanceState == null) {
            if (isLoggedIn) {
                Fragment destination = getFragmentForUserType(userType);
                clearBackStack(); // ✅ מנקה כל מסך קודם
                loadFragment(destination);
            } else {
                loadFragment(new HomeFragment());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // ✅ הפסקת האזנה ל-Firebase
        if (activitySyncManager != null) {
            activitySyncManager.stopListening();
        }
        if (userSyncManager != null) {
            userSyncManager.stopListening();
        }
    }

    /**
     * טוען Fragment לתוך המסך הראשי (ללא backstack)
     */
    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit(); // ❌ לא מוסיפים ל־BackStack
    }

    /**
     * מנקה את כל ה־BackStack
     */
    public void clearBackStack() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    /**
     * מחזיר את ה־Fragment המתאים לפי סוג המשתמש
     */
    private Fragment getFragmentForUserType(String userType) {
        switch (userType) {
            case "מנהל":
                return new AdminFragment();
            case "רכז":
                return new CoordinatorFragment();
            case "מדריך":
                return new GuideFragment();
            case "הורה":
                return new ParentFragment();
            case "ילד":
                return new ChildFragment();
            default:
                return new HomeFragment();
        }
    }
}
