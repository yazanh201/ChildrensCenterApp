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
import com.google.firebase.firestore.*;

import java.util.*;

/**
 * {@code GuideListFragment} – פרגמנט להצגת רשימת המדריכים במערכת.
 * <p>
 * תפקיד המחלקה:
 * <ul>
 *   <li>טעינת רשימת המדריכים מ-Firebase Firestore בזמן אמת.</li>
 *   <li>הצגת המדריכים ב-RecyclerView באמצעות {@link GuideAdapter}.</li>
 *   <li>אפשרות לסינון המדריכים לפי שם, תחום התמחות ופעילויות.</li>
 * </ul>
 */
public class GuideListFragment extends Fragment {

    /** RecyclerView להצגת רשימת המדריכים */
    private RecyclerView recyclerView;

    /** אדפטר מותאם אישית להצגת פרטי המדריכים */
    private GuideAdapter guideAdapter;

    /** חיבור ל-Firebase Firestore */
    private FirebaseFirestore firestore;

    /** Spinners לסינון לפי שם, תחום התמחות ופעילות */
    private Spinner spinnerGuideName, spinnerSpecialization, spinnerActivity;

    /** רשימת כל מסמכי המדריכים */
    private List<DocumentSnapshot> allGuideDocs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGuides);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        spinnerGuideName = view.findViewById(R.id.spinnerGuideName);
        spinnerSpecialization = view.findViewById(R.id.spinnerSpecialization);
        spinnerActivity = view.findViewById(R.id.spinnerActivity);

        guideAdapter = new GuideAdapter(new ArrayList<>(), requireContext());
        recyclerView.setAdapter(guideAdapter);

        firestore = FirebaseFirestore.getInstance();

        // טעינת המדריכים מהמסד
        loadGuidesFromFirestore();

        return view;
    }

    /**
     * טוען את רשימת המדריכים מ-Firebase Firestore בעזרת SnapshotListener.
     * הנתונים מתעדכנים בזמן אמת כאשר יש שינוי במסד.
     */
    private void loadGuidesFromFirestore() {
        firestore.collection("users")
                .whereEqualTo("type", "מדריך")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        Toast.makeText(getContext(), "❌ שגיאה בטעינת מדריכים", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allGuideDocs.clear();
                    allGuideDocs.addAll(value.getDocuments());

                    // עדכון האדפטר והגדרת הסינון
                    guideAdapter.setGuideDocuments(allGuideDocs);
                    setupSpinners();
                });
    }

    /**
     * מגדיר את ה-Spinners לסינון המדריכים לפי שם, תחום התמחות ופעילות.
     * שולף ערכים ייחודיים מתוך מסמכי המדריכים.
     */
    private void setupSpinners() {
        List<String> names = new ArrayList<>();
        List<String> domains = new ArrayList<>();
        List<String> activities = new ArrayList<>();

        names.add("חיפוש לפי שם");
        domains.add("חיפוש לפי תחום");
        activities.add("חיפוש לפי פעילות");

        for (DocumentSnapshot doc : allGuideDocs) {
            String name = doc.getString("name");
            String specialization = doc.getString("specialization");
            List<String> guideActivities = (List<String>) doc.get("activities");

            if (name != null && !names.contains(name)) names.add(name);
            if (specialization != null && !domains.contains(specialization)) domains.add(specialization);
            if (guideActivities != null) {
                for (String act : guideActivities) {
                    if (!activities.contains(act)) activities.add(act);
                }
            }
        }

        spinnerGuideName.setAdapter(createAdapter(names));
        spinnerSpecialization.setAdapter(createAdapter(domains));
        spinnerActivity.setAdapter(createAdapter(activities));

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterGuides();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerGuideName.setOnItemSelectedListener(filterListener);
        spinnerSpecialization.setOnItemSelectedListener(filterListener);
        spinnerActivity.setOnItemSelectedListener(filterListener);
    }

    /**
     * יוצר {@link ArrayAdapter} עבור Spinner מסוים.
     *
     * @param items רשימת פריטים להצגה ב-Spinner.
     * @return Adapter מותאם לשימוש עם Spinner.
     */
    private ArrayAdapter<String> createAdapter(List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    /**
     * סינון רשימת המדריכים לפי הערכים שנבחרו ב-Spinners (שם, תחום, פעילות).
     * התוצאה מעודכנת ב-RecyclerView.
     */
    private void filterGuides() {
        String selectedName = spinnerGuideName.getSelectedItem().toString();
        String selectedDomain = spinnerSpecialization.getSelectedItem().toString();
        String selectedActivity = spinnerActivity.getSelectedItem().toString();

        List<DocumentSnapshot> filtered = new ArrayList<>();

        for (DocumentSnapshot doc : allGuideDocs) {
            String name = doc.getString("name");
            String specialization = doc.getString("specialization");
            List<String> guideActivities = (List<String>) doc.get("activities");

            boolean matchName = selectedName.equals("חיפוש לפי שם") || selectedName.equals(name);
            boolean matchDomain = selectedDomain.equals("חיפוש לפי תחום") || selectedDomain.equals(specialization);
            boolean matchActivity = selectedActivity.equals("חיפוש לפי פעילות") ||
                    (guideActivities != null && guideActivities.contains(selectedActivity));

            if (matchName && matchDomain && matchActivity) {
                filtered.add(doc);
            }
        }

        guideAdapter.setGuideDocuments(filtered);
    }
}
