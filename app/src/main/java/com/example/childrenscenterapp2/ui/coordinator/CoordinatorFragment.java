package com.example.childrenscenterapp2.ui.coordinator;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.R;

/**
 * דף הבית של הרכז – כולל כפתור להוספה וכפתור להצגת פעילויות
 */
public class CoordinatorFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // טעינת קובץ ה־XML
        View view = inflater.inflate(R.layout.fragment_coordinator, container, false);

        // כפתור להוספת פעילות
        Button btnAddActivity = view.findViewById(R.id.btnAddActivity);

        // כפתור להצגת רשימת פעילויות (הוספת כפתור חדש ב־XML)
        Button btnShowActivities = view.findViewById(R.id.btnShowActivities);

        // לחיצה על כפתור הוספת פעילות
        btnAddActivity.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new AddActivityFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // לחיצה על כפתור הצגת הפעילויות
        btnShowActivities.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new CoordinatorActivitiesFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
