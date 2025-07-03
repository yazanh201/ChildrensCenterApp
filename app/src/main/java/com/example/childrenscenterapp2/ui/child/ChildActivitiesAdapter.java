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
            Log.d("ChildAdapter", "Button clicked for: " + activity.getName());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String childId = user.getUid();

            // קודם נחפש במסד ה-users
            db.collection("users").document(childId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        final String nameToUse;

                        if (documentSnapshot.exists()) {
                            String fetchedName = documentSnapshot.getString("name");
                            if (fetchedName != null && !fetchedName.trim().isEmpty()) {
                                nameToUse = fetchedName;
                            } else if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
                                nameToUse = user.getDisplayName();
                            } else {
                                nameToUse = user.getEmail();
                            }
                        } else if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
                            nameToUse = user.getDisplayName();
                        } else {
                            nameToUse = user.getEmail();
                        }

                        // בדוק אם כבר רשום
                        db.collection("activities")
                                .document(activity.getId())
                                .collection("registrations")
                                .whereEqualTo("childId", childId)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        Toast.makeText(holder.itemView.getContext(), "⚠️ כבר נרשמת לפעילות זו", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Map<String, Object> registration = new HashMap<>();
                                        registration.put("childId", childId);
                                        registration.put("childName", nameToUse);
                                        registration.put("registeredAt", Timestamp.now());

                                        db.collection("activities")
                                                .document(activity.getId())
                                                .collection("registrations")
                                                .add(registration)
                                                .addOnSuccessListener(documentReference -> {
                                                    Toast.makeText(holder.itemView.getContext(), "✅ נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(holder.itemView.getContext(), "❌ שגיאה בהרשמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(holder.itemView.getContext(), "❌ שגיאה בבדיקת הרשמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "❌ שגיאה בשליפת שם משתמש: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
