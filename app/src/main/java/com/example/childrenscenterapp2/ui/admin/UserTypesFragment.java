package com.example.childrenscenterapp2.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.childrenscenterapp2.ui.admin.UserListFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.childrenscenterapp2.R;

public class UserTypesFragment extends Fragment {

    public UserTypesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_types, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnCoordinator = view.findViewById(R.id.btnCoordinator);
        Button btnGuide = view.findViewById(R.id.btnGuide);
        Button btnChild = view.findViewById(R.id.btnChild);
        Button btnParent = view
                .findViewById(R.id.btnParent);

        btnCoordinator.setOnClickListener(v -> openUserListFragment("רכז"));
        btnGuide.setOnClickListener(v -> openUserListFragment("מדריך"));
        btnChild.setOnClickListener(v -> openUserListFragment("ילד"));
        btnParent.setOnClickListener(v -> openUserListFragment("הורה"));

    }

    private void openUserListFragment(String userType) {
        Bundle args = new Bundle();
        args.putString("userType", userType);

        Fragment userListFragment = new UserListFragment();
        userListFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, userListFragment)
                .addToBackStack(null)
                .commit();
    }
}
