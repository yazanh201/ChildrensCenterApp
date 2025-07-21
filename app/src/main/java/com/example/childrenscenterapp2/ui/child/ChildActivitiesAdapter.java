package com.example.childrenscenterapp2.ui.child;

import android.util.Log;
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

    public ChildActivitiesAdapter(List<ActivityModel> activities) {
        this.activities = activities;
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
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                Toast.makeText(holder.itemView.getContext(), "⚠️ לא מחובר למערכת", Toast.LENGTH_SHORT).show();
                return;
            }

            String childId = user.getUid();
            String activityId = activity.getId();
            String domain = activity.getDomain();

            if (activityId == null || domain == null) {
                Toast.makeText(holder.itemView.getContext(), "⚠️ שגיאה בפרטי הפעילות", Toast.LENGTH_SHORT).show();
                return;
            }

            // נבדוק אם הילד כבר נרשם לפעילות מאותו תחום
            db.collection("users")
                    .document(childId)
                    .collection("registrations")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        boolean alreadyInDomain = false;

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            String existingDomain = doc.getString("domain");
                            if (existingDomain != null && existingDomain.equals(domain)) {
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

                        // נבדוק אם כבר רשום לפעילות הזו עצמה
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
                                        // נרשום את הילד
                                        Map<String, Object> activityReg = new HashMap<>();
                                        activityReg.put("childId", childId);
                                        activityReg.put("childName", user.getEmail());
                                        activityReg.put("registeredAt", Timestamp.now());

                                        db.collection("activities")
                                                .document(activityId)
                                                .collection("registrations")
                                                .add(activityReg)
                                                .addOnSuccessListener(docRef -> {

                                                    Map<String, Object> userReg = new HashMap<>();
                                                    userReg.put("activityId", activityId);
                                                    userReg.put("domain", domain); // נדרש לבדיקה בעתיד
                                                    userReg.put("timestamp", Timestamp.now());

                                                    db.collection("users")
                                                            .document(childId)
                                                            .collection("registrations")
                                                            .document(activityId)
                                                            .set(userReg)
                                                            .addOnSuccessListener(unused -> {
                                                                Toast.makeText(holder.itemView.getContext(),
                                                                        "✅ נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(holder.itemView.getContext(),
                                                                        "שגיאה בשמירה אצל המשתמש", Toast.LENGTH_SHORT).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(holder.itemView.getContext(),
                                                            "שגיאה בהרשמה לפעילות", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(holder.itemView.getContext(),
                                            "שגיאה בבדיקת הרשמה", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(),
                                "שגיאה בבדיקת תחומים קודמים", Toast.LENGTH_SHORT).show();
                    });
        });


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
