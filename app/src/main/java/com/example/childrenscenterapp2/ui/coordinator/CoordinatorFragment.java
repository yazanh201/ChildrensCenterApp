package com.example.childrenscenterapp2.ui.coordinator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.ui.home.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;

/**
 * {@code CoordinatorFragment} – דף הבית של הרכז.
 * <p>
 * מטרות המחלקה:
 * <ul>
 *   <li>הצגת תפריט ראשי לרכז עם כפתורים לניהול פעילויות.</li>
 *   <li>מעבר למסכים שונים: הוספת פעילות, צפייה ברשימת פעילויות, וצפייה ברשימת מדריכים.</li>
 *   <li>כולל תפריט נפתח (⋮) עם אפשרות להתנתק מהמערכת.</li>
 * </ul>
 */
public class CoordinatorFragment extends Fragment {

    /**
     * אתחול התפריט המאפשר הצגת אפשרות התנתקות (⋮).
     *
     * @param savedInstanceState מצב שמור של הפרגמנט (אם קיים).
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // מאפשר להוסיף תפריט לאקשן בר
    }

    /**
     * יצירת ממשק המשתמש של דף הרכז.
     *
     * @param inflater     מנפח ה-XML.
     * @param container    הקונטיינר של הפרגמנט.
     * @param savedInstanceState מצב שמור של הפרגמנט.
     * @return View הראשי של הפרגמנט.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // טעינת קובץ ה־XML של דף הרכז
        View view = inflater.inflate(R.layout.fragment_coordinator, container, false);

        // אתחול כפתורי פעולה
        Button btnAddActivity = view.findViewById(R.id.btnAddActivity);
        Button btnShowActivities = view.findViewById(R.id.btnShowActivities);
        Button btnShowGuides = view.findViewById(R.id.btnShowGuides);

        // לחיצה על "הוסף פעילות" – מעבר למסך הוספת פעילות
        btnAddActivity.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new AddActivityFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // לחיצה על "הצג את כל הפעילויות" – מעבר למסך הצגת רשימת הפעילויות
        btnShowActivities.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new CoordinatorActivitiesFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // לחיצה על "הצג מדריכים" – מעבר לרשימת המדריכים
        btnShowGuides.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new GuideListFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    /**
     * יצירת תפריט (⋮) בחלק העליון של המסך, הכולל את אופציית ההתנתקות.
     *
     * @param menu     אובייקט התפריט להצגה.
     * @param inflater מנפח התפריט.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logout, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * טיפול בלחיצה על פריטי התפריט (למשל התנתקות).
     *
     * @param item פריט התפריט שנלחץ.
     * @return true אם האירוע טופל בהצלחה.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // התנתקות מהמשתמש הנוכחי ב-Firebase
            FirebaseAuth.getInstance().signOut();

            // ניקוי מידע מה-SharedPreferences כדי למחוק נתוני התחברות
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // מעבר חזרה לדף הבית / מסך הכניסה
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HomeFragment());
            transaction.commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
