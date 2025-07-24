package com.example.childrenscenterapp2.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewAllFeedbacksFragment extends Fragment {

    // משתנים עיקריים
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private FeedbackAdapter adapter;
    private List<Map<String, Object>> feedbackList = new ArrayList<>();
    private String activityId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // ניפוח תצוגת הפרגמנט מה-XML
        View view = inflater.inflate(R.layout.fragment_view_all_feedbacks, container, false);

        // אתחול RecyclerView להצגת ביקורות
        recyclerView = view.findViewById(R.id.recyclerViewFeedbacks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FeedbackAdapter(feedbackList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // קבלת מזהה פעילות מה-Bundle (אם קיים)
        if (getArguments() != null) {
            activityId = getArguments().getString("activityId");
            loadFeedbacks(); // טעינת הביקורות מה-DB
        }

        return view;
    }

    // פונקציה לטעינת הביקורות מהתת-collection registrations
    private void loadFeedbacks() {
        db.collection("activities")
                .document(activityId)
                .collection("registrations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    feedbackList.clear(); // ניקוי רשימה קודמת
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> feedbackData = doc.getData();
                        if (feedbackData != null) {
                            feedbackList.add(feedbackData); // הוספת הביקורת לרשימה
                        }
                    }
                    adapter.notifyDataSetChanged(); // עדכון התצוגה

                    // הצגת טקסט אם הרשימה ריקה
                    View emptyView = getView().findViewById(R.id.tvEmptyList);
                    if (feedbackList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת ביקורות", Toast.LENGTH_SHORT).show();
                });
    }
}
