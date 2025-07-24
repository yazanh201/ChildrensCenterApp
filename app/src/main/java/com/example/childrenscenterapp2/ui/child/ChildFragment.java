package com.example.childrenscenterapp2.ui.child;

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

public class ChildFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // מאפשר תפריט ⋮
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_child, container, false);

        // כפתור להצגת פעילויות
        Button btnShowActivities = view.findViewById(R.id.btnShowActivities);
        btnShowActivities.setOnClickListener(v -> openFragment(new ChildActivitiesFragment()));

        // כפתור לוח זמנים
        Button btnSchedule = view.findViewById(R.id.btnSchedule);
        btnSchedule.setOnClickListener(v -> openFragment(new ChildScheduleFragment()));

        return view;
    }

    // פתיחת פרגמנט חדש
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // יצירת תפריט ⋮
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logout, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // טיפול בלחיצה על Logout
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // התנתקות מ־Firebase
            FirebaseAuth.getInstance().signOut();

            // ניקוי המידע מה־SharedPreferences
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // חזרה לדף הבית
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HomeFragment());
            transaction.commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
