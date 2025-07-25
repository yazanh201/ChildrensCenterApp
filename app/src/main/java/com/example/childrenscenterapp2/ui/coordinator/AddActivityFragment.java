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

/**
 * {@code AddActivityFragment} - פרגמנט להוספת פעילות חדשה למערכת.
 * <p>
 * תפקיד הפרגמנט:
 * <ul>
 *   <li>קבלת פרטי פעילות מהמשתמש דרך טופס UI.</li>
 *   <li>שמירת הפעילות גם ב-Firebase Firestore וגם במסד הנתונים המקומי (SQLite).</li>
 *   <li>שיוך הפעילות למדריך נבחר.</li>
 *   <li>טעינת רשימת מדריכים מכל התחומים והצגתם ב-Spinner.</li>
 * </ul>
 */
public class AddActivityFragment extends Fragment {

    /** רכיבי טופס להזנת פרטי הפעילות */
    private EditText etName, etDescription, etMinAge, etMaxAge, etDays, etMaxParticipants;
    private Spinner spinnerDomain, spinnerGuide;
    private Switch switchOneTime;
    private Button btnSave;

    /** חיבור למסדי נתונים */
    private FirebaseFirestore firestore;
    private ActivityDatabaseHelper localDb;

    /** רשימות ומפות לשמירת נתוני המדריכים */
    private List<String> guideIds = new ArrayList<>();
    private List<String> guideNames = new ArrayList<>();
    private Map<String, String> guideNameToIdMap = new HashMap<>(); // ✅ מיפוי בין שם ל-UID
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

        // Spinner של תחומי פעילות
        ArrayAdapter<String> domainAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"מדע", "חברה", "יצירה"});
        domainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDomain.setAdapter(domainAdapter);

        // Spinner של מדריכים
        guideAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, guideNames);
        guideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGuide.setAdapter(guideAdapter);

        // בחירת מדריך מה-Spinner
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

        // בעת בחירת תחום - טען את כל המדריכים (ניתן לשנות לסינון לפי תחום)
        spinnerDomain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadGuidesFromAllDomains();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // כפתור שמירה
        btnSave.setOnClickListener(v -> saveActivity());

        // טעינת מדריכים מהמסד
        loadGuidesFromAllDomains();

        return view;
    }

    /**
     * טוען את רשימת המדריכים מכל התחומים מתוך Firestore ומעדכן את ה-Spinner.
     */
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
                            guideNameToIdMap.put(displayName, id); // ✅ מיפוי שם ל-UID
                        }
                    }

                    guideAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(), "⚠️ שגיאה בטעינת מדריכים", Snackbar.LENGTH_LONG).show());
    }

    /**
     * שמירת הפעילות החדשה במסדי הנתונים (Firebase + SQLite).
     * כולל בדיקת תקינות שדות והוספת הפעילות למדריך נבחר.
     */
    private void saveActivity() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String minAgeStr = etMinAge.getText().toString().trim();
        String maxAgeStr = etMaxAge.getText().toString().trim();
        String daysInput = etDays.getText().toString().trim();
        String maxParticipantsStr = etMaxParticipants.getText().toString().trim();
        String domain = spinnerDomain.getSelectedItem().toString();
        boolean isOneTime = switchOneTime.isChecked();

        // בדיקת תקינות שם פעילות
        if (TextUtils.isEmpty(name)) {
            Snackbar.make(requireView(), "נא להזין שם פעילות", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // המרת ערכים למספרים ורשימות
        int minAge = TextUtils.isEmpty(minAgeStr) ? 0 : Integer.parseInt(minAgeStr);
        int maxAge = TextUtils.isEmpty(maxAgeStr) ? 0 : Integer.parseInt(maxAgeStr);
        int maxParticipants = TextUtils.isEmpty(maxParticipantsStr) ? 0 : Integer.parseInt(maxParticipantsStr);
        List<String> days = TextUtils.isEmpty(daysInput) ? new ArrayList<>() : Arrays.asList(daysInput.split(",\\s*"));

        // יצירת מזהה ייחודי לפעילות
        String id = UUID.randomUUID().toString();
        Timestamp now = Timestamp.now();
        boolean approved = !isOneTime;
        String month = LocalDate.now().getMonthValue() + "-" + LocalDate.now().getYear();
        String guideName = selectedGuideName;
        String guideUid = guideNameToIdMap.get(guideName); // ✅ מציאת UID של המדריך הנבחר

        // בניית אובייקט הפעילות
        ActivityModel activity = new ActivityModel(
                id, name, description, domain, minAge, maxAge, days,
                maxParticipants, now, isOneTime, approved, guideName, month
        );

        // שמירת הפעילות ב-Firebase
        firestore.collection("activities")
                .document(id)
                .set(activity)
                .addOnSuccessListener(unused -> {
                    // הוספת שדה isRegistrationOpen כברירת מחדל: סגור
                    firestore.collection("activities")
                            .document(id)
                            .update("isRegistrationOpen", false);

                    // שמירה מקומית ב-SQLite
                    localDb.insertActivity(activity);
                    Log.d("SaveActivity", "🎉 שמירה ל-Firebase ו-SQLite בוצעה בהצלחה");
                    Snackbar.make(requireView(), "✅ הפעילות נשמרה בהצלחה", Snackbar.LENGTH_LONG).show();

                    // עדכון רשימת הפעילויות של המדריך הנבחר
                    if (guideUid != null) {
                        firestore.collection("users")
                                .document(guideUid)
                                .update("activities", FieldValue.arrayUnion(name))
                                .addOnSuccessListener(unused2 -> Log.d("GuideUpdate", "🎯 פעילות עודכנה אצל המדריך"))
                                .addOnFailureListener(e -> Log.e("GuideUpdate", "❌ שגיאה בעדכון מדריך", e));
                    }

                    // איפוס השדות לאחר שמירה
                    clearFields();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(), "❌ שגיאה: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    /**
     * איפוס כל השדות בטופס להזנת פעילות חדשה.
     */
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
