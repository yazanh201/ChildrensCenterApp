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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code ScheduleAdapter} – אדפטר מותאם להצגת לוח הזמנים של הילד.
 * <p>
 * תפקיד המחלקה:
 * <ul>
 *   <li>מציגה את הפעילויות שהילד רשום אליהן ברשימת RecyclerView.</li>
 *   <li>מציגה את שם הפעילות, תחום הפעילות, הימים בהם היא מתקיימת, וציון ממוצע.</li>
 *   <li>מאפשרת מחיקת פעילות מהרשימה באמצעות כפתור ייעודי.</li>
 * </ul>
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    /** רשימת הפעילויות בלוח הזמנים */
    private List<ActivityModel> scheduleList;

    /** מאזין ללחיצת מחיקה */
    private OnDeleteClickListener deleteClickListener;

    /** טבלת ציונים ממוצעים לפי מזהה פעילות */
    private Map<String, Double> scores = new HashMap<>();

    /**
     * ממשק להאזנה למחיקת פעילות מהלוח.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(ActivityModel activity);
    }

    /**
     * בנאי לאדפטר.
     *
     * @param scheduleList        רשימת הפעילויות להצגה.
     * @param deleteClickListener מאזין למחיקה.
     */
    public ScheduleAdapter(List<ActivityModel> scheduleList, OnDeleteClickListener deleteClickListener) {
        this.scheduleList = scheduleList;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_activity, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ActivityModel activity = scheduleList.get(position);

        // הצגת פרטי הפעילות
        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText("תחום: " + activity.getDomain());
        holder.tvDays.setText("ימים: " + android.text.TextUtils.join(", ", activity.getDays()));

        // הצגת ציון ממוצע אם קיים
        Double avgScore = scores.get(activity.getId());
        if (avgScore != null) {
            holder.tvAverageScore.setText("ציון ממוצע: " + String.format("%.1f", avgScore));
        } else {
            holder.tvAverageScore.setText("ציון ממוצע: לא זמין");
        }

        // לחיצה על כפתור מחיקה – מעבירה את האירוע למאזין החיצוני
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

    /**
     * {@code ScheduleViewHolder} – מחזיק התצוגה לכל פריט בלוח הזמנים.
     */
    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvDays, tvAverageScore;
        ImageButton btnDelete;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvAverageScore = itemView.findViewById(R.id.tvAverageScore);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    /**
     * עדכון רשימת הפעילויות בלוח הזמנים.
     *
     * @param newSchedule רשימה חדשה של פעילויות.
     */
    public void updateData(List<ActivityModel> newSchedule) {
        this.scheduleList = newSchedule;
        notifyDataSetChanged();
    }

    /**
     * עדכון ציון ממוצע עבור פעילות מסוימת.
     *
     * @param activityId מזהה הפעילות.
     * @param score      הציון הממוצע לעדכון.
     */
    public void updateScore(String activityId, double score) {
        scores.put(activityId, score);
        notifyDataSetChanged();
    }
}
