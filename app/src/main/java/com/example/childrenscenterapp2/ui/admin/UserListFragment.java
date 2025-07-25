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

    private RecyclerView recyclerView;          // רשימת משתמשים בתצוגה
    private List<User> userList = new ArrayList<>(); // רשימת האובייקטים מסוג User
    private UsersAdapter adapter;               // מתאם לרשימת המשתמשים
    private FirebaseFirestore db;               // הפניה ל-Firebase Firestore

    // בניית התצוגה של ה־Fragment בעת טעינה
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        // אתחול RecyclerView
        recyclerView = view.findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UsersAdapter(userList, user -> deleteUser(user)); // שליחת פעולה למחיקת משתמש
        recyclerView.setAdapter(adapter);

        // אתחול מסד הנתונים
        db = FirebaseFirestore.getInstance();

        // קבלת סוג המשתמש שהועבר כ־argument (לדוגמה: "guide", "parent", "child")
        String userType = getArguments() != null ? getArguments().getString("userType") : null;

        // אם התקבל סוג, טען את המשתמשים המתאימים
        if (userType != null) {
            loadUsersFromFirestore(userType);
        }

        return view;
    }

    // טעינת משתמשים מסוג מסוים (לפי שדה type במסד הנתונים)
    private void loadUsersFromFirestore(String type) {
        db.collection("users")
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(query -> {
                    userList.clear(); // ניקוי הרשימה הקיימת
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        User user = doc.toObject(User.class); // המרה לאובייקט מסוג User
                        userList.add(user);
                    }
                    adapter.notifyDataSetChanged(); // רענון התצוגה לאחר הטעינה
                })
                .addOnFailureListener(e -> {
                    // ניתן להוסיף כאן Toast או Log במקרה של שגיאה
                });
    }

    // מחיקת משתמש מהמערכת (שלב 1: Firestore)
    private void deleteUser(User user) {
        // 1. מחיקה מה-Firestore לפי UID
        db.collection("users").document(user.getUid())
                .delete()
                .addOnSuccessListener(unused -> {
                    // הסרה מה-Adapter ורענון התצוגה
                    userList.remove(user);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // TODO: ניתן להציג הודעת שגיאה או לוג
                });

        // 2. מחיקה מה-Authentication (Firebase Auth) — נדרש Cloud Function ⚠️
    }

}
