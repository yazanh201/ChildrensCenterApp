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

/**
 * {@code CoordinatorActivitiesFragment} - פרגמנט המאפשר לרכז לצפות ולנהל את כל הפעילויות.
 * <p>
 * תפקיד הפרגמנט:
 * <ul>
 *   <li>טעינת כל הפעילויות מ-Firebase Firestore והצגתן ברשימת RecyclerView.</li>
 *   <li>מיון וסינון פעילויות לפי תחום, חודש ומדריך.</li>
 *   <li>אפשרות למחוק פעילויות קיימות ולעדכן את רשימת הפעילויות בזמן אמת.</li>
 *   <li>חישוב והצגת סטטיסטיקות: מספר משתתפים ודירוג ממוצע לכל פעילות.</li>
 * </ul>
 */
public class CoordinatorActivitiesFragment extends Fragment {

    /** רכיב להצגת רשימת הפעילויות */
    private RecyclerView recyclerView;

    /** אדפטר מותאם אישית להצגת פרטי הפעילויות */
    private ActivitiesAdapter adapter;

    /** רשימת פעילויות נטענות מהמסד */
    private List<ActivityModel> activityList = new ArrayList<>();

    /** חיבור למסד הנתונים Firebase Firestore */
    private FirebaseFirestore firestore;

    /** רכיבי סינון */
    private Spinner spinnerDomain, spinnerMonth, spinnerGuide;

    /** כפתורי מיון */
    private Button btnSortByParticipants, btnTop10Activities;

    /** מפות לשמירת סטטיסטיקות לכל פעילות */
    private final Map<String, Integer> participantCountMap = new HashMap<>();
    private final Map<String, Double> averageScoreMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coordinator_activities, container, false);

        // אתחול רכיבי UI
        recyclerView = view.findViewById(R.id.recyclerActivities);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        spinnerDomain = view.findViewById(R.id.spinnerDomain);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerGuide = view.findViewById(R.id.spinnerGuide);
        btnSortByParticipants = view.findViewById(R.id.btnSortByParticipants);
        btnTop10Activities = view.findViewById(R.id.btnTop10Activities);

        firestore = FirebaseFirestore.getInstance();

        // אתחול האדפטר והגדרת פעולות עריכה ומחיקה
        adapter = new ActivitiesAdapter(activityList, new ActivitiesAdapter.OnActivityClickListener() {
            @Override
            public void onEdit(ActivityModel activity) {
                // מעבר למסך עריכת פעילות
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
                // דיאלוג אישור מחיקה
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("אישור מחיקה")
                        .setMessage("האם אתה בטוח שברצונך למחוק את הפעילות \"" + activity.getName() + "\"?")
                        .setPositiveButton("מחק", (dialog, which) -> deleteActivity(activity))
                        .setNegativeButton("בטל", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        // טעינת פעילויות מהמסד
        loadActivitiesFromFirestore();

        // מיון לפי מספר משתתפים בלחיצה על הכפתור
        btnSortByParticipants.setOnClickListener(v -> {
            List<ActivityModel> sortedList = new ArrayList<>(activityList);
            sortedList.sort((a, b) -> {
                int countA = participantCountMap.getOrDefault(a.getId(), 0);
                int countB = participantCountMap.getOrDefault(b.getId(), 0);
                return Integer.compare(countB, countA); // מהגבוה לנמוך
            });
            adapter.setData(sortedList);
        });

        // מיון והצגת Top 10 פעילויות עם הדירוג הממוצע הגבוה ביותר
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

    /**
     * טוען את רשימת הפעילויות מ-Firebase Firestore ומעדכן את ה-RecyclerView.
     */
    private void loadActivitiesFromFirestore() {
        firestore.collection("activities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activityList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ActivityModel activity = doc.toObject(ActivityModel.class);
                        activity.setId(doc.getId()); // שמירת ה-ID מהמסד
                        activityList.add(activity);
                    }
                    adapter.setData(activityList);
                    setupSpinners();            // הגדרת אפשרויות הסינון
                    calculateStatsForActivities(); // חישוב סטטיסטיקות עבור כל פעילות
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "שגיאה בטעינת פעילויות", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * מחיקת פעילות מהמסד Firebase Firestore ועדכון הרשימה.
     *
     * @param activity הפעילות למחיקה.
     */
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

    /**
     * הגדרת ה-Spinners (תחום, חודש, מדריך) לפי הנתונים הקיימים.
     * מאפשר סינון רשימת הפעילויות לפי בחירות המשתמש.
     */
    private void setupSpinners() {
        List<String> domains = new ArrayList<>();
        List<String> months = new ArrayList<>();
        List<String> guides = new ArrayList<>();

        domains.add("חיפוש לפי תחום");
        months.add("חיפוש לפי חודש");
        guides.add("חיפוש לפי מדריך");

        // איסוף ערכים ייחודיים מתוך רשימת הפעילויות
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

        // מאזינים לשינוי בחירה ב-Spinners
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
     * סינון הפעילויות בהתאם לערכים שנבחרו ב-Spinners.
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
     * יצירת {@link ArrayAdapter} עבור Spinner מסוים.
     *
     * @param items רשימת פריטים להצגה.
     * @return Adapter מותאם.
     */
    private ArrayAdapter<String> createAdapter(List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    /**
     * חישוב סטטיסטיקות עבור כל פעילות:
     * - מספר המשתתפים.
     * - הדירוג הממוצע לפי משובים.
     * מעדכן את הנתונים באדפטר.
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

                        // שמירת הנתונים במפות הסטטיסטיקות
                        participantCountMap.put(activityId, count);
                        averageScoreMap.put(activityId, average);

                        // עדכון באדפטר להצגה ב-UI
                        adapter.updateStatsForActivity(activityId, count, average);
                    });
        }
    }
}
