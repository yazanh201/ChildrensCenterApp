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

/**
 * Fragment שמציג כפתורים לכל סוגי המשתמשים:
 * רכז, מדריך, ילד, הורה.
 * לחיצה על כל כפתור פותחת את רשימת המשתמשים המתאימים לסוג שנבחר.
 */
public class UserTypesFragment extends Fragment {

    // בנאי ריק נדרש עבור Fragment
    public UserTypesFragment() {
        // Required empty public constructor
    }

    /**
     * יצירת התצוגה של ה־Fragment מתוך הקובץ fragment_user_types.xml
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_types, container, false);
    }

    /**
     * לאחר יצירת ה־View, מאתחל את הכפתורים ומגדיר לחיצות עליהם
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // כפתורים לכל סוגי המשתמשים
        Button btnCoordinator = view.findViewById(R.id.btnCoordinator);
        Button btnGuide = view.findViewById(R.id.btnGuide);
        Button btnChild = view.findViewById(R.id.btnChild);
        Button btnParent = view.findViewById(R.id.btnParent);

        // כל כפתור מוביל לרשימת משתמשים לפי הסוג המתאים
        btnCoordinator.setOnClickListener(v -> openUserListFragment("רכז"));
        btnGuide.setOnClickListener(v -> openUserListFragment("מדריך"));
        btnChild.setOnClickListener(v -> openUserListFragment("ילד"));
        btnParent.setOnClickListener(v -> openUserListFragment("הורה"));
    }

    /**
     * טעינת Fragment של רשימת משתמשים עם הסוג שנבחר כ־argument
     *
     * @param userType סוג המשתמש שנבחר (רכז, מדריך, ילד, הורה)
     */
    private void openUserListFragment(String userType) {
        // העברת הסוג כ־argument ל־UserListFragment
        Bundle args = new Bundle();
        args.putString("userType", userType);

        Fragment userListFragment = new UserListFragment();
        userListFragment.setArguments(args);

        // טעינת ה־Fragment החדש עם יכולת חזרה אחורה
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, userListFragment)
                .addToBackStack(null)
                .commit();
    }
}
