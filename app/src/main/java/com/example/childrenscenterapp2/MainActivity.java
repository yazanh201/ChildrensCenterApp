package com.example.childrenscenterapp2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.data.sync.ActivitySyncManager;
import com.example.childrenscenterapp2.data.sync.UserSyncManager;
import com.example.childrenscenterapp2.data.sync.RegistrationSyncManager;
import com.example.childrenscenterapp2.data.sync.UserRegistrationSyncManager;
import com.example.childrenscenterapp2.ui.admin.AdminFragment;
import com.example.childrenscenterapp2.ui.coordinator.CoordinatorFragment;
import com.example.childrenscenterapp2.ui.guide.GuideFragment;
import com.example.childrenscenterapp2.ui.home.HomeFragment;
import com.example.childrenscenterapp2.ui.parent.ParentFragment;
import com.example.childrenscenterapp2.ui.child.ChildFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivitySyncManager activitySyncManager;
    private UserSyncManager userSyncManager;
    private final List<RegistrationSyncManager> registrationSyncManagers = new ArrayList<>();
    private final List<UserRegistrationSyncManager> userRegistrationSyncManagers = new ArrayList<>();

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

        // ✅ סנכרון כל תתי הקולקציות של הרשמות עבור כל פעילות
        startRegistrationSyncForAllActivities();

        // ✅ סנכרון הרשמות המשתמש הנוכחי
        String currentUserId = getCurrentUserId();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            startUserRegistrationSync(currentUserId);
        }

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

    /**
     * התחלת האזנה לכל ההרשמות עבור כל פעילות
     */
    private void startRegistrationSyncForAllActivities() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("activities").get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String activityId = doc.getId();
                RegistrationSyncManager manager = new RegistrationSyncManager(this, activityId);
                manager.startListening();
                registrationSyncManagers.add(manager);
                Log.d("MainActivity", "✅ האזנה להרשמות עבור פעילות: " + activityId);
            }
        }).addOnFailureListener(e -> {
            Log.e("MainActivity", "❌ נכשל בשליפת פעילויות לסנכרון הרשמות: " + e.getMessage());
        });
    }

    /**
     * התחלת האזנה להרשמות משתמש ספציפי (למשתמש נוכחי)
     */
    private void startUserRegistrationSync(String userId) {
        UserRegistrationSyncManager userManager = new UserRegistrationSyncManager(this, userId);
        userManager.startListening();
        userRegistrationSyncManagers.add(userManager);
        Log.d("MainActivity", "✅ האזנה להרשמות המשתמש: " + userId);
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

        // ✅ הפסקת האזנה לכל מנהלי ההרשמות של פעילויות
        for (RegistrationSyncManager manager : registrationSyncManagers) {
            manager.stopListening();
        }

        // ✅ הפסקת האזנה לכל מנהלי ההרשמות של המשתמשים
        for (UserRegistrationSyncManager manager : userRegistrationSyncManagers) {
            manager.stopListening();
        }

        Log.d("MainActivity", "🛑 הופסקו כל ההאזנות לסנכרון");
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

    /**
     * פונקציה לדוגמה להשגת מזהה המשתמש הנוכחי
     */
    private String getCurrentUserId() {
        // כאן אתה יכול לשלוף את מזהה המשתמש לפי איך שמנהל המשתמשים אצלך
        // לדוגמה אם אתה משתמש ב-FirebaseAuth:
        // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // return user != null ? user.getUid() : null;

        // או מ-SharedPreferences, אם שמרת שם
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("userId", null);
    }
}
