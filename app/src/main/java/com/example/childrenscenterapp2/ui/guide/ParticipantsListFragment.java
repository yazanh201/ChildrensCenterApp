// ParticipantsListFragment.java
package com.example.childrenscenterapp2.ui.guide;

import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment להצגת רשימת משתתפים בפעילות מסוימת
 * משמש מדריך כדי לצפות ולתת משוב למשתתפים
 */
public class ParticipantsListFragment extends Fragment {

    private static final String ARG_ACTIVITY_ID = "activityId";     // מפתח ל־argument מזהה פעילות
    private static final String ARG_ACTIVITY_NAME = "activityName"; // מפתח ל־argument שם פעילות

    private String activityId;
    private String activityName;
    private RecyclerView recyclerView;

    /**
     * יצירת מופע חדש של Fragment עם פרטי הפעילות כ־arguments
     */
    public static ParticipantsListFragment newInstance(String activityId, String activityName) {
        ParticipantsListFragment fragment = new ParticipantsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACTIVITY_ID, activityId);
        args.putString(ARG_ACTIVITY_NAME, activityName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * בניית התצוגה של המסך - כולל אתחול RecyclerView וטעינת נתונים
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_participants_list, container, false);
        recyclerView = view.findViewById(R.id.rvParticipants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // שליפת פרטי הפעילות מתוך arguments
        if (getArguments() != null) {
            activityId = getArguments().getString(ARG_ACTIVITY_ID);
            activityName = getArguments().getString(ARG_ACTIVITY_NAME);
        }

        loadParticipants(); // טען משתתפים מה־Firestore
        return view;
    }

    /**
     * טעינת רשימת המשתתפים הרשומים לפעילות מתוך Firestore
     * ושיוכם ל־ParticipantsAdapter_guide
     */
    private void loadParticipants() {
        FirebaseFirestore.getInstance()
                .collection("activities")
                .document(activityId)
                .collection("registrations")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Map<String, Object>> participants = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Map<String, Object> data = doc.getData();
                        data.put("registrationId", doc.getId()); // הוספת מזהה רשומה
                        participants.add(data);
                    }

                    // יצירת ומתן האדפטר לרשימה
                    ParticipantsAdapter_guide adapter = new ParticipantsAdapter_guide(participants, activityId);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "שגיאה בטעינת משתתפים", Toast.LENGTH_SHORT).show());
    }
}
