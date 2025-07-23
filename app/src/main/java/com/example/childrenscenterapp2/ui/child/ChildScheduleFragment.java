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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildScheduleFragment extends Fragment {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private TextView tvScheduleSummary;
    private TextView tvAverageScore;

    private FirebaseFirestore db;
    private String currentChildId;

    private static final String TAG = "ChildSchedule";

    private Map<String, Double> activityScores = new HashMap<>();
    private List<ActivityModel> activityList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_child_schedule, container, false);

        recyclerView = view.findViewById(R.id.recyclerSchedule);
        tvScheduleSummary = view.findViewById(R.id.tvScheduleSummary);
        tvAverageScore = view.findViewById(R.id.tvAverageScore);

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
                    if (regSnapshot.isEmpty()) {
                        Log.d(TAG, "No registrations found for child.");
                        Toast.makeText(getContext(), "לא קיימות פעילויות בלוח הזמנים שלך", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<ActivityModel> activities = new ArrayList<>();
                    List<String> domains = new ArrayList<>();

                    final int[] loadedCount = {0};
                    final int totalActivities = regSnapshot.size();

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

                                            db.collection("activities")
                                                    .document(activityId)
                                                    .collection("registrations")
                                                    .get()
                                                    .addOnSuccessListener(scoreSnapshot -> {
                                                        double total = 0;
                                                        int count = 0;
                                                        for (DocumentSnapshot doc : scoreSnapshot.getDocuments()) {
                                                            Double score = doc.getDouble("feedbackScore");
                                                            if (score != null) {
                                                                total += score;
                                                                count++;
                                                            }
                                                        }

                                                        double avg = count > 0 ? total / count : 0.0;
                                                        activityScores.put(activityId, avg);
                                                        activities.add(activity);
                                                        domains.add(activity.getDomain());

                                                        adapter.updateData(activities);
                                                        adapter.updateScore(activityId, avg);
                                                        updateSummary(activities, domains);

                                                        loadedCount[0]++;
                                                        if (loadedCount[0] == totalActivities) {
                                                            // כל הפעילויות טוענו - חשב ממוצע כללי
                                                            activityList = activities;
                                                            calculateOverallAverageScore(activities);
                                                        }
                                                    });
                                        }
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

        tvScheduleSummary.setText(summary);
    }

    private void calculateOverallAverageScore(List<ActivityModel> activities) {
        double totalScore = 0;
        int scoredActivities = 0;

        for (ActivityModel activity : activities) {
            Double score = activityScores.get(activity.getId());
            if (score != null) {
                totalScore += score;
                scoredActivities++;
            }
        }

        double average = scoredActivities > 0 ? totalScore / scoredActivities : 0.0;
        String result = String.format("ציון ממוצע כללי: %.1f", average);
        tvAverageScore.setText(result);
    }

    private void deleteRegistrationForActivity(ActivityModel activity) {
        db.collection("users")
                .document(currentChildId)
                .collection("registrations")
                .whereEqualTo("activityId", activity.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

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
                                loadChildSchedule();

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
