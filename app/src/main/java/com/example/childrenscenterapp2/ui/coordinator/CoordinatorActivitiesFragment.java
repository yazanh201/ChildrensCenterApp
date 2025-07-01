package com.example.childrenscenterapp2.ui.coordinator;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment שמציג את כל הפעילויות לרכז
 */
public class CoordinatorActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivitiesAdapter adapter;
    private List<ActivityModel> activityList = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coordinator_activities, container, false);

        recyclerView = view.findViewById(R.id.recyclerActivities);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        firestore = FirebaseFirestore.getInstance();

        adapter = new ActivitiesAdapter(activityList, new ActivitiesAdapter.OnActivityClickListener() {
            @Override
            public void onEdit(ActivityModel activity) {
                Toast.makeText(requireContext(), "עריכת פעילות: " + activity.getName(), Toast.LENGTH_SHORT).show();
                // כאן תוכל לפתוח Fragment חדש לעריכה (נבנה את זה בשלב הבא)
            }

            @Override
            public void onDelete(ActivityModel activity) {
                deleteActivity(activity);
            }
        });

        recyclerView.setAdapter(adapter);

        loadActivitiesFromFirestore();

        return view;
    }

    /**
     * שליפת כל הפעילויות מה-Firestore
     */
    private void loadActivitiesFromFirestore() {
        firestore.collection("activities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activityList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ActivityModel activity = doc.toObject(ActivityModel.class);
                        activityList.add(activity);
                    }
                    adapter.setData(activityList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "שגיאה בטעינת פעילויות", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * מחיקת פעילות
     */
    private void deleteActivity(ActivityModel activity) {
        firestore.collection("activities")
                .document(activity.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "🗑️ פעילות נמחקה", Toast.LENGTH_SHORT).show();
                    loadActivitiesFromFirestore(); // רענון
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "❌ שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                });
    }
}
