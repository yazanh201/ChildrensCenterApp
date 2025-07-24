package com.example.childrenscenterapp2.ui.admin;

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

public class AdminFragment extends Fragment {

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

        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        // כפתור פעילויות
        Button btnActivities = view.findViewById(R.id.btnActivities);
        btnActivities.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ActivitiesListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // כפתור משתמשים
        Button btnUsers = view.findViewById(R.id.btnUsers);
        btnUsers.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UserTypesFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
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
