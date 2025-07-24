package com.example.childrenscenterapp2.ui.child;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChildActivitiesFragment extends Fragment {

    private Spinner spinnerDomain, spinnerDay;
    private EditText etAge;
    private Button btnSearch;
    private RecyclerView recyclerViewActivities;
    private ChildActivitiesAdapter adapter;
    private List<ActivityModel> allActivities = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference activitiesRef = db.collection("activities");

    private String childUid = null;
    private String childName = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_child_activities, container, false);

        spinnerDomain = view.findViewById(R.id.spinnerDomain);
        spinnerDay = view.findViewById(R.id.spinnerDay);
        etAge = view.findViewById(R.id.etAge);
        btnSearch = view.findViewById(R.id.btnSearch);
        recyclerViewActivities = view.findViewById(R.id.recyclerViewActivities);

        recyclerViewActivities.setLayoutManager(new LinearLayoutManager(getContext()));

        // Spinner תחום
        ArrayAdapter<CharSequence> domainAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.activity_domains,
                android.R.layout.simple_spinner_item
        );
        domainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDomain.setAdapter(domainAdapter);

        // Spinner ימים
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.week_days,
                android.R.layout.simple_spinner_item
        );
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        // קבלת UID של ילד אם עבר ב־Bundle
        if (getArguments() != null && getArguments().containsKey("childUid")) {
            childUid = getArguments().getString("childUid");

            FirebaseFirestore.getInstance().collection("users")
                    .document(childUid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String nameFromDb = snapshot.getString("name");
                            if (nameFromDb != null && !nameFromDb.isEmpty()) {
                                childName = nameFromDb;
                                setupAdapter(); // ערכים מוכנים
                            } else {
                                Toast.makeText(getContext(), "שם הילד חסר במסד הנתונים", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "המשתמש לא קיים במסד", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "שגיאה בטעינת שם הילד", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // אם אין childUid – נניח שזה הילד המחובר
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                childUid = user.getUid();
                childName = user.getEmail(); // או כל שם שיופיע
                setupAdapter(); // בטוח לקרוא כאן
            } else {
                Toast.makeText(getContext(), "⚠️ אין משתמש מחובר", Toast.LENGTH_SHORT).show();
            }
        }

        btnSearch.setOnClickListener(v -> applyFilters());

        return view;
    }

    private void setupAdapter() {
        // נבדוק אם זה צפייה כהורה או כילד
        boolean isParentView = getArguments() != null && getArguments().getBoolean("isParentView", false);
        adapter = new ChildActivitiesAdapter(new ArrayList<ActivityModel>(), !isParentView); // רק אם זה *לא* הורה – הכפתור יוצג


        // הגנה מפני ערכים חסרים
        if (childUid != null && childName != null) {
            adapter.setChildOverride(childUid, childName);
        }

        recyclerViewActivities.setAdapter(adapter);
        loadActivities();
    }


    private void loadActivities() {
        activitiesRef.get().addOnSuccessListener(querySnapshot -> {
            allActivities.clear();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                ActivityModel activity = doc.toObject(ActivityModel.class);
                activity.setId(doc.getId());

                // ✅ שליפת ערך isRegistrationOpen ישירות מהמסד
                Boolean isOpen = doc.getBoolean("isRegistrationOpen");
                if (isOpen != null) {
                    activity.setIsRegistrationOpen(isOpen);
                } else {
                    activity.setIsRegistrationOpen(false); // ברירת מחדל אם לא קיים
                }

                allActivities.add(activity);
            }

            applyFilters();
        });
    }


    private void applyFilters() {
        String selectedDomain = spinnerDomain.getSelectedItem().toString();
        String selectedDay = spinnerDay.getSelectedItem().toString();
        String ageText = etAge.getText().toString().trim();

        Integer age = null;
        if (!TextUtils.isEmpty(ageText)) {
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "גיל לא תקין", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        List<ActivityModel> filtered = new ArrayList<>();
        for (ActivityModel activity : allActivities) {
            if (!selectedDomain.equals("כל התחומים") && !activity.getDomain().equals(selectedDomain)) {
                continue;
            }
            if (!selectedDay.equals("כל הימים") && (activity.getDays() == null || !activity.getDays().contains(selectedDay))) {
                continue;
            }
            if (age != null && (age < activity.getMinAge() || age > activity.getMaxAge())) {
                continue;
            }
            filtered.add(activity);
        }

        adapter.setData(filtered);
    }
}
