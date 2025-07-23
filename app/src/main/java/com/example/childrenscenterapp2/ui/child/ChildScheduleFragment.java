package com.example.childrenscenterapp2.ui.child;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChildScheduleFragment extends Fragment {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private TextView tvScheduleSummary;

    private FirebaseFirestore db;
    private String currentChildId;

    private static final String TAG = "ChildSchedule";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_child_schedule, container, false);

        recyclerView = view.findViewById(R.id.recyclerSchedule);
        tvScheduleSummary = view.findViewById(R.id.tvScheduleSummary);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapter(new ArrayList<>(), activity -> {
            deleteRegistrationForActivity(activity);
        });

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentChildId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Current child UID: " + currentChildId);

        loadChildSchedule();

        return view;
    }

    private void loadChildSchedule() {
        Log.d(TAG, "Loading schedule for child: " + currentChildId);

        db.collection("users")
                .document(currentChildId)
                .collection("registrations")
                .get()
                .addOnSuccessListener(regSnapshot -> {
                    Log.d(TAG, "Registrations fetched. Count: " + regSnapshot.size());
                    if (regSnapshot.isEmpty()) {
                        Log.d(TAG, "No registrations found for child.");
                        Toast.makeText(getContext(), "לא קיימות פעילויות בלוח הזמנים שלך", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<ActivityModel> activities = new ArrayList<>();
                    List<String> domains = new ArrayList<>();

                    for (DocumentSnapshot regDoc : regSnapshot.getDocuments()) {
                        String activityId = regDoc.getString("activityId");
                        if (activityId == null) continue;

                        db.collection("activities")
                                .document(activityId)
                                .get()
                                .addOnSuccessListener(activityDoc -> {
                                    if (activityDoc.exists()) {
                                        ActivityModel activity = activityDoc.toObject(ActivityModel.class);
                                        if (activity != null) {
                                            Log.d(TAG, "Loaded activity: " + activity.getName());
                                            activities.add(activity);
                                            domains.add(activity.getDomain());

                                            adapter.updateData(activities);
                                            updateSummary(activities, domains);
                                        }
                                    } else {
                                        Log.d(TAG, "Activity not found: " + activityId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to load activity: " + activityId, e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load registrations", e);
                    Toast.makeText(getContext(), "שגיאה בטעינת לוח הזמנים", Toast.LENGTH_SHORT).show();
                });
    }




    private void updateSummary(List<ActivityModel> list, List<String> domains) {
        int total = list.size();
        int countScience = 0, countSocial = 0, countArt = 0;

        for (String domain : domains) {
            switch (domain) {
                case "מדע":
                    countScience++;
                    break;
                case "חברה":
                    countSocial++;
                    break;
                case "יצירה":
                    countArt++;
                    break;
            }
        }

        String summary = "סה\"כ פעילויות: " + total +
                "\nמדע: " + countScience +
                ", חברה: " + countSocial +
                ", יצירה: " + countArt;

        Log.d(TAG, "Summary: " + summary);
        tvScheduleSummary.setText(summary);
    }


    private void deleteRegistrationForActivity(ActivityModel activity) {
        // 1. מחיקת הפעילות אצל הילד
        db.collection("users")
                .document(currentChildId)
                .collection("registrations")
                .whereEqualTo("activityId", activity.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

                    // 2. מחיקת הילד מתוך הפעילות עצמה
                    db.collection("activities")
                            .document(activity.getId())
                            .collection("registrations")
                            .whereEqualTo("childId", currentChildId)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                    doc.getReference().delete();
                                }

                                Toast.makeText(getContext(), "הפעילות נמחקה בהצלחה", Toast.LENGTH_SHORT).show();
                                loadChildSchedule(); // רענון הרשימה

                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "שגיאה במחיקת הילד מהפעילות", e);
                                Toast.makeText(getContext(), "שגיאה במחיקת הילד מהפעילות", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "שגיאה במחיקת הפעילות אצל הילד", e);
                    Toast.makeText(getContext(), "שגיאה במחיקת פעילות", Toast.LENGTH_SHORT).show();
                });
    }


}
