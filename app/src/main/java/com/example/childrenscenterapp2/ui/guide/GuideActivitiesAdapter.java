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

    public class GuideActivitiesAdapter extends RecyclerView.Adapter<com.example.childrenscenterapp2.ui.guide.GuideActivitiesAdapter.ActivityViewHolder> {

        private List<ActivityModel> activities;

    public GuideActivitiesAdapter(List<ActivityModel> activities) {
        this.activities = activities;
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

        // מציג רק כפתורי משוב ותמונות
        holder.btnFeedback.setVisibility(View.VISIBLE);
        holder.btnPhotos.setVisibility(View.VISIBLE);

        holder.btnFeedback.setOnClickListener(v -> {
            // TODO: כתוב כאן קוד פתיחת מסך המשוב
        });

        holder.btnPhotos.setOnClickListener(v -> {
            // TODO: כתוב כאן קוד פתיחת מסך העלאת התמונות
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }


    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvMonth;
        Button btnFeedback, btnPhotos;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            btnFeedback = itemView.findViewById(R.id.btnParticipentschilds);
            btnPhotos = itemView.findViewById(R.id.btnPhotos);
        }
    }
}
