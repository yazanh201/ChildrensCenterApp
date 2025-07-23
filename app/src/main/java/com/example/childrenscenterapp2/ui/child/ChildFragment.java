package com.example.childrenscenterapp2.ui.child;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.R;

public class ChildFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_child, container, false);

        // כפתור להצגת פעילויות
        Button btnShowActivities = view.findViewById(R.id.btnShowActivities);
        btnShowActivities.setOnClickListener(v -> openFragment(new ChildActivitiesFragment()));

        // כפתור חדש: לוח זמנים
        Button btnSchedule = view.findViewById(R.id.btnSchedule);
        btnSchedule.setOnClickListener(v -> openFragment(new ChildScheduleFragment()));

        return view;
    }

    // פונקציה כללית לפתיחת פרגמנט
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
