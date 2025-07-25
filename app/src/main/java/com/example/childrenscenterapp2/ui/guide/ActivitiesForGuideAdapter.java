package com.example.childrenscenterapp2.ui.guide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;

import java.util.*;

/**
 * Adapter שמציג פעילויות למדריך (Guide)
 * כולל נתוני משתתפים ודירוג ממוצע, ללא כפתורי עריכה/מחיקה.
 */
public class ActivitiesForGuideAdapter extends RecyclerView.Adapter<ActivitiesForGuideAdapter.ActivityViewHolder> {

    // רשימת הפעילויות להציג
    private List<ActivityModel> activityList;

    // מפות לשמירת מספר משתתפים ודירוג ממוצע עבור כל פעילות לפי ID
    private final Map<String, Integer> participantCounts = new HashMap<>();
    private final Map<String, Double> averageScores = new HashMap<>();

    /**
     * בנאי של האדפטר - מקבל רשימת פעילויות
     */
    public ActivitiesForGuideAdapter(List<ActivityModel> activityList) {
        this.activityList = activityList;
    }

    /**
     * יצירת ViewHolder חדש לפי הפריסה activity_item.xml
     */
    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item, parent, false);
        return new ActivityViewHolder(view);
    }

    /**
     * קישור הנתונים מהפעילות לרכיבי התצוגה בפריט
     */
    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = activityList.get(position);
        holder.bind(activity);
    }

    /**
     * מחזיר את מספר הפריטים ברשימה
     */
    @Override
    public int getItemCount() {
        return activityList.size();
    }

    /**
     * עדכון רשימת הפעילויות כולה
     */
    public void setData(List<ActivityModel> newList) {
        this.activityList = newList;
        notifyDataSetChanged();
    }

    /**
     * עדכון מספר משתתפים ודירוג ממוצע לפעילות מסוימת לפי מזהה
     */
    public void updateStatsForActivity(String activityId, int participants, double avgScore) {
        participantCounts.put(activityId, participants);
        averageScores.put(activityId, avgScore);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder פנימי שמחזיק את רכיבי התצוגה של כל פעילות
     */
    class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvMonth, tvParticipants, tvAverageScore;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvParticipants = itemView.findViewById(R.id.tvParticipantsCount);
            tvAverageScore = itemView.findViewById(R.id.tvFeedbackAvg);

            // הסתרת כפתורי עריכה ומחיקה - לא רלוונטיים למדריך
            itemView.findViewById(R.id.btnEdit).setVisibility(View.GONE);
            itemView.findViewById(R.id.btnDelete).setVisibility(View.GONE);
        }

        /**
         * קישור אובייקט פעילות לרכיבי התצוגה של הפריט
         */
        public void bind(ActivityModel activity) {
            tvName.setText(activity.getName());
            tvDomain.setText("תחום: " + activity.getDomain());
            tvMonth.setText("חודש: " + activity.getMonth());

            int participants = participantCounts.getOrDefault(activity.getId(), 0);
            double avgScore = averageScores.getOrDefault(activity.getId(), 0.0);

            tvParticipants.setText("משתתפים: " + participants);
            tvAverageScore.setText("דירוג ממוצע: " + String.format("%.1f", avgScore));
        }
    }
}
