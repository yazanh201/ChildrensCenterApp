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

/**
 * {@code ChildScheduleFragment} – פרגמנט להצגת לוח הזמנים של הילד.
 * <p>
 * תפקיד המחלקה:
 * <ul>
 *   <li>טעינת כל הפעילויות שהילד רשום אליהן ממסד הנתונים (Firestore).</li>
 *   <li>הצגת רשימת הפעילויות ב-RecyclerView באמצעות {@link ScheduleAdapter}.</li>
 *   <li>חישוב והצגת:
 *       <ul>
 *         <li>סיכום תחומים (מדע, חברה, יצירה).</li>
 *         <li>ציון ממוצע לכל פעילות וציון ממוצע כללי.</li>
 *       </ul>
 *   </li>
 *   <li>בדיקה האם הילד רשום לכל התחומים הנדרשים והצגת אזהרה אם חסר תחום.</li>
 *   <li>אפשרות למחוק פעילות מהרשימה כולל עדכון במסד הנתונים.</li>
 * </ul>
 */
public class ChildScheduleFragment extends Fragment {

    /** RecyclerView להצגת לוח הזמנים */
    private RecyclerView recyclerView;

    /** אדפטר מותאם להצגת פעילויות */
    private ScheduleAdapter adapter;

    /** טקסטים להצגת סיכום וציונים */
    private TextView tvScheduleSummary;
    private TextView tvAverageScore;

    /** גישה ל-Firestore */
    private FirebaseFirestore db;

    /** מזהה הילד המחובר */
    private String currentChildId;

    /** רשימת תחומים הנדרשים */
    private final List<String> requiredDomains = List.of("מדע", "חברה", "יצירה");

    /** תיוג לוגים */
    private static final String TAG = "ChildSchedule";

    /** מפת ציונים ממוצעים לפעילות */
    private Map<String, Double> activityScores = new HashMap<>();

    /** רשימת הפעילויות בלוח הזמנים */
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

        // אתחול אדפטר עם Listener למחיקת פעילות
        adapter = new ScheduleAdapter(new ArrayList<>(), activity -> {
            deleteRegistrationForActivity(activity);
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentChildId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Current child UID: " + currentChildId);

        // טעינת לוח הזמנים של הילד
        loadChildSchedule();

        return view;
    }

    /**
     * טוען את לוח הזמנים של הילד מה-DB:
     * <ul>
     *   <li>שליפת כל ההרשמות של הילד.</li>
     *   <li>טעינת פרטי הפעילויות והציונים הממוצעים.</li>
     *   <li>חישוב סיכום תחומים וציונים כלליים.</li>
     * </ul>
     */
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

                                            // חישוב הציון הממוצע לפעילות
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
                                                            activityList = activities;
                                                            calculateOverallAverageScore(activities);

                                                            // בדיקה אם חסרים תחומים נדרשים
                                                            List<String> missingDomains = new ArrayList<>();
                                                            for (String required : requiredDomains) {
                                                                if (!domains.contains(required)) {
                                                                    missingDomains.add(required);
                                                                }
                                                            }

                                                            if (!missingDomains.isEmpty()) {
                                                                String message = "שים לב: חסרה הרשמה לתחומים: " + String.join(", ", missingDomains);
                                                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                                                            }
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

    /**
     * עדכון הסיכום במסך לפי כמות הפעילויות ותחומים.
     *
     * @param list    רשימת הפעילויות.
     * @param domains רשימת התחומים של הפעילויות.
     */
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

    /**
     * חישוב הציון הממוצע הכללי של כל הפעילויות בלוח הזמנים.
     *
     * @param activities רשימת הפעילויות הנוכחיות.
     */
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

    /**
     * מחיקת רישום פעילות של הילד גם מאוסף המשתמש וגם מאוסף הפעילות.
     *
     * @param activity הפעילות למחיקה.
     */
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
