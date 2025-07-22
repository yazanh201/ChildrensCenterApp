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
            String newActivityId = activity.getId();
            List<String> newDays = activity.getDays();

            if (newActivityId == null || newDays == null) {
                Toast.makeText(holder.itemView.getContext(), "⚠️ שגיאה בנתוני פעילות", Toast.LENGTH_SHORT).show();
                return;
            }

            // בדיקה אם כבר רשום לפעילות הזו עצמה
            db.collection("activities")
                    .document(newActivityId)
                    .collection("registrations")
                    .whereEqualTo("childId", childId)
                    .get()
                    .addOnSuccessListener(regSnapshot -> {
                        if (!regSnapshot.isEmpty()) {
                            Toast.makeText(holder.itemView.getContext(), "⚠️ כבר נרשמת לפעילות זו", Toast.LENGTH_SHORT).show();
                        } else {
                            // בדיקה של חפיפת ימים עם פעילויות אחרות
                            db.collection("users")
                                    .document(childId)
                                    .collection("registrations")
                                    .get()
                                    .addOnSuccessListener(userRegs -> {
                                        final boolean[] hasConflict = {false};

                                        final int[] checksDone = {0};
                                        int totalChecks = userRegs.size();

                                        if (totalChecks == 0) {
                                            // אין שום פעילות קודמת – ישר נרשום
                                            registerActivity(db, holder, user, activity, childId);
                                            return;
                                        }

                                        for (DocumentSnapshot doc : userRegs.getDocuments()) {
                                            String existingActivityId = doc.getString("activityId");

                                            if (existingActivityId == null) {
                                                checksDone[0]++;
                                                if (checksDone[0] == totalChecks && !hasConflict[0]) {
                                                    registerActivity(db, holder, user, activity, childId);
                                                }
                                                continue;
                                            }

                                            db.collection("activities")
                                                    .document(existingActivityId)
                                                    .get()
                                                    .addOnSuccessListener(activityDoc -> {
                                                        List<String> existingDays = (List<String>) activityDoc.get("days");

                                                        // בדיקת חפיפת ימים
                                                        if (existingDays != null) {
                                                            for (String day : existingDays) {
                                                                if (newDays.contains(day)) {
                                                                    hasConflict[0] = true;
                                                                    Toast.makeText(holder.itemView.getContext(), "⚠️ יש חפיפה בימים עם פעילות אחרת", Toast.LENGTH_SHORT).show();
                                                                    break;
                                                                }
                                                            }
                                                        }

                                                        checksDone[0]++;
                                                        if (checksDone[0] == totalChecks && !hasConflict[0]) {
                                                            registerActivity(db, holder, user, activity, childId);
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        checksDone[0]++;
                                                        if (checksDone[0] == totalChecks && !hasConflict[0]) {
                                                            registerActivity(db, holder, user, activity, childId);
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(holder.itemView.getContext(), "שגיאה בבדיקת הרשמות קודמות", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "שגיאה בבדיקה", Toast.LENGTH_SHORT).show();
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

    // רישום הפעילות בפועל
    private void registerActivity(FirebaseFirestore db, ActivityViewHolder holder, FirebaseUser user,
                                  ActivityModel activity, String childId) {

        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("childId", childId);
        registrationData.put("childName", user.getEmail());
        registrationData.put("registeredAt", Timestamp.now());

        db.collection("activities")
                .document(activity.getId())
                .collection("registrations")
                .add(registrationData)
                .addOnSuccessListener(documentReference -> {

                    Map<String, Object> userReg = new HashMap<>();
                    userReg.put("activityId", activity.getId());
                    userReg.put("domain", activity.getDomain());
                    userReg.put("days", activity.getDays());
                    userReg.put("timestamp", Timestamp.now());

                    db.collection("users")
                            .document(childId)
                            .collection("registrations")
                            .document(activity.getId())
                            .set(userReg)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(holder.itemView.getContext(), "✅ נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(holder.itemView.getContext(), "שגיאה בשמירה אצל המשתמש", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "שגיאה בהרשמה", Toast.LENGTH_SHORT).show();
                });
    }
}
