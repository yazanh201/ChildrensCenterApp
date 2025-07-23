package com.example.childrenscenterapp2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;

import java.util.List;

public class ActivitiesSimpleAdapter extends RecyclerView.Adapter<ActivitiesSimpleAdapter.ViewHolder> {

    private final List<ActivityModel> activityList;

    public ActivitiesSimpleAdapter(List<ActivityModel> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityModel activity = activityList.get(position);

        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText("תחום: " + activity.getDomain());
        holder.tvGuide.setText("מדריך: " + activity.getGuideName());
        holder.tvMonth.setText("חודש: " + activity.getMonth());

        String ageRange = activity.getMinAge() + " עד " + activity.getMaxAge();
        holder.tvAgeRange.setText("גילאים: " + ageRange);

        if (activity.getDays() != null && !activity.getDays().isEmpty()) {
            holder.tvDays.setText("ימים: " + String.join(", ", activity.getDays()));
        } else {
            holder.tvDays.setText("ימים: לא צוין");
        }

        holder.tvMaxParticipants.setText("משתתפים מקס': " + activity.getMaxParticipants());
        holder.tvOneTime.setText("חד פעמית: " + (activity.isOneTime() ? "כן" : "לא"));
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvGuide, tvMonth,
                tvAgeRange, tvDays, tvMaxParticipants, tvOneTime;

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
        }
    }
}
