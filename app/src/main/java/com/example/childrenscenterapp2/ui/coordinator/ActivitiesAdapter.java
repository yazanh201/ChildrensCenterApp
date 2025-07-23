package com.example.childrenscenterapp2.ui.coordinator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * אדפטר להצגת פעילויות ברשימה
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {

    private List<ActivityModel> activityList;
    private List<ActivityModel> fullList; // 🆕 לשמירה על כל הנתונים
    private OnActivityClickListener listener;

    // סטטיסטיקות – מס' משתתפים ודירוג ממוצע
    private final Map<String, Integer> participantCounts = new HashMap<>();
    private final Map<String, Double> averageScores = new HashMap<>();

    // ממשק תקשורת
    public interface OnActivityClickListener {
        void onEdit(ActivityModel activity);
        void onDelete(ActivityModel activity);
    }

    public ActivitiesAdapter(List<ActivityModel> activityList, OnActivityClickListener listener) {
        this.activityList = activityList;
        this.fullList = new ArrayList<>(activityList); // העתקה מלאה של הרשימה
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = activityList.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    // עדכון הנתונים המלאים
    public void setData(List<ActivityModel> newList) {
        this.activityList = newList;
        this.fullList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    // עדכון סטטיסטיקות מבחוץ
    public void updateStatsForActivity(String activityId, int participants, double avgScore) {
        participantCounts.put(activityId, participants);
        averageScores.put(activityId, avgScore);
        notifyDataSetChanged();
    }

    // 🟢 מיון לפי מספר משתתפים (מהרב למעט)
    public void sortByParticipantsDescending() {
        activityList.sort((a1, a2) -> {
            int p1 = participantCounts.getOrDefault(a1.getId(), 0);
            int p2 = participantCounts.getOrDefault(a2.getId(), 0);
            return Integer.compare(p2, p1); // גדול->קטן
        });
        notifyDataSetChanged();
    }

    // 🟢 מיון לפי דירוג ממוצע – החזרת רק 10 פעילויות הכי טובות
    public void sortByTopAverageScore(int topLimit) {
        activityList.sort((a1, a2) -> {
            double s1 = averageScores.getOrDefault(a1.getId(), 0.0);
            double s2 = averageScores.getOrDefault(a2.getId(), 0.0);
            return Double.compare(s2, s1); // גבוה->נמוך
        });

        // חיתוך לרק 10 הטובות אם יש יותר
        if (activityList.size() > topLimit) {
            activityList = new ArrayList<>(activityList.subList(0, topLimit));
        }

        notifyDataSetChanged();
    }

    // 🔁 איפוס לרשימה המקורית
    public void resetData() {
        this.activityList = new ArrayList<>(fullList);
        notifyDataSetChanged();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvMonth, tvParticipants, tvAverageScore;
        Button btnEdit, btnDelete;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvParticipants = itemView.findViewById(R.id.tvParticipantsCount);
            tvAverageScore = itemView.findViewById(R.id.tvFeedbackAvg);
        }

        public void bind(ActivityModel activity) {
            tvName.setText(activity.getName());
            tvDomain.setText("תחום: " + activity.getDomain());
            tvMonth.setText("חודש: " + activity.getMonth());

            btnEdit.setOnClickListener(v -> listener.onEdit(activity));
            btnDelete.setOnClickListener(v -> listener.onDelete(activity));

            // הצגת הסטטיסטיקות מתוך המפות
            int participants = participantCounts.getOrDefault(activity.getId(), 0);
            double avgScore = averageScores.getOrDefault(activity.getId(), 0.0);

            tvParticipants.setText("משתתפים: " + participants);
            tvAverageScore.setText("דירוג ממוצע: " + String.format("%.1f", avgScore));
        }
    }


}
