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
 * Fragment שמציג את כל הפעילויות לרכז
 */
public class CoordinatorActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivitiesAdapter adapter;
    private List<ActivityModel> activityList = new ArrayList<>();
    private FirebaseFirestore firestore;

    private Spinner spinnerDomain, spinnerMonth, spinnerGuide;

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
                    setupSpinners(); // אתחול ספינרים אחרי טעינה
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

    /**
     * אתחול הספינרים והאזנה לבחירה
     */
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

    /**
     * סינון הפעילויות לפי הערכים שנבחרו
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
     * יצירת מתאם פשוט עבור Spinner
     */
    private ArrayAdapter<String> createAdapter(List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
}
