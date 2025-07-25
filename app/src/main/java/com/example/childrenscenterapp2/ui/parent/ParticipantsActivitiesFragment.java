package com.example.childrenscenterapp2.ui.parent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment להצגת כל הפעילויות שאליהן הילד נרשם,
 * כולל כפתורים להוספת משוב הורה, צפייה בתמונות ובמשוב מדריך.
 */
public class ParticipantsActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivitiesForChildAdapter adapter;
    private List<String> activityNames = new ArrayList<>();
    private String childUid;

    public ParticipantsActivitiesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_participants_activities, container, false);
        recyclerView = view.findViewById(R.id.rvActivitiesForChild);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // יצירת האדאפטר עם רשימת הפעילויות
        adapter = new ActivitiesForChildAdapter(activityNames, null, getParentFragmentManager());
        adapter.setUserRole("parent"); // קובע שהתצוגה עבור הורה
        recyclerView.setAdapter(adapter);

        // קבלת הפרמטרים שהועברו ל־Fragment
        if (getArguments() != null) {
            if (getArguments().containsKey("childUid")) {
                // אם הועבר מזהה ילד – שלוף את הפעילויות שלו מהמסמכים
                childUid = getArguments().getString("childUid");
                adapter.setChildId(childUid);
                Log.d("ActivitiesDebug", "Child UID: " + childUid);
                loadActivitiesForChild(childUid);

            } else if (getArguments().containsKey("activityIds")) {
                // אם הועברה רשימת מזהי פעילויות – הצג כפי שהיא
                List<String> ids = getArguments().getStringArrayList("activityIds");
                if (ids != null && !ids.isEmpty()) {
                    activityNames.clear();
                    activityNames.addAll(ids);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "❌ אין פעילויות להצגה", Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            Toast.makeText(getContext(), "❌ לא נבחרו נתונים", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /**
     * שליפת כל הפעילויות של הילד לפי תת־אוסף registrations במסמכי המשתמש.
     * כל פעילות תוצג בפורמט: תחום@activityId
     */
    private void loadActivitiesForChild(String childUid) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(childUid)
                .collection("registrations")
                .get()
                .addOnSuccessListener(snapshot -> {
                    activityNames.clear();

                    if (snapshot.isEmpty()) {
                        Toast.makeText(getContext(), "❌ אין פעילויות משויכות", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot doc : snapshot) {
                        String activityId = doc.getString("activityId");
                        String domain = doc.getString("domain");

                        if (activityId != null && domain != null) {
                            String display = domain + "@" + activityId; // פורמט אחיד להצגה באדאפטר
                            if (!activityNames.contains(display)) {
                                activityNames.add(display);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged(); // רענון רשימת הפעילויות
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
                });
    }
}
