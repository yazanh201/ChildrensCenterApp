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

        Button btnShowActivities = view.findViewById(R.id.btnShowActivities);
        btnShowActivities.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ChildActivitiesFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }


    private void openActivitiesFragment() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ChildActivitiesFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
