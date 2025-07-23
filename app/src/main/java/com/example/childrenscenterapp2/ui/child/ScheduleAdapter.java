package com.example.childrenscenterapp2.ui.child;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<ActivityModel> scheduleList;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(ActivityModel activity);
    }

    public ScheduleAdapter(List<ActivityModel> scheduleList, OnDeleteClickListener deleteClickListener) {
        this.scheduleList = scheduleList;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_activity, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ActivityModel activity = scheduleList.get(position);
        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText("תחום: " + activity.getDomain());
        holder.tvDays.setText("ימים: " + android.text.TextUtils.join(", ", activity.getDays()));

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(activity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvDays, tvHour;
        ImageButton btnDelete;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvDays = itemView.findViewById(R.id.tvDays);
            btnDelete = itemView.findViewById(R.id.btnDelete); // חדש
        }
    }

    public void updateData(List<ActivityModel> newSchedule) {
        this.scheduleList = newSchedule;
        notifyDataSetChanged();
    }
}
