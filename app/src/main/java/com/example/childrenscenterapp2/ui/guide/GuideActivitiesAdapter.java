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

    // ממשק ללחיצות על כפתורים שונים בcardview (כפתור העלאת תמונות , כפתור צפיה במשתתפים)
    public interface OnParticipantsClickListener {
        void onParticipantsClick(String activityId, String activityName); // צפייה במשתתפים
        void onUploadPhotosClick(String activityId);                      // פתיחת פרגמנט העלאת תמונות
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

        // לחיצה על כפתור צפייה במשתתפים
        holder.btnParticipantsList.setOnClickListener(v -> {
            if (listener != null) {
                listener.onParticipantsClick(activity.getId(), activity.getName());
            }
        });

        // לחיצה על כפתור העלאת תמונות
        holder.btnPhotos.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUploadPhotosClick(activity.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    // ViewHolder של כרטיס פעילות
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
