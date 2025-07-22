package com.example.childrenscenterapp2;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
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
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String userType = prefs.getString("userType", "");

        if (savedInstanceState == null) {
            if (isLoggedIn) {
                Fragment destination = null;
                switch (userType) {
                    case "מנהל":
                        destination = new AdminFragment();
                        break;
                    case "רכז":
                        destination = new CoordinatorFragment();
                        break;
                    case "מדריך":
                        destination = new GuideFragment();
                        break;
                    case "הורה":
                        destination = new ParentFragment();
                        break;
                    case "ילד":
                        destination = new ChildFragment();
                        break;
                    default:
                        destination = new HomeFragment();
                        break;
                }

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
     * טוען Fragment לתוך המסך.
     */
    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}