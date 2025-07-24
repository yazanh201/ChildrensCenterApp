package com.example.childrenscenterapp2.ui.guide;

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

public class GuideFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // טוען את העיצוב החדש של המסך הראשי של המדריך (רק כפתור)
        View view = inflater.inflate(R.layout.fragment_guide_home, container, false);

        // מוצא את הכפתור ומגדיר לו האזנה ללחיצה
        Button btnActivities = view.findViewById(R.id.btnShowActivities);
        btnActivities.setOnClickListener(v -> {
            // מעביר ל־GuideActivitiesFragment שמציג את רשימת הפעילויות
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new GuideActivitiesFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
