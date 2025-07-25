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
import com.example.childrenscenterapp2.data.local.ActivityDatabaseHelper;
import android.util.Log;

/**
 * {@code EditActivityFragment} – פרגמנט המאפשר לערוך פעילות קיימת.
 * <p>
 * תפקיד המחלקה:
 * <ul>
 *   <li>טעינת נתוני פעילות קיימת והצגתם בטופס עריכה.</li>
 *   <li>עדכון פרטי פעילות ב-Firebase Firestore.</li>
 *   <li>סנכרון שינויים למסד הנתונים המקומי (SQLite).</li>
 *   <li>ניהול קשר בין פעילות לבין מדריך נבחר (כולל עדכון מדריך חדש/ישן).</li>
 * </ul>
 */
public class EditActivityFragment extends Fragment {

    /** רכיבי טופס עריכה */
    private EditText etName, etDescription, etMinAge, etMaxAge, etDays, etMaxParticipants;
    private Spinner spinnerDomain, spinnerGuide;
    private Switch switchOneTime;
    private Button btnSave;

    /** הפעילות שנבחרה לעריכה */
    private ActivityModel activityToEdit;

    /** חיבור ל-Firebase Firestore */
    private FirebaseFirestore firestore;

    /** רשימות נתוני מדריכים */
    private List<String> guideIds = new ArrayList<>();
    private List<String> guideNames = new ArrayList<>();
    private ArrayAdapter<String> guideAdapter;
    private String selectedGuideName;

    /**
     * בנאי המקבל פעילות לעריכה.
     *
     * @param activity אובייקט פעילות לעריכה.
     */
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

        // Spinner תחומים
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

        // בחירת מדריך
        spinnerGuide.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < guideNames.size()) {
                    selectedGuideName = guideNames.get(position); // ✨ שומר את שם המדריך שנבחר
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGuideName = null;
            }
        });

        // מילוי השדות בנתונים הקיימים של הפעילות
        fillFieldsWithData();

        // שינוי כפתור לשמירה עם טקסט עדכון
        btnSave.setText("💾 עדכן פעילות");
        btnSave.setOnClickListener(v -> updateActivity());

        // טעינת רשימת מדריכים לצורך בחירה
        loadGuidesFromAllDomains();

        return view;
    }

    /**
     * מילוי שדות הטופס עם הנתונים של הפעילות שנבחרה לעריכה.
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

        // בחירת התחום הנוכחי ב-Spinner
        String currentDomain = activityToEdit.getDomain();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerDomain.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(currentDomain);
            if (position >= 0) spinnerDomain.setSelection(position);
        }
    }

    /**
     * טוען את כל המדריכים מ-Firebase ומציג אותם ב-Spinner עם תחום ההתמחות ליד השם.
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

                    // ❗️ בחירת המדריך הנוכחי בטופס אם קיים
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
     * עדכון הפעילות ב-Firebase Firestore וב-SQLite כולל טיפול בשינוי מדריך.
     */
    private void updateActivity() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("הכנס שם פעילות");
            return;
        }

        // המרת הימים לרשימה
        List<String> daysList = new ArrayList<>();
        for (String day : etDays.getText().toString().split(",")) {
            daysList.add(day.trim());
        }

        String newGuideName = selectedGuideName;
        String oldGuideName = activityToEdit.getGuideName(); // שם המדריך הישן לפני עדכון

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // עדכון נתוני הפעילות ב-Firebase
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

                    // ✅ עדכון ב-SQLite מקומית
                    ActivityModel updatedActivity = new ActivityModel(
                            activityToEdit.getId(),
                            name,
                            description,
                            spinnerDomain.getSelectedItem().toString(),
                            Integer.parseInt(etMinAge.getText().toString()),
                            Integer.parseInt(etMaxAge.getText().toString()),
                            daysList,
                            Integer.parseInt(etMaxParticipants.getText().toString())
                    );
                    updatedActivity.setGuideName(newGuideName);
                    updatedActivity.setOneTime(switchOneTime.isChecked());

                    ActivityDatabaseHelper localDb = new ActivityDatabaseHelper(requireContext());
                    boolean success = localDb.updateActivity(updatedActivity);

                    if (success) {
                        Log.d("SQLiteSync", "✔️ הפעילות עודכנה בהצלחה גם במסד המקומי (SQLite)");
                    } else {
                        Log.e("SQLiteSync", "❌ עדכון הפעילות נכשל במסד המקומי (SQLite)");
                    }

                    // ✅ עדכון קשר בין מדריך ישן למדריך חדש
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
