package com.example.childrenscenterapp2.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.User;
import com.example.childrenscenterapp2.ui.admin.UsersAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<User> userList = new ArrayList<>();
    private UsersAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UsersAdapter(userList, user -> deleteUser(user));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // קבלת סוג המשתמש מה־arguments
        String userType = getArguments() != null ? getArguments().getString("userType") : null;

        if (userType != null) {
            loadUsersFromFirestore(userType);
        }

        return view;
    }

    private void loadUsersFromFirestore(String type) {
        db.collection("users")
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(query -> {
                    userList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        User user = doc.toObject(User.class);
                        userList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // כאן אפשר להוסיף Toast או לוג לשגיאה
                });
    }


    private void deleteUser(User user) {
        // 1. מחיקה מה-Firestore
        db.collection("users").document(user.getUid())
                .delete()
                .addOnSuccessListener(unused -> {
                    // הסר מהרשימה ועדכן את התצוגה
                    userList.remove(user);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // TODO: הצג Toast או לוג על כישלון
                });

        // 2. מחיקה מה-Authentication דורשת שימוש ב-Cloud Function ⚠️
    }

}
