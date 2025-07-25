package com.example.childrenscenterapp2.ui.parent;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.ui.guide.DialogFeedbackInput;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter להצגת רשימת פעילויות עבור ילד אחד, עם אפשרות להורה לתת משוב, לצפות במשוב מדריך או בתמונות.
 */
public class ActivitiesForChildAdapter extends RecyclerView.Adapter<ActivitiesForChildAdapter.ActivityViewHolder> {

    private List<String> activities; // פורמט: "activityName@activityId"
    private String childId; // מזהה הילד
    private FragmentManager fragmentManager; // ניהול מעבר בין פרגמנטים
    private String userRole = "parent"; // תפקיד המשתמש (ברירת מחדל: הורה)

    /**
     * בנאי של האדפטר
     * @param activities רשימת פעילויות בפורמט "שם@מזהה"
     * @param childId מזהה הילד
     * @param fragmentManager ניהול ניווט בין פרגמנטים
     */
    public ActivitiesForChildAdapter(List<String> activities, String childId, FragmentManager fragmentManager) {
        this.activities = activities;
        this.childId = childId;
        this.fragmentManager = fragmentManager;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public void setUserRole(String role) {
        this.userRole = role;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_for_child, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        String raw = activities.get(position);
        String[] parts = raw.split("@");

        if (parts.length < 2) {
            Log.w("ActivitiesAdapter", "⚠️ פורמט שגוי: אין '@' או רק חלק אחד");
            holder.tvActivityName.setText("פעילות לא תקינה");
            return;
        }

        String activityName = parts[0];
        String activityId = parts[1];
        holder.tvActivityName.setText(activityName);

        // אם לא מדובר בהורה – הסתרת הכפתורים
        if (!"parent".equals(userRole)) {
            holder.btnAddFeedback.setVisibility(View.GONE);
            holder.btnViewGuideFeedback.setVisibility(View.GONE);
            holder.btnViewPhotos.setVisibility(View.GONE);
        }

        // שליחת משוב כהורה
        holder.btnAddFeedback.setOnClickListener(v -> {
            DialogFeedbackInput.showDialog(v.getContext(), (score, comment) -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("activities")
                        .document(activityId)
                        .collection("registrations")
                        .whereEqualTo("childId", childId)
                        .get()
                        .addOnSuccessListener(query -> {
                            if (!query.isEmpty()) {
                                DocumentReference regDoc = query.getDocuments().get(0).getReference();

                                Map<String, Object> data = new HashMap<>();
                                data.put("parentScore", score);
                                data.put("parentComment", comment);

                                regDoc.set(data, SetOptions.merge())
                                        .addOnSuccessListener(unused ->
                                                Toast.makeText(v.getContext(), "✅ הביקורת נשמרה", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(v.getContext(), "❌ שגיאה בשמירת הביקורת", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(v.getContext(), "❌ לא נמצא רישום לילד", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(), "❌ שגיאה בגישה למסמכים", Toast.LENGTH_SHORT).show());
            });
        });

        // הצגת משוב מדריך
        holder.btnViewGuideFeedback.setOnClickListener(v -> {
            GuideFeedbackFragment fragment = GuideFeedbackFragment.newInstance(activityId, childId);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // הצגת תמונות מהפעילות
        holder.btnViewPhotos.setOnClickListener(v -> {
            ViewPhotosFragment fragment = ViewPhotosFragment.newInstance(activityId);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    /**
     * ViewHolder עבור כל כרטיס פעילות
     */
    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvActivityName;
        Button btnAddFeedback, btnViewGuideFeedback, btnViewPhotos;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityName = itemView.findViewById(R.id.tvActivityName);
            btnAddFeedback = itemView.findViewById(R.id.btnAddParentFeedback);
            btnViewGuideFeedback = itemView.findViewById(R.id.btnViewGuideFeedback);
            btnViewPhotos = itemView.findViewById(R.id.btnViewPhotos);
        }
    }
}
