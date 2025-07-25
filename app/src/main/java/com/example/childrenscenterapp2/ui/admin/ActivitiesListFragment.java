package com.example.childrenscenterapp2.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivitiesListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivitiesSimpleAdapter adapter;
    private final List<Map<String, Object>> activityList = new ArrayList<>();
    private SwitchCompat switchRegistration;

    // בנאי ריק (נדרש ל־Fragment)
    public ActivitiesListFragment() {}

    // יצירת View של ה־Fragment והגדרת הרכיבים
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities_list_admin, container, false);

        // הגדרת RecyclerView להצגת רשימת הפעילויות
        recyclerView = view.findViewById(R.id.recyclerActivities);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ActivitiesSimpleAdapter(activityList, requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(adapter);

        // הגדרת מתג לפתיחה/סגירה כוללת של הרשמה
        switchRegistration = view.findViewById(R.id.switchRegistration);

        loadRegistrationStatus();      // ✅ שלב 1: טען מצב הרשמה כולל
        fetchActivitiesFromFirebase(); // ✅ שלב 2: טען את הפעילויות

        return view;
    }

    // שליפה של כל הפעילויות מ-Firebase והצגתן ברשימה
    private void fetchActivitiesFromFirebase() {
        FirebaseFirestore.getInstance().collection("activities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activityList.clear(); // ניקוי הרשימה לפני עדכון חדש
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> data = doc.getData();
                        data.put("id", doc.getId()); // הוספת מזהה של המסמך
                        activityList.add(data);
                    }
                    adapter.notifyDataSetChanged(); // רענון התצוגה
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "שגיאה בטעינת פעילויות", Toast.LENGTH_SHORT).show());
    }

    // טעינת סטטוס ההרשמה הכולל והצגת טקסט מתאים במתג
    private void loadRegistrationStatus() {
        FirebaseFirestore.getInstance().collection("activities")
                .get()
                .addOnSuccessListener(query -> {
                    boolean allOpen = true; // הנחה שכל הפעילויות פתוחות
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Boolean open = doc.getBoolean("isRegistrationOpen");
                        if (open == null || !open) {
                            allOpen = false;
                            break;
                        }
                    }

                    // עדכון מצב המתג בהתאם
                    switchRegistration.setChecked(allOpen);
                    switchRegistration.setText(allOpen ? "ההרשמה פתוחה" : "ההרשמה סגורה");

                    // מאזין לשינוי מצב במתג — עדכון הרשמה בכל הפעילויות
                    switchRegistration.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        updateAllActivitiesRegistration(isChecked);
                    });

                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "שגיאה בבדיקת מצב הרשמה", Toast.LENGTH_SHORT).show();
                });
    }

    // עדכון השדה isRegistrationOpen עבור כל הפעילויות לפי מצב המתג
    private void updateAllActivitiesRegistration(boolean open) {
        FirebaseFirestore.getInstance().collection("activities")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().update("isRegistrationOpen", open);
                    }
                    // עדכון הטקסט במתג בהתאם למצב
                    switchRegistration.setText(open ? "ההרשמה פתוחה" : "ההרשמה סגורה");
                    Toast.makeText(requireContext(),
                            open ? "✅ ההרשמה נפתחה לכל הפעילויות" : "❌ ההרשמה נסגרה בכל הפעילויות",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "שגיאה בעדכון הרשמה", Toast.LENGTH_SHORT).show();
                });
    }
}
