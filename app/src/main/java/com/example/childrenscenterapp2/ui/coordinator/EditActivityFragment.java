package com.example.childrenscenterapp2.ui.coordinator;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment לעריכת פעילות קיימת
 */
public class EditActivityFragment extends Fragment {

    private EditText etName, etDescription, etMinAge, etMaxAge, etDays, etMaxParticipants;
    private Spinner spinnerDomain, spinnerGuide;
    private Switch switchOneTime;
    private Button btnSave;

    private ActivityModel activityToEdit;
    private FirebaseFirestore firestore;

    private List<String> guideIds = new ArrayList<>();
    private List<String> guideNames = new ArrayList<>();
    private ArrayAdapter<String> guideAdapter;
    private String selectedGuideName;

    public EditActivityFragment(ActivityModel activity) {
        this.activityToEdit = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_activity, container, false);

        // אתחול רכיבי UI
        etName = view.findViewById(R.id.etName);
        etDescription = view.findViewById(R.id.etDescription);
        etMinAge = view.findViewById(R.id.etMinAge);
        etMaxAge = view.findViewById(R.id.etMaxAge);
        etDays = view.findViewById(R.id.etDays);
        etMaxParticipants = view.findViewById(R.id.etMaxParticipants);
        spinnerDomain = view.findViewById(R.id.spinnerDomain);
        spinnerGuide = view.findViewById(R.id.spinnerGuide);
        switchOneTime = view.findViewById(R.id.switchOneTime);
        btnSave = view.findViewById(R.id.btnSave);

        firestore = FirebaseFirestore.getInstance();

        // הגדרת Spinner של התחומים
        ArrayAdapter<String> domainAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"מדע", "חברה", "יצירה"});
        domainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDomain.setAdapter(domainAdapter);

        // הגדרת Spinner של מדריכים
        guideAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, guideNames);
        guideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGuide.setAdapter(guideAdapter);

        spinnerGuide.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < guideNames.size()) {
                    selectedGuideName = guideNames.get(position); // ✨ שומר את שם המדריך
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGuideName = null;
            }
        });


        // מילוי שדות עם הנתונים הקיימים
        fillFieldsWithData();

        btnSave.setText("💾 עדכן פעילות");
        btnSave.setOnClickListener(v -> updateActivity());

        // טען את כל המדריכים עם תחום לצורך תצוגה
        loadGuidesFromAllDomains();

        return view;
    }

    /**
     * מילוי שדות הטופס עם נתוני הפעילות
     */
    private void fillFieldsWithData() {
        etName.setText(activityToEdit.getName());
        etDescription.setText(activityToEdit.getDescription());
        etMinAge.setText(String.valueOf(activityToEdit.getMinAge()));
        etMaxAge.setText(String.valueOf(activityToEdit.getMaxAge()));
        etDays.setText(TextUtils.join(", ", activityToEdit.getDays()));
        etMaxParticipants.setText(String.valueOf(activityToEdit.getMaxParticipants()));
        switchOneTime.setChecked(activityToEdit.isOneTime());
        selectedGuideName = activityToEdit.getGuideName();

        // הצגת התחום שנבחר מראש
        String currentDomain = activityToEdit.getDomain();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerDomain.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(currentDomain);
            if (position >= 0) spinnerDomain.setSelection(position);
        }
    }

    /**
     * טוען את כל המדריכים מה-DB עם התחום ליד השם
     */
    private void loadGuidesFromAllDomains() {
        firestore.collection("users")
                .whereEqualTo("type", "מדריך")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    guideIds.clear();
                    guideNames.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getString("uid");
                        String name = doc.getString("name");
                        String specialization = doc.getString("specialization");

                        if (id != null && name != null) {
                            String fullName = name + " (" + specialization + ")";
                            guideIds.add(id);
                            guideNames.add(fullName);
                        }
                    }

                    guideAdapter.notifyDataSetChanged();

                    // ❗️הצגת המדריך הנבחר בטופס
                    if (selectedGuideName != null) {
                        int pos = guideNames.indexOf(selectedGuideName);
                        if (pos >= 0) {
                            spinnerGuide.setSelection(pos);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "❌ שגיאה בטעינת מדריכים", Toast.LENGTH_SHORT).show());
    }


    /**
     * עדכון הנתונים בפיירבייס
     */
    private void updateActivity() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("הכנס שם פעילות");
            return;
        }

        List<String> daysList = new ArrayList<>();
        for (String day : etDays.getText().toString().split(",")) {
            daysList.add(day.trim());
        }

        String newGuideName = selectedGuideName;
        String oldGuideName = activityToEdit.getGuideName(); // נניח שזה נשמר באובייקט הפעילות

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("activities")
                .document(activityToEdit.getId())
                .update(
                        "name", name,
                        "description", description,
                        "minAge", Integer.parseInt(etMinAge.getText().toString()),
                        "maxAge", Integer.parseInt(etMaxAge.getText().toString()),
                        "days", daysList,
                        "maxParticipants", Integer.parseInt(etMaxParticipants.getText().toString()),
                        "oneTime", switchOneTime.isChecked(),
                        "domain", spinnerDomain.getSelectedItem().toString(),
                        "guideName", newGuideName
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "✔️ פעילות עודכנה בהצלחה", Toast.LENGTH_SHORT).show();

                    // אם המדריך השתנה – נעדכן את המדריך הישן והחדש
                    if (!TextUtils.isEmpty(newGuideName) && !TextUtils.isEmpty(oldGuideName)
                            && !newGuideName.equals(oldGuideName)) {

                        String newGuideOnlyName = newGuideName.split(" \\(")[0];
                        String oldGuideOnlyName = oldGuideName.split(" \\(")[0];

                        // הסרת הפעילות מהמדריך הישן
                        firestore.collection("users")
                                .whereEqualTo("type", "מדריך")
                                .whereEqualTo("name", oldGuideOnlyName)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    if (!snapshot.isEmpty()) {
                                        String oldGuideId = snapshot.getDocuments().get(0).getId();
                                        firestore.collection("users")
                                                .document(oldGuideId)
                                                .update("activities", com.google.firebase.firestore.FieldValue.arrayRemove(name));
                                    }
                                });

                        // הוספת הפעילות למדריך החדש
                        firestore.collection("users")
                                .whereEqualTo("type", "מדריך")
                                .whereEqualTo("name", newGuideOnlyName)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    if (!snapshot.isEmpty()) {
                                        String newGuideId = snapshot.getDocuments().get(0).getId();
                                        firestore.collection("users")
                                                .document(newGuideId)
                                                .update("activities", com.google.firebase.firestore.FieldValue.arrayUnion(name));
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "❌ שגיאה בעדכון פעילות", Toast.LENGTH_SHORT).show());
    }

}