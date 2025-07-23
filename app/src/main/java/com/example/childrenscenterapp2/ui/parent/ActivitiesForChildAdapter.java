package com.example.childrenscenterapp2.ui.parent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.ui.guide.DialogFeedbackInput;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ActivitiesForChildAdapter extends RecyclerView.Adapter<ActivitiesForChildAdapter.ActivityViewHolder> {

    private List<String> activities; // format: name@id
    private String childId;

    public ActivitiesForChildAdapter(List<String> activities, String childId) {
        this.activities = activities;
        this.childId = childId;
    }

    // ✅ מתודה להחלפת childId אחרי יצירת האדפטר
    public void setChildId(String childId) {
        this.childId = childId;
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
        String activityName = parts[0];
        String activityId = parts[1];

        holder.tvActivityName.setText(activityName);

        holder.btnAddFeedback.setOnClickListener(v -> {
            DialogFeedbackInput.showDialog(v.getContext(), (score, comment) -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference regRef = db.collection("activities")
                        .document(activityId)
                        .collection("registrations")
                        .document(childId);

                regRef.update("parentScore", score, "parentComment", comment)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(v.getContext(), "✅ הביקורת נשמרה", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(), "❌ שגיאה בשמירת הביקורת", Toast.LENGTH_SHORT).show());
            });
        });

        holder.btnViewGuideFeedback.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("activities")
                    .document(activityId)
                    .collection("registrations")
                    .whereEqualTo("childId", childId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            String score = snapshot.getDocuments().get(0).getString("feedbackScore");
                            String comment = snapshot.getDocuments().get(0).getString("feedbackComment");
                            Context context = v.getContext();
                            Toast.makeText(context, "ציון מהמדריך: " + score + "\nהערה: " + comment, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(v.getContext(), "אין משוב זמין מהמדריך", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(v.getContext(), "שגיאה בטעינת המשוב", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvActivityName;
        Button btnAddFeedback, btnViewGuideFeedback;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityName = itemView.findViewById(R.id.tvActivityName);
            btnAddFeedback = itemView.findViewById(R.id.btnAddParentFeedback);
            btnViewGuideFeedback = itemView.findViewById(R.id.btnViewGuideFeedback);
        }
    }
}
