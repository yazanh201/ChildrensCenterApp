package com.example.childrenscenterapp2.ui.coordinator;

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

public class CoordinatorActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivitiesAdapter adapter;
    private List<ActivityModel> activityList = new ArrayList<>();
    private FirebaseFirestore firestore;

    private Spinner spinnerDomain, spinnerMonth, spinnerGuide;

    private Button btnSortByParticipants, btnTop10Activities;
    private final Map<String, Integer> participantCountMap = new HashMap<>();
    private final Map<String, Double> averageScoreMap = new HashMap<>();



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
        btnSortByParticipants = view.findViewById(R.id.btnSortByParticipants);
        btnTop10Activities = view.findViewById(R.id.btnTop10Activities);

        firestore = FirebaseFirestore.getInstance();

        adapter = new ActivitiesAdapter(activityList, new ActivitiesAdapter.OnActivityClickListener() {
            @Override
            public void onEdit(ActivityModel activity) {
                EditActivityFragment editFragment = new EditActivityFragment(activity);
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDelete(ActivityModel activity) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("אישור מחיקה")
                        .setMessage("האם אתה בטוח שברצונך למחוק את הפעילות \"" + activity.getName() + "\"?")
                        .setPositiveButton("מחק", (dialog, which) -> deleteActivity(activity))
                        .setNegativeButton("בטל", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        // קריאת נתונים מה-DB
        loadActivitiesFromFirestore();

        // כפתור מיון לפי מספר משתתפים
        btnSortByParticipants.setOnClickListener(v -> {
            List<ActivityModel> sortedList = new ArrayList<>(activityList);
            sortedList.sort((a, b) -> {
                int countA = participantCountMap.getOrDefault(a.getId(), 0);
                int countB = participantCountMap.getOrDefault(b.getId(), 0);
                return Integer.compare(countB, countA); // מהגבוה לנמוך
            });
            adapter.setData(sortedList);
        });

        // כפתור Top 10 פעילויות בדירוג
        btnTop10Activities.setOnClickListener(v -> {
            List<ActivityModel> sortedList = new ArrayList<>(activityList);
            sortedList.sort((a, b) -> {
                double scoreA = averageScoreMap.getOrDefault(a.getId(), 0.0);
                double scoreB = averageScoreMap.getOrDefault(b.getId(), 0.0);
                return Double.compare(scoreB, scoreA); // מהגבוה לנמוך
            });
            List<ActivityModel> top10 = sortedList.subList(0, Math.min(10, sortedList.size()));
            adapter.setData(top10);
        });

        return view;
    }


    private void loadActivitiesFromFirestore() {
        firestore.collection("activities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activityList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ActivityModel activity = doc.toObject(ActivityModel.class);
                        activity.setId(doc.getId()); // חשוב
                        activityList.add(activity);
                    }
                    adapter.setData(activityList);
                    setupSpinners();
                    calculateStatsForActivities(); // מחשב ממוצע ודירוג
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "שגיאה בטעינת פעילויות", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteActivity(ActivityModel activity) {
        firestore.collection("activities")
                .document(activity.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "🗑️ פעילות נמחקה", Toast.LENGTH_SHORT).show();
                    loadActivitiesFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "❌ שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSpinners() {
        List<String> domains = new ArrayList<>();
        List<String> months = new ArrayList<>();
        List<String> guides = new ArrayList<>();

        domains.add("חיפוש לפי תחום");
        months.add("חיפוש לפי חודש");
        guides.add("חיפוש לפי מדריך");

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

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterActivities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinnerDomain.setOnItemSelectedListener(filterListener);
        spinnerMonth.setOnItemSelectedListener(filterListener);
        spinnerGuide.setOnItemSelectedListener(filterListener);
    }

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

    private ArrayAdapter<String> createAdapter(List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

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

                        // שמירה במפות
                        participantCountMap.put(activityId, count);
                        averageScoreMap.put(activityId, average);

                        // הצגה בעזרת מתודה קיימת באדפטר
                        adapter.updateStatsForActivity(activityId, count, average);
                    });
        }
    }

}
