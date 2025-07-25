package com.example.childrenscenterapp2.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class ActivitiesSimpleAdapter extends RecyclerView.Adapter<ActivitiesSimpleAdapter.ViewHolder> {

    // רשימת הפעילויות שמוצגות ברשימה
    private final List<Map<String, Object>> activityList;

    // מנהל ה־Fragments עבור פתיחת מסכי צפייה בביקורות
    private final FragmentManager fragmentManager;

    // בנאי שמקבל את רשימת הפעילויות ואת FragmentManager
    public ActivitiesSimpleAdapter(List<Map<String, Object>> activityList, FragmentManager fragmentManager) {
        this.activityList = activityList;
        this.fragmentManager = fragmentManager;
    }

    // יצירת ViewHolder חדש לכל פריט ברשימה
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_simple_admin, parent, false);
        return new ViewHolder(view);
    }

    // קישור נתונים לכל פריט ברשימה לפי מיקום
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> activity = activityList.get(position);

        String activityId = (String) activity.get("id");
        holder.tvName.setText((String) activity.get("name"));
        holder.tvDomain.setText("תחום: " + activity.get("domain"));
        holder.tvGuide.setText("מדריך: " + activity.get("guideName"));
        holder.tvMonth.setText("חודש: " + activity.get("month"));

        // טווח גילאים
        Object minAge = activity.get("minAge");
        Object maxAge = activity.get("maxAge");
        holder.tvAgeRange.setText("גילאים: " + minAge + " עד " + maxAge);

        // ימים שבהם הפעילות מתקיימת
        List<String> days = (List<String>) activity.get("days");
        if (days != null && !days.isEmpty()) {
            holder.tvDays.setText("ימים: " + String.join(", ", days));
        } else {
            holder.tvDays.setText("ימים: לא צוין");
        }

        // מספר משתתפים מקסימלי
        holder.tvMaxParticipants.setText("משתתפים מקס': " + activity.get("maxParticipants"));

        // האם הפעילות חד פעמית
        boolean oneTime = activity.get("oneTime") != null && (boolean) activity.get("oneTime");
        holder.tvOneTime.setText("חד פעמית: " + (oneTime ? "כן" : "לא"));

        // הגדרת Spinner לבחירת סטטוס
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                holder.itemView.getContext(),
                R.array.status_options,
                android.R.layout.simple_spinner_item
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerStatus.setAdapter(statusAdapter);

        // סטטוס נוכחי של הפעילות
        String currentStatus = activity.get("status") != null ? activity.get("status").toString() : "בסדר";
        int index = statusAdapter.getPosition(currentStatus);
        holder.spinnerStatus.setSelection(index >= 0 ? index : 1); // ברירת מחדל: "בסדר"

        // כפתור לשמירת הסטטוס שבחר המנהל
        holder.btnSaveStatus.setOnClickListener(v -> {
            String selectedStatus = holder.spinnerStatus.getSelectedItem().toString();
            FirebaseFirestore.getInstance()
                    .collection("activities")
                    .document(activityId)
                    .update("status", selectedStatus)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(holder.itemView.getContext(), "✅ סטטוס עודכן ל-" + selectedStatus, Toast.LENGTH_SHORT).show();
                        activity.put("status", selectedStatus); // עדכון מקומי של הרשימה
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "❌ שגיאה בשמירת סטטוס", Toast.LENGTH_SHORT).show();
                    });
        });

        // כפתור לצפייה בכל הביקורות של הפעילות
        holder.btnViewReviews.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("activityId", activityId);

            ViewAllFeedbacksFragment fragment = new ViewAllFeedbacksFragment();
            fragment.setArguments(bundle);

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    // מספר הפריטים ברשימה
    @Override
    public int getItemCount() {
        return activityList.size();
    }

    // מחזיק את רכיבי התצוגה של כל כרטיס פעילות
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvGuide, tvMonth,
                tvAgeRange, tvDays, tvMaxParticipants, tvOneTime;
        Button btnViewReviews, btnSaveStatus;
        Spinner spinnerStatus;

        // אתחול רכיבי התצוגה לפי מזהים מתוך ה-XML
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvGuide = itemView.findViewById(R.id.tvGuide);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvAgeRange = itemView.findViewById(R.id.tvAgeRange);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvMaxParticipants = itemView.findViewById(R.id.tvMaxParticipants);
            tvOneTime = itemView.findViewById(R.id.tvOneTime);
            btnViewReviews = itemView.findViewById(R.id.btnViewReviews);
            btnSaveStatus = itemView.findViewById(R.id.btnSaveStatus);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
        }
    }
}
