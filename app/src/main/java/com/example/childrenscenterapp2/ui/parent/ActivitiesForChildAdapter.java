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

public class ActivitiesForChildAdapter extends RecyclerView.Adapter<ActivitiesForChildAdapter.ActivityViewHolder> {

    private List<String> activities; // פורמט: שם@id
    private String childId;
    private FragmentManager fragmentManager;
    private String userRole = "parent"; // ברירת מחדל - הורה

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

        // 👇 הסתרה לפי תפקיד - אם לא הורה, הסתר כפתורים
        if (!"parent".equals(userRole)) {
            holder.btnAddFeedback.setVisibility(View.GONE);
            holder.btnViewGuideFeedback.setVisibility(View.GONE);
            holder.btnViewPhotos.setVisibility(View.GONE);
        }

        // ✔ שליחת משוב הורה
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

        // ✔ צפייה במשוב מדריך
        holder.btnViewGuideFeedback.setOnClickListener(v -> {
            GuideFeedbackFragment fragment = GuideFeedbackFragment.newInstance(activityId, childId);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // ✔ צפייה בתמונות
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

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvActivityName;
        Button btnAddFeedback, btnViewGuideFeedback, btnViewPhotos;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityName = itemView.findViewById(R.id.tvActivityName);
            btnAddFeedback = itemView.findViewById(R.id.btnAddParentFeedback);
            btnViewGuideFeedback = itemView.findViewById(R.id.btnViewGuideFeedback);
            btnViewPhotos = itemView.findViewById(R.id.btnViewPhotos); // ודא שקיים ב-XML
        }
    }
}
