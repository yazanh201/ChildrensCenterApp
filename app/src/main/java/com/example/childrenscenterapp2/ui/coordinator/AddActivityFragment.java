package com.example.childrenscenterapp2.ui.coordinator;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.local.ActivityDatabaseHelper;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.*;

public class AddActivityFragment extends Fragment {

    private EditText etName, etDescription, etMinAge, etMaxAge, etDays, etMaxParticipants;
    private Spinner spinnerDomain, spinnerGuide;
    private Switch switchOneTime;
    private Button btnSave;

    private FirebaseFirestore firestore;
    private ActivityDatabaseHelper localDb;

    private List<String> guideIds = new ArrayList<>();
    private List<String> guideNames = new ArrayList<>();
    private Map<String, String> guideNameToIdMap = new HashMap<>(); // ✅ מיפוי שם ל־uid
    private ArrayAdapter<String> guideAdapter;
    private String selectedGuideName = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
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
        localDb = new ActivityDatabaseHelper(requireContext());

        // Spinner התחומים
        ArrayAdapter<String> domainAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"מדע", "חברה", "יצירה"});
        domainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDomain.setAdapter(domainAdapter);

        // Spinner מדריכים
        guideAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, guideNames);
        guideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGuide.setAdapter(guideAdapter);

        spinnerGuide.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < guideNames.size()) {
                    selectedGuideName = guideNames.get(position);
                } else {
                    selectedGuideName = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGuideName = null;
            }
        });

        spinnerDomain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadGuidesFromAllDomains();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> saveActivity());

        loadGuidesFromAllDomains();

        return view;
    }

    private void loadGuidesFromAllDomains() {
        firestore.collection("users")
                .whereEqualTo("type", "מדריך")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    guideIds.clear();
                    guideNames.clear();
                    guideNameToIdMap.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getString("uid");
                        String name = doc.getString("name");
                        String specialization = doc.getString("specialization");

                        if (id != null && name != null) {
                            String displayName = name + " (" + specialization + ")";
                            guideIds.add(id);
                            guideNames.add(displayName);
                            guideNameToIdMap.put(displayName, id); // ✅ מיפוי לשם
                        }
                    }

                    guideAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(), "⚠️ שגיאה בטעינת מדריכים", Snackbar.LENGTH_LONG).show());
    }

    private void saveActivity() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String minAgeStr = etMinAge.getText().toString().trim();
        String maxAgeStr = etMaxAge.getText().toString().trim();
        String daysInput = etDays.getText().toString().trim();
        String maxParticipantsStr = etMaxParticipants.getText().toString().trim();
        String domain = spinnerDomain.getSelectedItem().toString();
        boolean isOneTime = switchOneTime.isChecked();

        if (TextUtils.isEmpty(name)) {
            Snackbar.make(requireView(), "נא להזין שם פעילות", Snackbar.LENGTH_SHORT).show();
            return;
        }

        int minAge = TextUtils.isEmpty(minAgeStr) ? 0 : Integer.parseInt(minAgeStr);
        int maxAge = TextUtils.isEmpty(maxAgeStr) ? 0 : Integer.parseInt(maxAgeStr);
        int maxParticipants = TextUtils.isEmpty(maxParticipantsStr) ? 0 : Integer.parseInt(maxParticipantsStr);
        List<String> days = TextUtils.isEmpty(daysInput) ? new ArrayList<>() : Arrays.asList(daysInput.split(",\\s*"));
        String id = UUID.randomUUID().toString();
        Timestamp now = Timestamp.now();
        boolean approved = !isOneTime;
        String month = LocalDate.now().getMonthValue() + "-" + LocalDate.now().getYear();
        String guideName = selectedGuideName;
        String guideUid = guideNameToIdMap.get(guideName); // ✅ מביא את ה־uid

        ActivityModel activity = new ActivityModel(
                id, name, description, domain, minAge, maxAge, days,
                maxParticipants, now, isOneTime, approved, guideName, month
        );

        firestore.collection("activities")
                .document(id)
                .set(activity)
                .addOnSuccessListener(unused -> {
                    localDb.insertActivity(activity);
                    Log.d("SaveActivity", "\uD83C\uDF89 שמירה ל-Firebase ו-SQLite בוצעה בהצלחה");
                    Snackbar.make(requireView(), "✅ הפעילות נשמרה בהצלחה", Snackbar.LENGTH_LONG).show();

                    // ✅ עדכון המדריך עם מזהה הפעילות
                    if (guideUid != null) {
                        firestore.collection("users")
                                .document(guideUid)
                                .update("activities", FieldValue.arrayUnion(name))

                                .addOnSuccessListener(unused2 -> Log.d("GuideUpdate", "🎯 פעילות עודכנה אצל המדריך"))
                                .addOnFailureListener(e -> Log.e("GuideUpdate", "❌ שגיאה בעדכון מדריך", e));
                    }

                    clearFields();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(), "❌ שגיאה: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    private void clearFields() {
        etName.setText("");
        etDescription.setText("");
        etMinAge.setText("");
        etMaxAge.setText("");
        etDays.setText("");
        etMaxParticipants.setText("");
        switchOneTime.setChecked(false);
        spinnerDomain.setSelection(0);
    }
}
