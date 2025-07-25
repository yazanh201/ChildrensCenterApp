package com.example.childrenscenterapp2.ui.guide;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

/**
 * Fragment שמציג את כל הפעילויות עבור מדריך
 * כולל סינון לפי תחום, חודש ומדריך, וחישוב סטטיסטיקות כמו כמות משתתפים ודירוג ממוצע
 */
public class AllActivitiesForGuideFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivitiesForGuideAdapter adapter;
    private List<ActivityModel> activityList = new ArrayList<>();
    private FirebaseFirestore firestore;

    private Spinner spinnerDomain, spinnerMonth, spinnerGuide;

    // מפות לשמירת כמות משתתפים ודירוג ממוצע לכל פעילות לפי ID
    private final Map<String, Integer> participantCountMap = new HashMap<>();
    private final Map<String, Double> averageScoreMap = new HashMap<>();

    /**
     * בניית התצוגה הראשית של המסך
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coordinator_activities, container, false);

        recyclerView = view.findViewById(R.id.recyclerActivities);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        spinnerDomain = view.findViewById(R.id.spinnerDomain);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerGuide = view.findViewById(R.id.spinnerGuide);

        // הסתרת כפתורים לא רלוונטיים עבור מדריך
        Button btnSortByParticipants = view.findViewById(R.id.btnSortByParticipants);
        Button btnTop10Activities = view.findViewById(R.id.btnTop10Activities);
        btnSortByParticipants.setVisibility(View.GONE);
        btnTop10Activities.setVisibility(View.GONE);

        firestore = FirebaseFirestore.getInstance();

        adapter = new ActivitiesForGuideAdapter(activityList);
        recyclerView.setAdapter(adapter);

        loadActivitiesFromFirestore();

        return view;
    }

    /**
     * שליפת כל הפעילויות מ-Firestore והצגתן ברשימה
     */
    private void loadActivitiesFromFirestore() {
        firestore.collection("activities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activityList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ActivityModel activity = doc.toObject(ActivityModel.class);
                        activity.setId(doc.getId());
                        activityList.add(activity);
                    }
                    adapter.setData(activityList);
                    setupSpinners();           // הגדרת אפשרויות הסינון
                    calculateStatsForActivities(); // חישוב סטטיסטיקות
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "שגיאה בטעינת פעילויות", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * יצירת רשימות אפשרויות עבור הספינרים (תחום, חודש, מדריך)
     */
    private void setupSpinners() {
        List<String> domains = new ArrayList<>();
        List<String> months = new ArrayList<>();
        List<String> guides = new ArrayList<>();

        // פריט ברירת מחדל בכל Spinner
        domains.add("חיפוש לפי תחום");
        months.add("חיפוש לפי חודש");
        guides.add("חיפוש לפי מדריך");

        // הוספת ערכים ייחודיים מתוך רשימת הפעילויות
        for (ActivityModel activity : activityList) {
            if (activity.getDomain() != null && !domains.contains(activity.getDomain()))
                domains.add(activity.getDomain());

            if (activity.getMonth() != null && !months.contains(activity.getMonth()))
                months.add(activity.getMonth());

            if (activity.getGuideName() != null && !guides.contains(activity.getGuideName()))
                guides.add(activity.getGuideName());
        }

        spinnerDomain.setAdapter(createAdapter(domains));
        spinnerMonth.setAdapter(createAdapter(months));
        spinnerGuide.setAdapter(createAdapter(guides));

        // מאזין לשינוי בחירה בכל אחד מהספינרים → מבצע סינון
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterActivities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerDomain.setOnItemSelectedListener(filterListener);
        spinnerMonth.setOnItemSelectedListener(filterListener);
        spinnerGuide.setOnItemSelectedListener(filterListener);
    }

    /**
     * סינון רשימת הפעילויות לפי התחום, החודש והמדריך שנבחרו
     */
    private void filterActivities() {
        String selectedDomain = spinnerDomain.getSelectedItem().toString();
        String selectedMonth = spinnerMonth.getSelectedItem().toString();
        String selectedGuide = spinnerGuide.getSelectedItem().toString();

        List<ActivityModel> filteredList = new ArrayList<>();
        for (ActivityModel activity : activityList) {
            boolean matchDomain = selectedDomain.equals("חיפוש לפי תחום") || selectedDomain.equals(activity.getDomain());
            boolean matchMonth = selectedMonth.equals("חיפוש לפי חודש") || selectedMonth.equals(activity.getMonth());
            boolean matchGuide = selectedGuide.equals("חיפוש לפי מדריך") || selectedGuide.equals(activity.getGuideName());

            if (matchDomain && matchMonth && matchGuide) {
                filteredList.add(activity);
            }
        }

        adapter.setData(filteredList);
    }

    /**
     * יצירת ArrayAdapter עבור Spinner
     */
    private ArrayAdapter<String> createAdapter(List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    /**
     * חישוב מספר המשתתפים ודירוג ממוצע עבור כל פעילות (מתוך תת-collection registrations)
     */
    private void calculateStatsForActivities() {
        for (ActivityModel activity : activityList) {
            String activityId = activity.getId();

            firestore.collection("activities")
                    .document(activityId)
                    .collection("registrations")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        int count = snapshot.size();
                        double totalScore = 0.0;
                        int scoredCount = 0;

                        for (QueryDocumentSnapshot doc : snapshot) {
                            if (doc.contains("feedbackScore")) {
                                try {
                                    double score = doc.getDouble("feedbackScore");
                                    totalScore += score;
                                    scoredCount++;
                                } catch (Exception ignored) {}
                            }
                        }

                        double average = scoredCount > 0 ? totalScore / scoredCount : 0.0;

                        participantCountMap.put(activityId, count);
                        averageScoreMap.put(activityId, average);

                        // עדכון הנתונים באדפטר (כמות משתתפים ודירוג)
                        adapter.updateStatsForActivity(activityId, count, average);
                    });
        }
    }
}
