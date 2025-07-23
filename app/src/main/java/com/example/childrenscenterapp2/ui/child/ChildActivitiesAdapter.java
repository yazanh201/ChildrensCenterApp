package com.example.childrenscenterapp2.ui.child;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ChildActivitiesAdapter extends RecyclerView.Adapter<ChildActivitiesAdapter.ActivityViewHolder> {

    private List<ActivityModel> activities;
    private String overrideChildId = null;
    private String overrideChildName = null;

    public ChildActivitiesAdapter(List<ActivityModel> activities) {
        this.activities = activities;
    }

    // אפשרות להזין ידנית את פרטי הילד (למשל כשנבחר ע"י הורה)
    public void setChildOverride(String childId, String childName) {
        this.overrideChildId = childId;
        this.overrideChildName = childName;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_child, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = activities.get(position);

        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText("תחום: " + activity.getDomain());
        String daysString = android.text.TextUtils.join(", ", activity.getDays());
        holder.tvDays.setText("ימים: " + daysString);
        holder.tvAge.setText("גיל מתאים: " + activity.getMinAge() + "-" + activity.getMaxAge());

        holder.btnRegister.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) {
                Toast.makeText(holder.itemView.getContext(), "⚠️ לא מחובר למערכת", Toast.LENGTH_SHORT).show();
                return;
            }

            // שימוש בילד שנבחר ע"י הורה או בילד המחובר
            String childId = (overrideChildId != null) ? overrideChildId : currentUser.getUid();
            String childName = (overrideChildName != null) ? overrideChildName : currentUser.getEmail();

            String activityId = activity.getId();
            String domain = activity.getDomain();

            if (activityId == null || domain == null) {
                Toast.makeText(holder.itemView.getContext(), "⚠️ שגיאה בפרטי הפעילות", Toast.LENGTH_SHORT).show();
                return;
            }

            // בדיקה אם הילד כבר רשום לפעילות בתחום הזה
            db.collection("users")
                    .document(childId)
                    .collection("registrations")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        boolean alreadyInDomain = false;
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            String existingDomain = doc.getString("domain");
                            if (domain.equals(existingDomain)) {
                                alreadyInDomain = true;
                                break;
                            }
                        }

                        if (alreadyInDomain) {
                            Toast.makeText(holder.itemView.getContext(),
                                    "כבר נרשמת לפעילות בתחום הזה: " + domain,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // בדיקה אם הילד כבר רשום לפעילות הזו עצמה
                        db.collection("activities")
                                .document(activityId)
                                .collection("registrations")
                                .whereEqualTo("childId", childId)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        Toast.makeText(holder.itemView.getContext(),
                                                "⚠️ כבר נרשמת לפעילות זו",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        // ביצוע רישום
                                        registerChildToActivity(db, holder, childId, childName, activityId, domain);
                                    }
                                })
                                .addOnFailureListener(e -> showError(holder, "שגיאה בבדיקת הרשמה"));
                    })
                    .addOnFailureListener(e -> showError(holder, "שגיאה בבדיקת תחומים קודמים"));
        });
    }

    private void registerChildToActivity(FirebaseFirestore db, ActivityViewHolder holder,
                                         String childId, String childName,
                                         String activityId, String domain) {

        Map<String, Object> activityReg = new HashMap<>();
        activityReg.put("childId", childId);
        activityReg.put("childName", childName);
        activityReg.put("registeredAt", Timestamp.now());

        db.collection("activities")
                .document(activityId)
                .collection("registrations")
                .add(activityReg)
                .addOnSuccessListener(docRef -> {
                    Map<String, Object> userReg = new HashMap<>();
                    userReg.put("activityId", activityId);
                    userReg.put("domain", domain);
                    userReg.put("timestamp", Timestamp.now());

                    db.collection("users")
                            .document(childId)
                            .collection("registrations")
                            .document(activityId)
                            .set(userReg)
                            .addOnSuccessListener(unused -> Toast.makeText(holder.itemView.getContext(),
                                    "✅ נרשמת בהצלחה!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> showError(holder, "שגיאה בשמירה אצל המשתמש"));
                })
                .addOnFailureListener(e -> showError(holder, "שגיאה בהרשמה לפעילות"));
    }

    private void showError(ActivityViewHolder holder, String message) {
        Toast.makeText(holder.itemView.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public void setData(List<ActivityModel> newList) {
        this.activities = newList;
        notifyDataSetChanged();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvDays, tvAge;
        Button btnRegister;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvAge = itemView.findViewById(R.id.tvAge);
            btnRegister = itemView.findViewById(R.id.btnRegister);
        }
    }
}
