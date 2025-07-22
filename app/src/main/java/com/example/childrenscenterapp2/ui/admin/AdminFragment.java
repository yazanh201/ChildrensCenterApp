package com.example.childrenscenterapp2.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.childrenscenterapp2.ActivitiesListFragment;
import com.example.childrenscenterapp2.R;

public class AdminFragment extends Fragment {

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
}
