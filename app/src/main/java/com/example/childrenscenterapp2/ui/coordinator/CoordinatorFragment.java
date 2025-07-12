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
import com.example.childrenscenterapp2.ui.coordinator.AddActivityFragment;
import com.example.childrenscenterapp2.ui.coordinator.CoordinatorActivitiesFragment;
import com.example.childrenscenterapp2.ui.home.HomeFragment;
import com.example.childrenscenterapp2.ui.login.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;

/**
 * דף הבית של הרכז – כולל כפתורים להוספת פעילות, הצגת פעילויות, ותפריט התנתקות
 */
public class CoordinatorFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // מאפשר יצירת תפריט (⋮)
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // טעינת קובץ ה־XML
        View view = inflater.inflate(R.layout.fragment_coordinator, container, false);

        // כפתורי פעולה
        Button btnAddActivity = view.findViewById(R.id.btnAddActivity);
        Button btnShowActivities = view.findViewById(R.id.btnShowActivities);

        // לחיצה על "הוסף פעילות"
        btnAddActivity.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new AddActivityFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // לחיצה על "הצג את כל הפעילויות"
        btnShowActivities.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new CoordinatorActivitiesFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btnShowGuides = view.findViewById(R.id.btnShowGuides);
        btnShowGuides.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new GuideListFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });



        return view;
    }

    // יצירת תפריט (⋮)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logout, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // טיפול בלחיצה על פריט בתפריט
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // התנתקות
            FirebaseAuth.getInstance().signOut();

            // ניקוי המידע מה־SharedPreferences
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // חזרה לדף הכניסה
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HomeFragment());
            transaction.commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
