package com.example.childrenscenterapp2.ui.guide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;

import java.util.List;

public class GuideActivitiesAdapter extends RecyclerView.Adapter<GuideActivitiesAdapter.ActivityViewHolder> {

    private List<ActivityModel> activities;
    private OnParticipantsClickListener listener;

    public interface OnParticipantsClickListener {
        void onParticipantsClick(String activityId, String activityName);
    }

    public GuideActivitiesAdapter(List<ActivityModel> activities, OnParticipantsClickListener listener) {
        this.activities = activities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_guide, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = activities.get(position);

        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText("תחום: " + activity.getDomain());
        holder.tvMonth.setText("חודש: " + activity.getMonth());

        holder.btnParticipantsList.setVisibility(View.VISIBLE);
        holder.btnPhotos.setVisibility(View.VISIBLE);

        holder.btnParticipantsList.setOnClickListener(v -> {
            if (listener != null) {
                listener.onParticipantsClick(activity.getId(), activity.getName());
            }
        });

        holder.btnPhotos.setOnClickListener(v -> {
            // תוכל להוסיף כאן פעולה להעלאת תמונות אם תרצה
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvMonth;
        Button btnParticipantsList, btnPhotos;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            btnParticipantsList = itemView.findViewById(R.id.btnParticipentschilds);
            btnPhotos = itemView.findViewById(R.id.btnPhotos);
        }
    }
}
