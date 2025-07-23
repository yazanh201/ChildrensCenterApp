package com.example.childrenscenterapp2.ui.guide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticipantsAdapter_guide extends RecyclerView.Adapter<ParticipantsAdapter_guide.ParticipantViewHolder> {

    private List<Map<String, Object>> participants;
    private String activityId;
    private Map<String, String> emailToNameCache = new HashMap<>();

    public ParticipantsAdapter_guide(List<Map<String, Object>> participants, String activityId) {
        this.participants = participants;
        this.activityId = activityId;
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        Map<String, Object> participant = participants.get(position);
        String childNameOrEmail = (String) participant.get("childName");
        String childId = (String) participant.get("childId");

        if (childNameOrEmail != null && childNameOrEmail.contains("@")) {
            // מדובר במייל – ננסה לחפש את השם בטבלת users
            if (emailToNameCache.containsKey(childNameOrEmail)) {
                holder.tvName.setText(emailToNameCache.get(childNameOrEmail));
            } else {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereEqualTo("email", childNameOrEmail)
                        .get()
                        .addOnSuccessListener(query -> {
                            if (!query.isEmpty()) {
                                String fullName = query.getDocuments().get(0).getString("name");
                                emailToNameCache.put(childNameOrEmail, fullName);
                                holder.tvName.setText(fullName);
                            } else {
                                holder.tvName.setText("שם לא נמצא");
                            }
                        })
                        .addOnFailureListener(e -> holder.tvName.setText("שגיאה בטעינה"));
            }
        } else {
            // זה שם רגיל
            holder.tvName.setText(childNameOrEmail);
        }

        holder.btnFeedback.setOnClickListener(v -> {
            DialogFeedbackInput.showDialog(v.getContext(), (score, comment) -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("activities")
                        .document(activityId)
                        .collection("registrations")
                        .whereEqualTo("childId", childId)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                db.collection("activities")
                                        .document(activityId)
                                        .collection("registrations")
                                        .document(snapshot.getDocuments().get(0).getId())
                                        .update("feedbackScore", score, "feedbackComment", comment);
                                Toast.makeText(v.getContext(), "✅ משוב נשמר", Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        });
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnFeedback;

        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChildName);
            btnFeedback = itemView.findViewById(R.id.btnAddFeedback);
        }
    }
}
