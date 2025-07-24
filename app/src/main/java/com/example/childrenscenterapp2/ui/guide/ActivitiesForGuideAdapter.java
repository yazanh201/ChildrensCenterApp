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

public class ActivitiesForGuideAdapter extends RecyclerView.Adapter<ActivitiesForGuideAdapter.ActivityViewHolder> {

    private List<ActivityModel> activityList;
    private final Map<String, Integer> participantCounts = new HashMap<>();
    private final Map<String, Double> averageScores = new HashMap<>();

    public ActivitiesForGuideAdapter(List<ActivityModel> activityList) {
        this.activityList = activityList;
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

    public void setData(List<ActivityModel> newList) {
        this.activityList = newList;
        notifyDataSetChanged();
    }

    public void updateStatsForActivity(String activityId, int participants, double avgScore) {
        participantCounts.put(activityId, participants);
        averageScores.put(activityId, avgScore);
        notifyDataSetChanged();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvMonth, tvParticipants, tvAverageScore;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvParticipants = itemView.findViewById(R.id.tvParticipantsCount);
            tvAverageScore = itemView.findViewById(R.id.tvFeedbackAvg);

            // הסתרת כפתורי עריכה ומחיקה
            itemView.findViewById(R.id.btnEdit).setVisibility(View.GONE);
            itemView.findViewById(R.id.btnDelete).setVisibility(View.GONE);
        }

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
