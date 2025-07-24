package com.example.childrenscenterapp2.ui.guide;

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

public class GuideFragment extends Fragment {

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

        View view = inflater.inflate(R.layout.fragment_guide_home, container, false);

        // כפתור 1: הצגת הפעילויות של המדריך (ישן)
        Button btnActivities = view.findViewById(R.id.btnShowActivities);
        btnActivities.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new GuideActivitiesFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // כפתור 2: הצגת כל הפעילויות – כולל סינון
        Button btnAllActivities = view.findViewById(R.id.btnAllActivities);
        btnAllActivities.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new AllActivitiesForGuideFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    // תפריט ⋮ להתנתקות
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logout, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // טיפול בלחיצה על "התנתק"
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();

            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HomeFragment());
            transaction.commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
