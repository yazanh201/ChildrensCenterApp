package com.example.childrenscenterapp2.ui.parent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
 * Fragment להצגת משוב שניתן לילד ע"י מדריך בפעילות מסוימת.
 * מחפש את הרישום של הילד לפעילות וטוען את השדות feedbackScore ו־feedbackComment.
 */
public class GuideFeedbackFragment extends Fragment {

    private static final String ARG_ACTIVITY_ID = "activityId";
    private static final String ARG_CHILD_ID = "childId";

    private String activityId;
    private String childId;

    private RecyclerView recyclerView;
    private TextView tvNoFeedback;
    private GuideFeedbackAdapter adapter;

    /**
     * חובה קונסטרקטור ריק עבור Fragment
     */
    public GuideFeedbackFragment() {}

    /**
     * יצירת מופע של Fragment עם פרמטרים (activityId ו־childId)
     */
    public static GuideFeedbackFragment newInstance(String activityId, String childId) {
        GuideFeedbackFragment fragment = new GuideFeedbackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACTIVITY_ID, activityId);
        args.putString(ARG_CHILD_ID, childId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * קריאה בעת יצירת ה־Fragment – שליפת הפרמטרים מה־Bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            activityId = getArguments().getString(ARG_ACTIVITY_ID);
            childId = getArguments().getString(ARG_CHILD_ID);
            Log.d("GuideFeedbackFragment", "📌 activityId=" + activityId + ", childId=" + childId);
        }
    }

    /**
     * יצירת תצוגת ה־Fragment (טעינת XML והגדרת RecyclerView)
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide_feedback, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFeedback);
        tvNoFeedback = view.findViewById(R.id.tvNoFeedback);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GuideFeedbackAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadFeedbacks(); // טען את הנתונים מ־Firestore

        return view;
    }

    /**
     * שליפת המשובים של הילד מתוך Firestore לפי activityId ו־childId
     */
    private void loadFeedbacks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (activityId == null || childId == null) {
            Log.e("GuideFeedbackFragment", "❌ activityId או childId חסרים");
            showNoFeedbackMessage();
            return;
        }

        db.collection("activities")
                .document(activityId)
                .collection("registrations")
                .whereEqualTo("childId", childId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Map<String, Object>> feedbacks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Map<String, Object> data = doc.getData();
                        // רק אם יש משוב כלשהו (ציון או הערה), נוסיף לרשימה
                        if (data.containsKey("feedbackScore") || data.containsKey("feedbackComment")) {
                            feedbacks.add(data);
                        }
                    }

                    if (feedbacks.isEmpty()) {
                        showNoFeedbackMessage();
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        tvNoFeedback.setVisibility(View.GONE);
                        adapter.setFeedbackList(feedbacks);
                        Log.d("GuideFeedbackFragment", "✅ נמצאו " + feedbacks.size() + " משובים");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("GuideFeedbackFragment", "🔥 שגיאה בגישה ל־Firestore", e);
                    showNoFeedbackMessage();
                });
    }

    /**
     * הצגת הודעה כאשר אין משוב להצגה
     */
    private void showNoFeedbackMessage() {
        recyclerView.setVisibility(View.GONE);
        tvNoFeedback.setVisibility(View.VISIBLE);
    }
}
